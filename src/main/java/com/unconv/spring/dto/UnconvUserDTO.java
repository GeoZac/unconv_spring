package com.unconv.spring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unconv.spring.base.BaseUser;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnconvUserDTO extends BaseUser {
    private UUID id;

    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @NotEmpty(message = "E-mail cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}
