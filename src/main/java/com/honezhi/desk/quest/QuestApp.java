package com.honezhi.desk.quest;

import com.honezhi.desk.quest.data.PhoneNumAndAreaCodeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuestApp {

    @Value("${quest.area.data.path}")
    private String dataPath;

    @Bean
    public PhoneNumAndAreaCodeMap phoneNumAndAreaCodeMap() {
        return new PhoneNumAndAreaCodeMap(448, dataPath);
    }
}
