package com.unconv.spring.service.impl;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvRoleRepository;
import com.unconv.spring.service.UnconvRoleService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnconvRoleServiceImpl implements UnconvRoleService {

    @Autowired private UnconvRoleRepository unconvRoleRepository;

    /**
     * Retrieves a paginated list of all UnconvRole entities.
     *
     * @param pageNo the page number
     * @param pageSize the size of each page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting
     * @return a {@link PagedResult} containing the UnconvRole entities
     */
    @Override
    public PagedResult<UnconvRole> findAllUnconvRoles(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<UnconvRole> unconvRolesPage = unconvRoleRepository.findAll(pageable);

        return new PagedResult<>(unconvRolesPage);
    }

    /**
     * Retrieves the UnconvRole entity with the specified ID.
     *
     * @param id the ID of the UnconvRole entity to retrieve
     * @return an {@link Optional} containing the UnconvRole entity, or empty if not found
     */
    @Override
    public Optional<UnconvRole> findUnconvRoleById(UUID id) {
        return unconvRoleRepository.findById(id);
    }

    /**
     * Retrieves the UnconvRole entity with the specified name.
     *
     * @param name the name of the UnconvRole entity to retrieve
     * @return the UnconvRole entity with the specified name
     */
    @Override
    public UnconvRole findUnconvRoleByName(String name) {
        return unconvRoleRepository.findByName(name);
    }

    /**
     * Saves a UnconvRole entity.
     *
     * @param unconvRole the UnconvRole entity to save
     * @return the saved UnconvRole entity
     */
    @Override
    public UnconvRole saveUnconvRole(UnconvRole unconvRole) {
        return unconvRoleRepository.save(unconvRole);
    }

    /**
     * Deletes the UnconvRole entity with the specified ID.
     *
     * @param id the ID of the UnconvRole entity to delete
     */
    @Override
    public void deleteUnconvRoleById(UUID id) {
        unconvRoleRepository.deleteById(id);
    }
}
