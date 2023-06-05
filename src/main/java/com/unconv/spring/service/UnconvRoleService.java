package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnconvRoleService {

    private final UnconvRoleRepository unconvRoleRepository;

    @Autowired
    public UnconvRoleService(UnconvRoleRepository unconvRoleRepository) {
        this.unconvRoleRepository = unconvRoleRepository;
    }

    public List<UnconvRole> findAllUnconvRoles() {
        return unconvRoleRepository.findAll();
    }

    public Optional<UnconvRole> findUnconvRoleById(UUID id) {
        return unconvRoleRepository.findById(id);
    }

    public UnconvRole saveUnconvRole(UnconvRole unconvRole) {
        return unconvRoleRepository.save(unconvRole);
    }

    public void deleteUnconvRoleById(UUID id) {
        unconvRoleRepository.deleteById(id);
    }
}
