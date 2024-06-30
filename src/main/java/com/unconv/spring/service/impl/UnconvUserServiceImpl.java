package com.unconv.spring.service.impl;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;

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

/**
 * Implementation of the {@link UnconvUserService} interface providing functionality related to
 * unconventional user operations. This service class manages transactions for user-related
 * operations.
 */
@Service
@Transactional
public class UnconvUserServiceImpl implements UnconvUserService {

    private final UnconvUserRepository unconvUserRepository;

    private final UnconvRoleService unconvRoleService;

    private final ModelMapper modelMapper;

    /**
     * Constructs an instance of {@link UnconvUserServiceImpl} with the specified dependencies.
     *
     * @param unconvUserRepository the repository for managing UnconvUser entities
     * @param unconvRoleService the service for managing UnconvRole entities
     * @param modelMapper the mapper for converting between DTOs and entities
     */
    @Autowired
    public UnconvUserServiceImpl(
            UnconvUserRepository unconvUserRepository,
            UnconvRoleService unconvRoleService,
            ModelMapper modelMapper) {
        this.unconvUserRepository = unconvUserRepository;
        this.unconvRoleService = unconvRoleService;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a paginated list of {@link UnconvUser}s.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of UnconvUsers.
     */
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

    /**
     * Retrieves an {@link UnconvUser} by its ID.
     *
     * @param id The ID of the UnconvUser.
     * @return An {@link Optional} containing the {@link UnconvUser}, or empty if not found.
     */
    @Override
    public Optional<UnconvUser> findUnconvUserById(UUID id) {
        return unconvUserRepository.findById(id);
    }

    /**
     * Checks if a username is unique.
     *
     * @param username The username to check.
     * @return true if the username is unique, false otherwise.
     */
    @Override
    public boolean isUsernameUnique(String username) {
        return !unconvUserRepository.existsByUsername(username);
    }

    /**
     * Retrieves an {@link UnconvUser} by username.
     *
     * @param username The username of the {@link UnconvUser}.
     * @return The {@link UnconvUser} with the given username.
     */
    @Override
    public UnconvUser findUnconvUserByUserName(String username) {
        return unconvUserRepository.findByUsername(username);
    }

    /**
     * Saves a new {@link UnconvUser}.
     *
     * @param unconvUser The UnconvUser to save.
     * @param rawPassword The raw password for the UnconvUser.
     * @return The saved UnconvUser.
     */
    @Override
    public UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword) {
        UnconvUser unconvUserWithRole = setRoleIfNotDefined(unconvUser);
        unconvUserWithRole.setPassword(bCryptPasswordEncoder().encode(rawPassword));
        return unconvUserRepository.save(unconvUser);
    }

    /**
     * Checks if the provided password matches the one stored for the given {@link UnconvUser}.
     *
     * @param unconvUserId The ID of the UnconvUser.
     * @param currentPassword The password to check.
     * @return true if the passwords match, false otherwise.
     */
    @Override
    public boolean checkPasswordMatch(UUID unconvUserId, String currentPassword) {
        UnconvUser unconvUser = unconvUserRepository.findUnconvUserById(unconvUserId);

        return bCryptPasswordEncoder().matches(currentPassword, unconvUser.getPassword());
    }

    /**
     * Creates a new {@link UnconvUser} from the provided DTO.
     *
     * @param unconvUserDTO The DTO containing UnconvUser information.
     * @return The created UnconvUserDTO.
     */
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

    /**
     * Updates an existing {@link UnconvUser} with information from the provided DTO.
     *
     * @param unconvUser The existing UnconvUser to update.
     * @param unconvUserDTO The DTO containing updated information.
     * @return The updated UnconvUserDTO.
     */
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

    /**
     * Deletes an {@link UnconvUser} by its ID.
     *
     * @param id The ID of the UnconvUser to delete.
     */
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

    /**
     * Bean definition for creating a {@link BCryptPasswordEncoder} instance.
     *
     * @return a new instance of {@link BCryptPasswordEncoder} for encoding passwords using BCrypt
     *     hashing
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
