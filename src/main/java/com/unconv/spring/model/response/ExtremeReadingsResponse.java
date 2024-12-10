package com.unconv.spring.model.response;

import com.unconv.spring.projection.EnvironmentalReadingProjection;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtremeReadingsResponse {

    private EnvironmentalReadingProjection maxTemperature;
    private EnvironmentalReadingProjection minTemperature;
    private EnvironmentalReadingProjection maxHumidity;
    private EnvironmentalReadingProjection minHumidity;

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
