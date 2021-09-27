package com.linewell.desk.quest.area;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.honezhi.desk.quest.QuestApp;
import com.honezhi.desk.quest.area.AreaService;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuestApp.class)
public class AreaServiceTest {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AreaServiceTest.class);

    @Autowired
    private AreaService areaService;

    @Before
    public void hostSpotWarmUp() {
        Random r = new Random();
        for (int i = 0; i < 6000000; i++) {
            String phone = "13" + String.format("%09d", r.nextInt(999999999));
            areaService.getArea(phone);
        }
    }

    @Test
    public void getArea() {
        assertNull(areaService.getArea("12900012333"));

        assertEquals(1300002, areaService.getArea("13000020001").getPrefix());
        assertEquals(393, areaService.getArea("13000020001").getCode());
        assertEquals("测试地区.393", areaService.getArea("13000020001").getName());

        // Prefix取手机号前7位
        assertEquals(1969742, areaService.getArea("19697420001").getPrefix());
        // Code取Prefix对应的地区code
        assertEquals(2, areaService.getArea("19697420001").getCode());
        // Name取地区code对应的地区name
        assertEquals("广东.深圳", areaService.getArea("19697420001").getName());
    }

    /**
     * i7-1070 * 2666MHz  =>  cost:734,  tps:1362397.9
     */
    @Test
    public void benchmark() {
        Random r = new Random();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String phone = "13" + String.format("%09d", r.nextInt(999999999));
            areaService.getArea(phone);
        }
        long time = System.currentTimeMillis() - t1;
        log.info("time cost:{},  tps:{}", time, 1000000f / time * 1000);
    }

    /**
     * memory:1400 KB
     */
    @Test
    public void memoryUsage() {
        long objectSize = ObjectSizeCalculator.getObjectSize(areaService) / 1024;
        log.info("memory:{} KB", objectSize);
        assertTrue(objectSize < 10 * 1024 * 1024L);
    }

}