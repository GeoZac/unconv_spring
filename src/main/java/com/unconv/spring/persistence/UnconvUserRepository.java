package com.unconv.spring.persistence;

import com.unconv.spring.domain.UnconvUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnconvUserRepository extends JpaRepository<UnconvUser, UUID> {

    UnconvUser findByUsername(String username);

    Optional<UnconvUser> findById(UUID id);
}
