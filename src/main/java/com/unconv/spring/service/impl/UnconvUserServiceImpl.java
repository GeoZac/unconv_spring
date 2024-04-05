package com.unconv.spring.service.impl;

import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvRoleService;
import com.unconv.spring.service.UnconvUserService;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnconvUserServiceImpl implements UnconvUserService {

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private UnconvRoleService unconvRoleService;

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
    public boolean isUsernameUnique(String username) {
        return !unconvUserRepository.existsByUsername(username);
    }

    @Override
    public UnconvUser findUnconvUserByUserName(String username) {
        return unconvUserRepository.findByUsername(username);
    }

    @Override
    public UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword) {
        UnconvUser unconvUserWithRole = setRoleIfNotDefined(unconvUser);
        unconvUserWithRole.setPassword(bCryptPasswordEncoder().encode(rawPassword));
        return unconvUserRepository.save(unconvUser);
    }

    @Override
    public boolean checkPasswordMatch(UUID unconvUserId, String currentPassword) {
        UnconvUser unconvUser = unconvUserRepository.findUnconvUserById(unconvUserId);

        return bCryptPasswordEncoder().matches(currentPassword, unconvUser.getPassword());
    }

    @Override
    public UnconvUserDTO createUnconvUser(UnconvUserDTO unconvUserDTO) {
        UnconvRole userUnconvRole = unconvRoleService.findUnconvRoleByName("ROLE_USER");
        Set<UnconvRole> unconvRoleSet = new HashSet<>();
        unconvRoleSet.add(userUnconvRole);
        unconvUserDTO.setUnconvRoles(unconvRoleSet);

        UnconvUser savedUnconvUser =
                saveUnconvUser(
                        modelMapper.map(unconvUserDTO, UnconvUser.class),
                        unconvUserDTO.getPassword());
        UnconvUserDTO savedUnconvUserDTO = modelMapper.map(savedUnconvUser, UnconvUserDTO.class);
        savedUnconvUserDTO.setPassword(null);
        return savedUnconvUserDTO;
    }

    @Override
    public UnconvUserDTO updateUnconvUser(UnconvUser unconvUser, UnconvUserDTO unconvUserDTO) {
        unconvUserDTO.setId(unconvUser.getId());
        unconvUserDTO.setUsername(unconvUser.getUsername());
        unconvUserDTO.setUnconvRoles(unconvUser.getUnconvRoles());

        UnconvUser updatedUnconvUser =
                saveUnconvUser(
                        modelMapper.map(unconvUserDTO, UnconvUser.class),
                        unconvUserDTO.getPassword());
        UnconvUserDTO updatedUnconvUserDTO =
                modelMapper.map(updatedUnconvUser, UnconvUserDTO.class);
        updatedUnconvUserDTO.setPassword(null);
        return updatedUnconvUserDTO;
    }

    @Override
    public void deleteUnconvUserById(UUID id) {
        unconvUserRepository.deleteById(id);
    }

    private UnconvUser setRoleIfNotDefined(UnconvUser unconvUser) {
        if (unconvUser.getUnconvRoles().isEmpty()) {
            UnconvRole userUnconvRole = unconvRoleService.findUnconvRoleByName(UNCONV_USER.name());
            Set<UnconvRole> unconvRoleSet = new HashSet<>();
            unconvRoleSet.add(userUnconvRole);
            unconvUser.setUnconvRoles(unconvRoleSet);
        }
        return unconvUser;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
