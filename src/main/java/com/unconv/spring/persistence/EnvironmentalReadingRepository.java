package com.unconv.spring.persistence;

import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.projection.EnvironmentalReadingProjection;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

/** Repository interface for accessing {@link EnvironmentalReading} entities in the database. */
public interface EnvironmentalReadingRepository extends JpaRepository<EnvironmentalReading, UUID> {

    /**
     * Retrieves environmental readings within a specified time range.
     *
     * @param start the start time of the range
     * @param end the end time of the range
     * @return a list of environmental readings within the specified time range
     */
    List<EnvironmentalReading> findByTimestampBetween(OffsetDateTime start, OffsetDateTime end);

    /**
     * Retrieves environmental readings for a specific sensor system within a specified time range.
     *
     * @param sensorSystemId the ID of the sensor system
     * @param start the start time of the range
     * @param end the end time of the range
     * @return a list of environmental readings for the specified sensor system within the specified
     *     time range
     */
    List<EnvironmentalReading> findBySensorSystemIdAndTimestampBetween(
            UUID sensorSystemId, OffsetDateTime start, OffsetDateTime end);

    /**
     * Retrieves a page of environmental readings for a specific sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @param pageable the pagination information
     * @return a {@code Page&lt;EnvironmentalReading&gt;} of environmental readings for the
     *     specified sensor system
     */
    Page<EnvironmentalReading> findAllBySensorSystemId(UUID sensorSystemId, Pageable pageable);

    /**
     * Counts the number of environmental readings for a specific sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the number of environmental readings for the specified sensor system
     */
    long countBySensorSystemId(UUID sensorSystemId);

    /**
     * Counts the number of environmental readings for a specific sensor system within a specified
     * time range.
     *
     * @param sensorSystemId the ID of the sensor system
     * @param start the start time of the range
     * @param end the end time of the range
     * @return the number of environmental readings for the specified sensor system within the
     *     specified time range
     */
    long countBySensorSystemIdAndTimestampBetween(
            UUID sensorSystemId, OffsetDateTime start, OffsetDateTime end);

    /**
     * Retrieves the most recent environmental reading for a specific sensor system.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the most recent environmental reading for the specified sensor system, or null if not
     *     found
     */
    @Nullable
    EnvironmentalReading findFirstBySensorSystemIdOrderByTimestampDesc(UUID sensorSystemId);

    /**
     * Retrieves the first 10 environmental readings for a specific user's sensor systems, ordered
     * by timestamp in descending order.
     *
     * @param id the ID of the user
     * @return a list of the first 10 environmental readings for the specified user's sensor
     *     systems, ordered by timestamp in descending order
     */
    List<EnvironmentalReading> findFirst10BySensorSystemUnconvUserIdOrderByTimestampDesc(UUID id);

    /**
     * Finds the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     * ordered by temperature in descending order.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     *     ordered by temperature in descending order, or null if not found
     */
    EnvironmentalReadingProjection findFirstBySensorSystemIdOrderByTemperatureDesc(
            UUID sensorSystemId);

    /**
     * Finds the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     * ordered by temperature in ascending order.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     *     ordered by temperature in ascending order, or null if not found
     */
    EnvironmentalReadingProjection findFirstBySensorSystemIdOrderByTemperatureAsc(
            UUID sensorSystemId);

    /**
     * Finds the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     * ordered by humidity in descending order.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     *     ordered by humidity in descending order, or null if not found
     */
    EnvironmentalReadingProjection findFirstBySensorSystemIdOrderByHumidityDesc(
            UUID sensorSystemId);

    /**
     * Finds the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     * ordered by humidity in ascending order.
     *
     * @param sensorSystemId the ID of the sensor system
     * @return the first {@link EnvironmentalReadingProjection} for the given sensor system ID,
     *     ordered by humidity in ascending order, or null if not found
     */
    EnvironmentalReadingProjection findFirstBySensorSystemIdOrderByHumidityAsc(UUID sensorSystemId);
}
