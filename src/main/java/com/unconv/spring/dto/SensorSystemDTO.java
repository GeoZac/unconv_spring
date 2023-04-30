package com.unconv.spring.dto;

import com.unconv.spring.domain.SensorLocation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorSystemDTO {

    private UUID id;

    @NotEmpty(message = "Text cannot be empty")
    private String sensorName;

    private SensorLocation sensorLocation;
}
