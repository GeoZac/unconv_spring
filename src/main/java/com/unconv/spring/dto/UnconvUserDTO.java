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
    private UUID id;

    @NotEmpty(message = "Username cannot be empty")
    @ValidUsername
    private String username;

    @NotEmpty(message = "E-mail cannot be empty")
    @Email()
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @ValidPassword
    private String password;
}
