package com.unconv.spring.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Base class representing Sensor Authentication Token. */
@Entity
@Table(name = "sensor_auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Auth token cannot be empty")
    private String authToken;

    @Column(nullable = false)
    @NotNull(message = "Expiry cannot be empty")
    private OffsetDateTime expiry;

    @Column(nullable = false)
    @NotEmpty(message = "Salted auth token cannot be empty")
    private String tokenHash;

    @OneToOne
    @JoinColumn(name = "sensor_system_id", referencedColumnName = "id", unique = true)
    @NotNull(message = "Sensor system cannot be empty")
    private SensorSystem sensorSystem;

    // TODO Remove this once code is tested
    /**
     * Constructs a new {@code SensorAuthToken} with the specified id, authentication token, expiry
     * date and time, and associated sensor system.
     *
     * @param id the unique identifier for this authentication token
     * @param authToken the authentication token string
     * @param expiry the date and time when this authentication token expires
     * @param sensorSystem the {@link SensorSystem} associated with this authentication token
     */
    public SensorAuthToken(
            UUID id, String authToken, OffsetDateTime expiry, SensorSystem sensorSystem) {
        this.id = id;
        this.authToken = authToken;
        this.expiry = expiry;
        this.sensorSystem = sensorSystem;
    }
}
