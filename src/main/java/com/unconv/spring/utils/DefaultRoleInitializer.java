package com.unconv.spring.utils;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.enums.DefaultUserRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/** Component class responsible for initializing default roles in the system. */
@Component
public class DefaultRoleInitializer {

    private final UnconvRoleRepository unconvRoleRepository;

    /**
     * Constructs a DefaultRoleInitializer with the specified UnconvRoleRepository.
     *
     * @param roleRepository the repository for unconverted roles
     */
    public DefaultRoleInitializer(UnconvRoleRepository roleRepository) {
        this.unconvRoleRepository = roleRepository;
    }

    /**
     * Initializes default roles by creating them if they do not already exist. Invoked after the
     * bean has been constructed and dependencies have been injected.
     */
    @PostConstruct
    public void initDefaultRoles() {
        for (DefaultUserRole defaultRole : DefaultUserRole.values()) {
            createRoleIfNotExists(defaultRole);
        }
    }

    /**
     * Creates a role if it does not already exist.
     *
     * @param defaultUserRole the default user role to create
     */
    private void createRoleIfNotExists(DefaultUserRole defaultUserRole) {
        UnconvRole existingRole = unconvRoleRepository.findByName(String.valueOf(defaultUserRole));
        if (existingRole == null) {
            UnconvRole newRole = new UnconvRole();
            newRole.setName(defaultUserRole.name());
            unconvRoleRepository.save(newRole);
        }
    }
}
