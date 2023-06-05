package com.unconv.spring.dto;

import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvRoleDTO {

    private UUID id;

    @NotEmpty(message = "Role name cannot be empty")
    private String name;
}
