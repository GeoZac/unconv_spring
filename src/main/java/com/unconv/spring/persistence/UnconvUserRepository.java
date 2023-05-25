package com.unconv.spring.persistence;

import com.unconv.spring.domain.UnconvUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnconvUserRepository extends JpaRepository<UnconvUser, UUID> {

    Optional<UnconvUser> findByUsername(String username);

    Optional<UnconvUser> findById(UUID id);
}
