package com.velocirawesome.stellarcoyote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class PermaNowClockConfig {
    @Bean
    public Clock clock() {
        // Fixed to 2020-06-21T00:00:00Z to match PERMA_NOW 
        return Clock.fixed(Instant.parse("2020-06-21T00:00:00Z"), ZoneId.of("UTC"));
    }
}
