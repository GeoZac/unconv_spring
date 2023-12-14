package com.unconv.spring.dto;

import com.unconv.spring.annotation.ValidPassword;
import com.unconv.spring.annotation.ValidUsername;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnconvUserDTO {

    public UnconvUserDTO(UUID id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    private UUID id;

    @NotEmpty(message = "Username cannot be empty")
    @ValidUsername
    private String username;

    @NotEmpty(message = "E-mail cannot be empty")
    @Email(message = "Must be a well-formed email address")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @ValidPassword
    private String password;

    private String currentPassword;
}
