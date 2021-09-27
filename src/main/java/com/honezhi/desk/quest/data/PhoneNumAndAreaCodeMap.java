package com.honezhi.desk.quest.data;

import com.honezhi.desk.quest.area.Area;

import javax.validation.constraints.NotBlank;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 将数据存入JVM内存会耗费空间, 但读取的效率会提升; 需要在两者之间找到平衡点.
 * 关键点:
 *      1. 基本数据类型: 节省内存, 性能开销比引用类型小很多
 *      2. boolean < char < short < int ...
 *      3. 在数据依次递增时, Array[] 能充当 HashMap
 *
 * <===================================手机号-地区码=========================================>
 * 因为1300000~1999999有将近70w个数据, 所以我们不直接存入HashMap
 * 并且观察下面发现, 只有数字, 没有引用类型, 那么我们就尽可能的使用基本数据类型(节省内存、提升效率)
 * 1300000	354
 * 1300001	143
 * 1300002	393
 * 1300003	145         1300003只能使用int存， short存不下， 为了节省内存， 将其拆分后使用short存
 * 1300004	426
 * 1300005	179
 * 1300006	209
 * 例如: 对1300024这种长度的数字进行拆分, 将其拆为 "130" + 0024 的形式
 * 直接使用两个数组将其存起来, 前部分用HashMap的key(String)存, 后部分用HashMap的value(short)存
 * 利用short[]`数组`的特性, 既能把key后半部分存了, 又能把areaCode给存了
 * 由于HashMap的`String`key没有重复, 所以每一次查找的时间复杂度: Map<S, []>: O(1) + Array[]: O(1)
 *
 * <===================================地区码-地区名=========================================>
 * 地区是从1开始, 依次递增, 最大448, 由于数据量比较少直接使用数组将地区代码和地区名建立映射,
 * 下标位地区code `1`, 值为地区name `北京.北京`
 * 根据首地址计算+偏移量计算地址的方式, 查找时间复杂度:O(1), 空间复杂度:O(n)
 * 数组从0开始, 为了和地区code从1开始保持一致, 需要将 code-1
 *
 * <===================================info=========================================>
 * <p>@author: lmmarise.j </p>
 **/
public class PhoneNumAndAreaCodeMap {
    // todo 一个short[1w] 20kb 接近70w的数据 数据读取完成后该成员变量有 1402kb
    // 手机号前缀与地区码的映射
    private final Map<String, short[]> phoneAndCodeMap = new HashMap<>();
    // todo 这里读取完成后, 使用`java visualVM`查看发现areas对象有 33.8kb
    // 地区码与地区名的映射
    private final String[] areas;

    /**
     * 传入数据文件路径
     * @param amount   指定地区去重后的个数
     * @param dataFilePath 读取地区的文件路径
     */
    public PhoneNumAndAreaCodeMap( int amount, String dataFilePath) {
        this.areas = new String[amount];
        // 初始化数据
        readFile2Map2Array(dataFilePath);
    }

    /*<================================为成员变量初始化数据===================================>*/
    /**
     * 读取area.txt将prefix, area这两列数据存入map
     *
     * txt文本内容格式:
     * 1300024 \t 3   \t 四川.成都
     * 1300025 \t 205 \t 测试地区.205
     * 1300026 \t 313 \t 测试地区.313
     */
    private void readFile2Map2Array(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = null;
            short[] areaCode = null;
            String preKey = "";  // 起始值是130~最终值是199
            while ((line = reader.readLine()) != null) {
                String[] lineSplit = line.split("\t+");
                String prefix = lineSplit[0];       // 1300000
                String key = prefix.substring(0, 3);
                short value = Short.parseShort(prefix.substring(3, 7));
                // 每一万次循环新建一个short[], 将这个数组对象放入`phoneAndCode`Map作为value
                if (!preKey.equals(key)) {
                    // System.out.println("preKey -> " + preKey + ", " + "key -> " + key);
                    preKey = key;
                    areaCode = new short[10000];
                    phoneAndCodeMap.put(key, areaCode); // "130"存为key, 新short[]存为value
                }
                // 字符串类型的Code转short
                short _areaCode = Short.parseShort(lineSplit[1]);
                // 地区code存入与号码对应的成员变量Map
                areaCode[value] = _areaCode;
                // 地区code存入与地区名对应的成员变量Array
                setAreaValue(_areaCode, lineSplit[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*<=================================基础功能-方便测试=====================================>*/

    /**
     * 设置指定地区code的地区name
     */
    public void setAreaValue(int areaCode, String areaName) {
        if (areaCode < 1 || areaCode > areas.length) return;
        if (areas[areaCode - 1] == null) {
            areas[areaCode - 1] = areaName;
        }
    }

    /**
     * 返回地区码对应的地区名
     */
    public String getAreaNameByAreaCode(int areaCode) {
        if (areaCode < 1 || areaCode > areas.length) return null;
        return areas[areaCode - 1];
    }

    /**
     * 通过手机号前缀获取归属地码
     */
    public Short getAreaCodeByMobilePrefix(@NotBlank String mobilePrefix) {
        String key = mobilePrefix.substring(0, 3);
        try {
            return phoneAndCodeMap.get(key)[Integer.parseInt(mobilePrefix.substring(3, 7))];
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获得能匹配归属地得手机号前缀, 前7位
     */
    public String getMobilePrefix(@NotBlank String mobile) {
        return mobile.substring(0, 7);
    }


    /*<===================================下面是组合功能=======================================>*/

    /**
     * 通过手机号查询归属地, 封装到Area对象
     */
    public Area getAreaByPhoneNumber(String mobile) {
        // 根据完整得手机号获得前缀
        String prefix = getMobilePrefix(mobile);
        if (prefix == null) {
            return null;
        }
        // 查得归属地码
        Short code = getAreaCodeByMobilePrefix(prefix);
        if (code == null) {
            return null;
        }
        // 归属地码查得归属地名
        String name = getAreaNameByAreaCode(code);
        // 将归属地信息封装到Area
        if (name == null) {
            return null;
        }
        return new Area(Integer.parseInt(prefix), code, name);
    }
}
