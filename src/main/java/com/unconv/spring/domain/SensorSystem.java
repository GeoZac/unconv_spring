package com.unconv.spring.domain;

import com.unconv.spring.enums.SensorStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a sensor system entity in the database. */
@Entity
@Table(name = "sensor_systems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
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

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdDate;

    @Column(nullable = false)
    private OffsetDateTime updatedDate;

    /**
     * Constructs a new SensorSystem with the specified parameters. Sets default values for backward
     * compatibility.
     *
     * @param uuid the unique identifier of the sensor system
     * @param sensorName the name of the sensor
     * @param sensorLocation the location of the sensor
     * @param unconvUser the user associated with the sensor
     */
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

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdDate = now;
        this.updatedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
