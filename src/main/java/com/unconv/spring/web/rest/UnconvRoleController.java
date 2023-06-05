package com.unconv.spring.web.rest;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.service.UnconvRoleService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/UnconvRole")
@Slf4j
public class UnconvRoleController {

    private final UnconvRoleService unconvRoleService;

    @Autowired
    public UnconvRoleController(UnconvRoleService unconvRoleService) {
        this.unconvRoleService = unconvRoleService;
    }

    @GetMapping
    public List<UnconvRole> getAllUnconvRoles() {
        return unconvRoleService.findAllUnconvRoles();
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
    public UnconvRole createUnconvRole(@RequestBody @Validated UnconvRole unconvRole) {
        return unconvRoleService.saveUnconvRole(unconvRole);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnconvRole> updateUnconvRole(
            @PathVariable UUID id, @RequestBody UnconvRole unconvRole) {
        return unconvRoleService
                .findUnconvRoleById(id)
                .map(
                        unconvRoleObj -> {
                            unconvRole.setId(id);
                            return ResponseEntity.ok(unconvRoleService.saveUnconvRole(unconvRole));
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
