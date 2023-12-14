package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

public interface UnconvUserService {
    PagedResult<UnconvUser> findAllUnconvUsers(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<UnconvUser> findUnconvUserById(UUID id);

    UnconvUser findUnconvUserByUserName(String username);

    UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword);

    boolean checkPasswordMatch(UUID unconvUserId, String currentPassword);

    ResponseEntity<MessageResponse<UnconvUserDTO>> checkUsernameUniquenessAndSaveUnconvUser(
            UnconvUser unconvUser, String rawPassword);

    void deleteUnconvUserById(UUID id);
}
