package com.unconv.spring.utils;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.enums.DefaultUserRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DefaultRoleInitializer {

    private final UnconvRoleRepository unconvRoleRepository;

    public DefaultRoleInitializer(UnconvRoleRepository roleRepository) {
        this.unconvRoleRepository = roleRepository;
    }

    @PostConstruct
    public void initDefaultRoles() {
        for (DefaultUserRole defaultRole : DefaultUserRole.values()) {
            createRoleIfNotExists(defaultRole);
        }
    }

    private void createRoleIfNotExists(DefaultUserRole defaultUserRole) {
        UnconvRole existingRole = unconvRoleRepository.findByName(String.valueOf(defaultUserRole));
        if (existingRole == null) {
            UnconvRole newRole = new UnconvRole();
            newRole.setName(defaultUserRole.name());
            unconvRoleRepository.save(newRole);
        }
    }
}
