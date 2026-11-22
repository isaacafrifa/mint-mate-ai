package iam.mintmate.ai.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    // Adding this bean so we can interpret relative dates such as "last month", "today", etc.
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Amsterdam"));
    }

}
