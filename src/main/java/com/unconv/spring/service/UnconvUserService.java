package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnconvUserService {

    @Autowired private UnconvUserRepository unconvUserRepository;

    public PagedResult<UnconvUser> findAllUnconvUsers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<UnconvUser> unconvUsersPage = unconvUserRepository.findAll(pageable);

        return new PagedResult<>(unconvUsersPage);
    }

    public Optional<UnconvUser> findUnconvUserById(UUID id) {
        return unconvUserRepository.findById(id);
    }

    public Optional<UnconvUser> findUnconvUserByUserName(String username) {
        return unconvUserRepository.findByUsername(username);
    }

    public UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword) {
        unconvUser.setPassword(bCryptPasswordEncoder().encode(rawPassword));
        return unconvUserRepository.save(unconvUser);
    }

    public ResponseEntity<MessageResponse> checkUsernameUniquenessAndSaveUnconvUser(
            UnconvUser unconvUser, String rawPassword) {
        MessageResponse<UnconvUser> messageResponse;
        HttpStatus httpStatus;
        Optional<UnconvUser> optionalUnconvUser =
                unconvUserRepository.findByUsername(unconvUser.getUsername());
        if (optionalUnconvUser.isPresent()) {
            messageResponse = new MessageResponse<>(unconvUser, "Username already taken");
            httpStatus = HttpStatus.BAD_REQUEST;
        } else {
            UnconvUser savedUnconvUser = saveUnconvUser(unconvUser, rawPassword);
            messageResponse = new MessageResponse<>(savedUnconvUser, "User created successfully");
            httpStatus = HttpStatus.CREATED;
        }
        return new ResponseEntity<>(messageResponse, httpStatus);
    }

    public void deleteUnconvUserById(UUID id) {
        unconvUserRepository.deleteById(id);
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
