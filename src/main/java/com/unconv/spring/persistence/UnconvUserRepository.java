package com.unconv.spring.persistence;

import com.unconv.spring.domain.UnconvUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository interface for accessing UnconvUser entities in the database. */
public interface UnconvUserRepository extends JpaRepository<UnconvUser, UUID> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return the user with the specified username, or null if not found
     */
    UnconvUser findByUsername(String username);

    /**
     * Checks if a user with the specified username exists.
     *
     * @param username the username to check for existence
     * @return true if a user with the specified username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Finds a user by ID.
     *
     * @param id the ID of the user to find
     * @return an Optional containing the user with the specified ID, or an empty Optional if not
     *     found
     */
    Optional<UnconvUser> findById(UUID id);

    /**
     * Finds a user by ID.
     *
     * @param id the ID of the user to find
     * @return the user with the specified ID, or null if not found
     */
    UnconvUser findUnconvUserById(UUID id);
}
