package com.unconv.spring.domain;

import com.unconv.spring.consts.SensorStatus;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensor_systems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorSystem {

    public SensorSystem(
            UUID uuid, String sensorName, SensorLocation sensorLocation, UnconvUser unconvUser) {
        this.id = uuid;
        this.sensorName = sensorName;
        this.sensorLocation = sensorLocation;
        this.unconvUser = unconvUser;

        // Set defaults for backward compatibility
        this.description = null;
        this.deleted = false;
        this.sensorStatus = SensorStatus.ACTIVE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Sensor name cannot be empty")
    private String sensorName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Deleted status cannot be null for Sensor")
    private boolean deleted = false;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    @NotNull(message = "Sensor status cannot be null")
    private SensorStatus sensorStatus;

    @ManyToOne(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "sensor_location_id")
    private SensorLocation sensorLocation;

    @ManyToOne(
            optional = false,
            fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "unconv_user_id")
    @NotNull(message = "UnconvUser cannot be empty")
    private UnconvUser unconvUser;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "humidity_threshold_id")
    private HumidityThreshold humidityThreshold;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "temperature_threshold_id")
    private TemperatureThreshold temperatureThreshold;
}
