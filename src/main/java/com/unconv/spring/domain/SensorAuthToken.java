package com.unconv.spring.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sensor_auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Auth token cannot be empty")
    private String authToken;

    @Column(nullable = false)
    @Future(message = "Expiry has to be in future")
    @NotNull(message = "Expiry cannot be empty")
    private OffsetDateTime expiry;
}
