package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing UnconvRole entities. */
public interface UnconvRoleService {

    /**
     * Retrieves a paginated list of all UnconvRole entities.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the UnconvRole entities
     */
    PagedResult<UnconvRole> findAllUnconvRoles(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves the UnconvRole entity with the specified ID.
     *
     * @param id the ID of the UnconvRole entity to retrieve
     * @return an {@link Optional} containing the UnconvRole entity, or empty if not found
     */
    Optional<UnconvRole> findUnconvRoleById(UUID id);

    /**
     * Retrieves the UnconvRole entity with the specified name.
     *
     * @param name the name of the UnconvRole entity to retrieve
     * @return the UnconvRole entity with the specified name
     */
    UnconvRole findUnconvRoleByName(String name);

    /**
     * Saves a UnconvRole entity.
     *
     * @param unconvRole the UnconvRole entity to save
     * @return the saved UnconvRole entity
     */
    UnconvRole saveUnconvRole(UnconvRole unconvRole);

    /**
     * Deletes the UnconvRole entity with the specified ID.
     *
     * @param id the ID of the UnconvRole entity to delete
     */
    void deleteUnconvRoleById(UUID id);
}
