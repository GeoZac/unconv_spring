package com.unconv.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "sensor_systems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Sensor name cannot be empty")
    private String sensorName;

    @ManyToOne
    @JoinColumn(name = "sensor_location_id")
    private SensorLocation sensorLocation;
}
