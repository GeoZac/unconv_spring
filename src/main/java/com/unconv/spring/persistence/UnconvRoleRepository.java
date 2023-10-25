package com.unconv.spring.persistence;

import com.unconv.spring.domain.UnconvRole;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnconvRoleRepository extends JpaRepository<UnconvRole, UUID> {
    UnconvRole findByName(String name);
}
