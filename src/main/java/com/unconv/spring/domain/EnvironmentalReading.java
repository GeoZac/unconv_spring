package com.unconv.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
import javax.validation.constraints.NotNull;

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

    @Column(nullable = false)
    @NotNull(message = "Temperature cannot be empty")
    private double temperature;

    @Column(nullable = false)
    @NotNull(message = "Humidity cannot be empty")
    private double humidity;

    @Column(nullable = false)
    @NotNull(message = "Timestamp cannot be empty")
    private OffsetDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private SensorSystem sensorSystem;
}
