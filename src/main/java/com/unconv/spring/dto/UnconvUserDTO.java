package com.unconv.spring.dto;

import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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
    @Size(min = 6, max = 25)
    private String username;

    @NotEmpty(message = "E-mail cannot be empty")
    @Email()
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 6, max = 25)
    private String password;
}
