package com.unconv.spring.persistence;

import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorSystemRepository extends JpaRepository<SensorSystem, UUID> {

    SensorSystem findSensorSystemById(UUID id);

    /**
     * Retrieves a distinct list of sensor locations associated with sensor systems belonging to a
     * specific UnconvUser identified by the provided UUID.
     *
     * <p>This method utilizes a JPQL (Java Persistence Query Language) query to select distinct
     * sensor locations from the {@link SensorSystem} entities based on the specified UnconvUser ID.
     *
     * @param unconvUserId The UUID identifying the UnconvUser whose associated sensor locations are
     *     to be retrieved.
     * @return A {@link List} containing distinct sensor locations associated with the specified
     *     UnconvUser's sensor systems.
     */
    @Query(
            "SELECT DISTINCT s.sensorLocation FROM SensorSystem s WHERE s.unconvUser.id = :unconvUserId")
    List<SensorLocation> findDistinctByUnconvUserId(@Param("unconvUserId") UUID unconvUserId);

    /**
     * Finds distinct sensor systems whose sensor names contain the specified string ignoring case,
     * and orders the result by sensor name in ascending order.
     *
     * @param sensorName The string to search for in sensor names, case-insensitive.
     * @return A list of {@code SensorSystem} objects matching the search criteria, with duplicate
     *     sensor names removed and sorted by name in ascending order.
     */
    List<SensorSystem> findDistinctBySensorNameContainingIgnoreCaseOrderBySensorNameAsc(
            String sensorName);

    /**
     * Finds distinct sensor systems whose sensor names contain the specified string ignoring case
     * and belong to the specified unconverted user, and orders the result by sensor name in
     * ascending order.
     *
     * @param sensorName The string to search for in sensor names, case-insensitive.
     * @param unconvUserId The ID of the unconverted user to whom the sensor systems belong.
     * @return A list of {@code SensorSystem} objects matching the search criteria, with duplicate
     *     sensor names removed and sorted by name in ascending order.
     */
    List<SensorSystem>
            findDistinctBySensorNameContainsIgnoreCaseAndUnconvUserIdOrderBySensorNameAsc(
                    String sensorName, UUID unconvUserId);

    /**
     * Retrieves a paginated list of sensor systems associated with a specific UnconvUser identified
     * by the provided UUID. This method includes both active and deleted sensor systems in the
     * result.
     *
     * <p><strong>Deprecated:</strong> This method has been deprecated in favor of {@link
     * #findByUnconvUserIdAndDeletedFalse(UUID, Pageable)} to provide a clearer and more precise
     * representation of active sensor systems only.
     *
     * @param unconvUserId The UUID identifying the UnconvUser whose associated sensor systems are
     *     to be retrieved.
     * @param pageable The pagination information for the result set.
     * @return A {@link Page} containing the sensor systems associated with the specified
     *     UnconvUser.
     * @deprecated Use {@link #findByUnconvUserIdAndDeletedFalse(UUID, Pageable)} for retrieving
     *     only active sensor systems.
     */
    @Deprecated(forRemoval = true)
    Page<SensorSystem> findAllByUnconvUserId(UUID unconvUserId, Pageable pageable);

    Page<SensorSystem> findByUnconvUserIdAndDeletedFalse(UUID unconvUserId, Pageable pageable);
}
