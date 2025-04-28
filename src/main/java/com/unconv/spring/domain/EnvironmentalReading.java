package com.unconv.spring.domain;

import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents an environmental reading entity in the database. */
@Entity
@Table(name = "environmental_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalReading {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @DecimalMin(value = "-9999.000")
    @DecimalMax(value = "9999.000")
    @Column(nullable = false, precision = 7)
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.00")
    @Column(nullable = false, precision = 5)
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
