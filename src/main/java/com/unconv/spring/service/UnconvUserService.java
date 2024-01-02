package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface UnconvUserService {
    PagedResult<UnconvUser> findAllUnconvUsers(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<UnconvUser> findUnconvUserById(UUID id);

    boolean isUsernameUnique(String username);

    UnconvUser findUnconvUserByUserName(String username);

    UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword);

    boolean checkPasswordMatch(UUID unconvUserId, String currentPassword);

    UnconvUserDTO createUnconvUser(UnconvUserDTO unconvUserDTO);

    UnconvUserDTO updateUnconvUser(UnconvUser unconvUser, UnconvUserDTO unconvUserDTO);

    void deleteUnconvUserById(UUID id);
}
