package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface UnconvRoleService {
    PagedResult<UnconvRole> findAllUnconvRoles(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<UnconvRole> findUnconvRoleById(UUID id);

    UnconvRole saveUnconvRole(UnconvRole unconvRole);

    void deleteUnconvRoleById(UUID id);
}
