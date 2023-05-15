package com.unconv.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvUserDTO {
    private UUID id;

    @NotEmpty(message = "Username cannot be empty")
    private String username;

    private String email;

    private String password;
}
