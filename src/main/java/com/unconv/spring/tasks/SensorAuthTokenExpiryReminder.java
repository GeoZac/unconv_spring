package com.unconv.spring.tasks;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SensorAuthTokenExpiryReminder {

    @Scheduled(fixedRate = 604800000)
    public void remindSensorAuthTokenExpiry() {
        // TODO Implement task
    }
}
