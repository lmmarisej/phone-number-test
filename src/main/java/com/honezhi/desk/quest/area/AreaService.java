package com.honezhi.desk.quest.area;

import com.honezhi.desk.quest.data.PhoneNumAndAreaCodeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AreaService {

    @Value("${quest.area.data.path}")
    private String dataPath;

    @Autowired
    private PhoneNumAndAreaCodeMap numAndAreaCodeMap;

    public AreaService() {
    }

    /**
     * @param mobile 手机号
     * @return 返回的地区，如果未查到，返回null
     */
    public Area getArea(String mobile) {
        return numAndAreaCodeMap.getAreaByPhoneNumber(mobile);
    }

}
