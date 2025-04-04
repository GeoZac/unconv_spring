package com.unconv.spring.domain.shared;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents a general threshold entity with a discriminator column specifying the
 * threshold type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "thresholds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "threshold_type", discriminatorType = DiscriminatorType.STRING)
public class Threshold {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID id;
}
