package com.unconv.spring.projection;

import java.time.OffsetDateTime;

/**
 * Represents a projection of an environmental reading, providing essential information about
 * temperature, humidity, and timestamp.
 */
public interface EnvironmentalReadingProjection {

    /**
     * Retrieves the temperature reading in degrees Celsius.
     *
     * @return the temperature reading
     */
    double getTemperature();

    /**
     * Retrieves the humidity reading as a percentage.
     *
     * @return the humidity reading
     */
    double getHumidity();

    /**
     * Retrieves the timestamp of the environmental reading.
     *
     * @return the timestamp of the reading
     */
    OffsetDateTime getTimestamp();
}
