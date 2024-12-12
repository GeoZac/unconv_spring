package com.unconv.spring.model.response;

import com.unconv.spring.projection.EnvironmentalReadingProjection;
import lombok.Getter;
import lombok.Setter;

/**
 * This class encapsulates extreme readings for environmental factors like temperature and humidity.
 * It provides getter and setter methods for each extreme reading projection.
 */
@Getter
@Setter
public class ExtremeReadingsResponse {

    /** The maximum temperature reading projection. */
    private EnvironmentalReadingProjection maxTemperature;

    /** The minimum temperature reading projection. */
    private EnvironmentalReadingProjection minTemperature;

    /** The maximum humidity reading projection. */
    private EnvironmentalReadingProjection maxHumidity;

    /** The minimum humidity reading projection. */
    private EnvironmentalReadingProjection minHumidity;

    /**
     * Constructs an ExtremeReadingsResponse object with the specified extreme reading projections.
     *
     * @param maxTemperature The maximum temperature reading projection.
     * @param minTemperature The minimum temperature reading projection.
     * @param maxHumidity The maximum humidity reading projection.
     * @param minHumidity The minimum humidity reading projection.
     */
    public ExtremeReadingsResponse(
            EnvironmentalReadingProjection maxTemperature,
            EnvironmentalReadingProjection minTemperature,
            EnvironmentalReadingProjection maxHumidity,
            EnvironmentalReadingProjection minHumidity) {
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.maxHumidity = maxHumidity;
        this.minHumidity = minHumidity;
    }
}
