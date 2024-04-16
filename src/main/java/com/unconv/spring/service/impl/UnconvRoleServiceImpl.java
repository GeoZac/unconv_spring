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

    @Override
    public Optional<UnconvRole> findUnconvRoleById(UUID id) {
        return unconvRoleRepository.findById(id);
    }

    @Override
    public UnconvRole findUnconvRoleByName(String name) {
        return unconvRoleRepository.findByName(name);
    }

    @Override
    public UnconvRole saveUnconvRole(UnconvRole unconvRole) {
        return unconvRoleRepository.save(unconvRole);
    }

    @Override
    public void deleteUnconvRoleById(UUID id) {
        unconvRoleRepository.deleteById(id);
    }
}
