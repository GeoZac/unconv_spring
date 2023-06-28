package com.unconv.spring.service.impl;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvUserService;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
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
public class UnconvUserServiceImpl implements UnconvUserService {

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private ModelMapper modelMapper;

    @Override
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

    @Override
    public Optional<UnconvUser> findUnconvUserById(UUID id) {
        return unconvUserRepository.findById(id);
    }

    @Override
    public UnconvUser findUnconvUserByUserName(String username) {
        return unconvUserRepository.findByUsername(username);
    }

    @Override
    public UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword) {
        unconvUser.setPassword(bCryptPasswordEncoder().encode(rawPassword));
        return unconvUserRepository.save(unconvUser);
    }

    @Override
    public ResponseEntity<MessageResponse<UnconvUserDTO>> checkUsernameUniquenessAndSaveUnconvUser(
            UnconvUser unconvUser, String rawPassword) {
        MessageResponse<UnconvUserDTO> messageResponse;
        HttpStatus httpStatus;
        UnconvUser existingUnconvUser =
                unconvUserRepository.findByUsername(unconvUser.getUsername());
        if (existingUnconvUser != null) {
            UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
            unconvUserDTO.setPassword(rawPassword);
            messageResponse = new MessageResponse<>(unconvUserDTO, "Username already taken");
            httpStatus = HttpStatus.BAD_REQUEST;
        } else {
            UnconvUser savedUnconvUser = saveUnconvUser(unconvUser, rawPassword);
            UnconvUserDTO savedUnconvUserDTO =
                    modelMapper.map(savedUnconvUser, UnconvUserDTO.class);
            messageResponse =
                    new MessageResponse<>(savedUnconvUserDTO, "User created successfully");
            httpStatus = HttpStatus.CREATED;
        }
        return new ResponseEntity<>(messageResponse, httpStatus);
    }

    @Override
    public void deleteUnconvUserById(UUID id) {
        unconvUserRepository.deleteById(id);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
