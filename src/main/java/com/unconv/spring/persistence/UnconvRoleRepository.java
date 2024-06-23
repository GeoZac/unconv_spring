package com.unconv.spring.persistence;

import com.unconv.spring.domain.UnconvRole;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing {@link UnconvRole} entities.
 *
 * @see JpaRepository
 */
public interface UnconvRoleRepository extends JpaRepository<UnconvRole, UUID> {

    /**
     * Finds an {@code UnconvRole} entity by its name.
     *
     * @param name the name of the role to search for
     * @return the found {@code UnconvRole} entity
     */
    UnconvRole findByName(String name);
}
