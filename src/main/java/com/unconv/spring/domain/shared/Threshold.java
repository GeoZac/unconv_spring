package com.unconv.spring.domain.shared;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a general threshold entity used in environmental or monitoring contexts.
 *
 * <p>This class serves as the base entity for different types of thresholds (e.g., temperature,
 * humidity) and uses a single-table inheritance strategy to store all threshold types in a single
 * database table. A discriminator column named {@code threshold_type} is used to distinguish
 * between specific threshold implementations.
 *
 * <p>Subclasses of {@code Threshold} should define specific fields relevant to the threshold type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "thresholds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "threshold_type", discriminatorType = DiscriminatorType.STRING)
public class Threshold {

    /**
     * The unique identifier for the threshold entry.
     *
     * <p>This ID is generated automatically and stored as a UUID in the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID id;
}
