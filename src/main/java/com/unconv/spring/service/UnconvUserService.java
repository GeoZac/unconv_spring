package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

/** Service interface for managing UnconvUsers. */
public interface UnconvUserService {

    /**
     * Retrieves a paginated list of UnconvUsers.
     *
     * @param pageNo The page number.
     * @param pageSize The size of each page.
     * @param sortBy The field to sort by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A paginated list of UnconvUsers.
     */
    PagedResult<UnconvUser> findAllUnconvUsers(
            int pageNo, int pageSize, String sortBy, String sortDir);

    /**
     * Retrieves an UnconvUser by its ID.
     *
     * @param id The ID of the UnconvUser.
     * @return An Optional containing the UnconvUser, or empty if not found.
     */
    Optional<UnconvUser> findUnconvUserById(UUID id);

    /**
     * Checks if a username is unique.
     *
     * @param username The username to check.
     * @return true if the username is unique, false otherwise.
     */
    boolean isUsernameUnique(String username);

    /**
     * Retrieves an UnconvUser by username.
     *
     * @param username The username of the UnconvUser.
     * @return The UnconvUser with the given username.
     */
    UnconvUser findUnconvUserByUserName(String username);

    /**
     * Saves a new UnconvUser.
     *
     * @param unconvUser The UnconvUser to save.
     * @param rawPassword The raw password for the UnconvUser.
     * @return The saved UnconvUser.
     */
    UnconvUser saveUnconvUser(UnconvUser unconvUser, String rawPassword);

    /**
     * Checks if the provided password matches the one stored for the given UnconvUser.
     *
     * @param unconvUserId The ID of the UnconvUser.
     * @param currentPassword The password to check.
     * @return true if the passwords match, false otherwise.
     */
    boolean checkPasswordMatch(UUID unconvUserId, String currentPassword);

    /**
     * Creates a new UnconvUser from the provided DTO.
     *
     * @param unconvUserDTO The DTO containing UnconvUser information.
     * @return The created UnconvUserDTO.
     */
    UnconvUserDTO createUnconvUser(UnconvUserDTO unconvUserDTO);

    /**
     * Updates an existing UnconvUser with information from the provided DTO.
     *
     * @param unconvUser The existing UnconvUser to update.
     * @param unconvUserDTO The DTO containing updated information.
     * @return The updated UnconvUserDTO.
     */
    UnconvUserDTO updateUnconvUser(UnconvUser unconvUser, UnconvUserDTO unconvUserDTO);

    /**
     * Deletes an UnconvUser by its ID.
     *
     * @param id The ID of the UnconvUser to delete.
     */
    void deleteUnconvUserById(UUID id);
}
