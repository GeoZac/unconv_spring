package com.unconv.spring.dto;

import java.time.LocalDateTime;

public class TenMinuteTemperature {
    private final LocalDateTime interval;
    private final Double temperature;

    public TenMinuteTemperature(LocalDateTime interval, Double temperature) {
        this.interval = interval;
        this.temperature = temperature;
    }

    public LocalDateTime getInterval() {
        return interval;
    }

    public Double getTemperature() {
        return temperature;
    }
}
