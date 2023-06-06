package com.unconv.spring.web.rest;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.dto.UnconvRoleDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvRoleService;
import com.unconv.spring.utils.AppConstants;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public PagedResult<UnconvRole> getAllUnconvRoles(
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
        return unconvRoleService.findAllUnconvRoles(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnconvRole> getUnconvRoleById(@PathVariable UUID id) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnconvRole createUnconvRole(@RequestBody @Validated UnconvRoleDTO unconvRoleDTO) {
        return unconvRoleService.saveUnconvRole(modelMapper.map(unconvRoleDTO, UnconvRole.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnconvRole> updateUnconvRole(
            @PathVariable UUID id, @RequestBody @Valid UnconvRoleDTO unconvRoleDTO) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(
                        unconvRoleObj -> {
                            unconvRoleDTO.setId(id);
                            return ResponseEntity.ok(
                                    unconvRoleService.saveUnconvRole(
                                            modelMapper.map(unconvRoleDTO, UnconvRole.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
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
