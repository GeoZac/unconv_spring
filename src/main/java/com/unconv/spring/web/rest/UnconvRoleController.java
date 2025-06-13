package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.dto.UnconvRoleDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvRoleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/UnconvRole")
@Slf4j
public class UnconvRoleController {

    @Autowired private UnconvRoleService unconvRoleService;

    @Autowired private ModelMapper modelMapper;

    /**
     * Retrieves a paginated list of UnconvRoles based on pagination parameters.
     *
     * @param pageNo The page number of the results to retrieve.
     * @param pageSize The number of items per page.
     * @param sortBy The field to sort the results by.
     * @param sortDir The sort direction (ASC or DESC).
     * @return A PagedResult containing the list of UnconvRoles for the specified page, sorted as
     *     requested.
     */
    @GetMapping
    @Secured("ROLE_TENANT")
    public PagedResult<UnconvRole> getAllUnconvRoles(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return unconvRoleService.findAllUnconvRoles(pageNo, pageSize, sortBy, sortDir);
    }

    /**
     * Retrieves an UnconvRole by its ID.
     *
     * @param id The ID of the UnconvRole to retrieve.
     * @return ResponseEntity with status 200 (OK) and the retrieved UnconvRole if found, or
     *     ResponseEntity with status 404 (Not Found) if no UnconvRole with the given ID exists.
     */
    @GetMapping("/{id}")
    @Secured("ROLE_TENANT")
    public ResponseEntity<UnconvRole> getUnconvRoleById(@PathVariable UUID id) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new UnconvRole based on the provided UnconvRoleDTO.
     *
     * @param unconvRoleDTO The UnconvRoleDTO containing the data for the new UnconvRole.
     * @return The created UnconvRole.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Secured("ROLE_MANAGER")
    public UnconvRole createUnconvRole(
            @RequestBody @Validated UnconvRoleDTO unconvRoleDTO, Authentication authentication) {
        unconvRoleDTO.setId(null);
        UnconvRole unconvRole = modelMapper.map(unconvRoleDTO, UnconvRole.class);
        unconvRole.setCreatedBy(authentication.getName());
        unconvRole.setCreatedReason(this.getClass().getName());
        return unconvRoleService.saveUnconvRole(unconvRole);
    }

    /**
     * Updates an existing UnconvRole identified by the given ID with the data from the provided
     * UnconvRoleDTO.
     *
     * @param id The ID of the UnconvRole to update.
     * @param unconvRoleDTO The updated data for the UnconvRole.
     * @return ResponseEntity with status 200 (OK) and the updated UnconvRole if found and updated
     *     successfully, or ResponseEntity with status 404 (Not Found) if no UnconvRole with the
     *     given ID exists.
     */
    @PutMapping("/{id}")
    @Secured("ROLE_MANAGER")
    public ResponseEntity<UnconvRole> updateUnconvRole(
            @PathVariable UUID id, @RequestBody @Valid UnconvRoleDTO unconvRoleDTO) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(
                        unconvRoleObj -> {
                            unconvRoleDTO.setId(id);
                            UnconvRole unconvRole =
                                    modelMapper.map(unconvRoleDTO, UnconvRole.class);
                            unconvRole.setCreatedBy(this.getClass().getName());
                            unconvRole.setCreatedReason(this.getClass().getName());
                            return ResponseEntity.ok(unconvRoleService.saveUnconvRole(unconvRole));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes an UnconvRole identified by the given ID.
     *
     * @param id The ID of the UnconvRole to delete.
     * @return ResponseEntity with status 200 (OK) and the deleted UnconvRole if found and deleted
     *     successfully, or ResponseEntity with status 404 (Not Found) if no UnconvRole with the
     *     given ID exists.
     */
    @DeleteMapping("/{id}")
    @Secured("ROLE_MANAGER")
    public ResponseEntity<UnconvRole> deleteUnconvRole(@PathVariable UUID id) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(
                        unconvRole -> {
                            unconvRoleService.deleteUnconvRoleById(id);
                            return ResponseEntity.ok(unconvRole);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
