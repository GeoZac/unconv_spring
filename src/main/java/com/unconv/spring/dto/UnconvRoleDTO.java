package com.unconv.spring.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data Transfer Object for UnconvRole entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvRoleDTO {

    private UUID id;

    @NotEmpty(message = "Role name cannot be empty")
    private String name;
}
