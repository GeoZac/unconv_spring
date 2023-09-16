package com.unconv.spring.domain;

import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "environmental_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalReading {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @DecimalMin(value = "-9999.000")
    @DecimalMax(value = "9999.000")
    @Column(nullable = false, precision = 7, scale = 3)
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    @Column(nullable = false)
    @PastOrPresent(message = "Readings has to be in past or present")
    @NotNull(message = "Timestamp cannot be empty")
    private OffsetDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "sensor_id")
    @NotNull(message = ENVT_VALID_SENSOR_SYSTEM)
    private SensorSystem sensorSystem;
}
