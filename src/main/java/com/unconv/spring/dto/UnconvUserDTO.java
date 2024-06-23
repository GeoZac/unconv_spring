package com.unconv.spring.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.unconv.spring.annotation.ValidPassword;
import com.unconv.spring.annotation.ValidUsername;
import com.unconv.spring.base.BaseUser;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.utils.UnconvAuthorityDeserializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Data Transfer Object for UnconvUser entity */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnconvUserDTO extends BaseUser {

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

    @JsonIgnore private Set<UnconvRole> unconvRoles = new HashSet<>();

    @Override
    @JsonDeserialize(using = UnconvAuthorityDeserializer.class)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (UnconvRole unconvRole : unconvRoles) {
            authorities.add(new SimpleGrantedAuthority(unconvRole.getName()));
        }
        return authorities;
    }
}
