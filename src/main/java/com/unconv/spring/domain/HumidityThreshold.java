package com.unconv.spring.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "humidity_thresholds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HumidityThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    @NotNull(message = "Min. value cannot be empty")
    private double minValue;

    @Column(nullable = false)
    @NotNull(message = "Max. value cannot be empty")
    private double maxValue;
}
