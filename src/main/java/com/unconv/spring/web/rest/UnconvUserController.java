package com.unconv.spring.web.rest;

import static com.unconv.spring.consts.MessageConstants.USER_CREATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_NAME_IN_USE;
import static com.unconv.spring.consts.MessageConstants.USER_PROVIDE_PASSWORD;
import static com.unconv.spring.consts.MessageConstants.USER_UPDATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_WRONG_PASSWORD;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvUserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class responsible for handling HTTP requests related to {@link UnconvUser}. It
 * provides endpoints for managing user entities.
 */
@RestController
@RequestMapping("/UnconvUser")
@Slf4j
public class UnconvUserController {

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of UnconvUsers.
     *
     * @param pageNo The page number to retrieve (default is 0).
     * @param pageSize The size of each page (default is 10).
     * @param sortBy The field to sort by (default is "sensorName").
     * @param sortDir The direction of sorting (default is "asc" for ascending).
     * @return A {@link PagedResult} containing the paginated list of {@link UnconvUser}s.
     */
    @GetMapping
    public PagedResult<UnconvUser> getAllUnconvUsers(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return unconvUserService.findAllUnconvUsers(pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Handles GET requests to retrieve a specific UnconvUser by ID.
     *
     * @param id The UUID of the UnconvUser to retrieve.
     * @return {@link ResponseEntity} containing the {@link UnconvUser} if found, or a 404 Not Found
     *     response if the {@link UnconvUser} with the given ID does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UnconvUser> getUnconvUserById(@PathVariable UUID id) {
        return unconvUserService
                .findUnconvUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to retrieve the currently authenticated user's details, including their username and
     * roles.
     *
     * @param authentication the {@link Authentication} object provided by Spring Security,
     *     containing the user's authentication information.
     * @return a {@link Map} containing the authenticated user's username under the key "username"
     *     and a list of their roles under the key "roles".
     */
    @GetMapping("/whoAmI")
    public Map<String, Object> whoAmI(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());

        List<String> roles =
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

        response.put("roles", roles);
        return response;
    }

    /**
     * Handles GET requests to check the availability of a username.
     *
     * @param username The username to check for availability.
     * @return A map indicating whether the username is available or not.
     */
    @GetMapping("/Username/Available/{username}")
    public Map<String, String> checkUsernameAvailability(@PathVariable String username) {
        boolean isAvailable = unconvUserService.isUsernameUnique(username);
        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("available", String.valueOf(isAvailable));
        return response;
    }

    /**
     * Handles POST requests to create a new UnconvUser.
     *
     * @param unconvUserDTO The {@link UnconvUserDTO} containing information about the new {@link
     *     UnconvUser} to be created.
     * @return {@link ResponseEntity} containing a {@link MessageResponse} with information about
     *     the outcome of the creation process. If the username is already in use, returns a 400 Bad
     *     Request response. Otherwise, returns a 201 Created response with information about the
     *     newly created {@link UnconvUser}.
     */
    @PostMapping
    public ResponseEntity<MessageResponse<UnconvUserDTO>> createUnconvUser(
            @RequestBody @Validated UnconvUserDTO unconvUserDTO) {
        if (!unconvUserService.isUsernameUnique(unconvUserDTO.getUsername())) {
            unconvUserDTO.setPassword(null);
            return new ResponseEntity<>(
                    new MessageResponse<>(unconvUserDTO, USER_NAME_IN_USE), HttpStatus.BAD_REQUEST);
        }
        unconvUserDTO.setId(null);
        UnconvUserDTO savedUnconvUserDTO = unconvUserService.createUnconvUser(unconvUserDTO);
        return new ResponseEntity<>(
                new MessageResponse<>(savedUnconvUserDTO, USER_CREATE_SUCCESS), HttpStatus.CREATED);
    }

    /**
     * Handles PUT requests to update an existing UnconvUser.
     *
     * @param id The {@link UUID} of the UnconvUser to update.
     * @param unconvUserDTO The {@link UnconvUserDTO} containing updated information for the {@link
     *     UnconvUser}.
     * @return {@link ResponseEntity} containing a {@link MessageResponse} with information about
     *     the outcome of the update process. If the provided current password is null, returns a
     *     400 Bad Request response indicating that the current password must be provided. If the
     *     current password provided does not match the user's actual password, returns a 401
     *     Unauthorized response indicating that the password is incorrect. If the {@link
     *     UnconvUser} with the given ID is found and updated successfully, returns a 200 OK
     *     response with information about the updated {@link UnconvUser}. If no {@link UnconvUser}
     *     with the given ID is found, returns a 404 Not Found response.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<UnconvUserDTO>> updateUnconvUser(
            @PathVariable UUID id, @RequestBody @Valid UnconvUserDTO unconvUserDTO) {

        return unconvUserService
                .findUnconvUserById(id)
                .map(
                        unconvUserObj -> {
                            if (unconvUserDTO.getCurrentPassword() == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(
                                                new MessageResponse<>(
                                                        unconvUserDTO, USER_PROVIDE_PASSWORD));
                            }
                            if (unconvUserService.checkPasswordMatch(
                                    unconvUserObj.getId(), unconvUserDTO.getCurrentPassword())) {
                                UnconvUserDTO updatedUnconvUserDTO =
                                        unconvUserService.updateUnconvUser(
                                                unconvUserObj, unconvUserDTO);
                                return ResponseEntity.ok(
                                        new MessageResponse<>(
                                                updatedUnconvUserDTO, USER_UPDATE_SUCCESS));
                            } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(
                                                new MessageResponse<>(
                                                        unconvUserDTO, USER_WRONG_PASSWORD));
                            }
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Handles DELETE requests to delete an existing UnconvUser.
     *
     * @param id The {@link UUID} of the UnconvUser to delete.
     * @return {@link ResponseEntity} containing the deleted {@link UnconvUser} if found and deleted
     *     successfully, or a 404 Not Found response if no {@link UnconvUser} with the given ID is
     *     found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UnconvUser> deleteUnconvUser(@PathVariable UUID id) {
        return unconvUserService
                .findUnconvUserById(id)
                .map(
                        unconvUser -> {
                            unconvUserService.deleteUnconvUserById(id);
                            return ResponseEntity.ok(unconvUser);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
