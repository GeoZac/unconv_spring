package com.unconv.spring.projection;

import java.time.OffsetDateTime;

public interface EnvironmentalReadingProjection {
    double getTemperature();

    double getHumidity();

    OffsetDateTime getTimestamp();
}
