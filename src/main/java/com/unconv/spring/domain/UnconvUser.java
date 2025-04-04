package com.unconv.spring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.unconv.spring.base.BaseUser;
import com.unconv.spring.utils.UnconvAuthorityDeserializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Table(name = "unconv_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnconvUser extends BaseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @Column(nullable = false)
    @NotEmpty(message = "E-mail cannot be empty")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Password cannot be empty")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @Size(min = 1, max = 3)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "unconv_user_id"),
            inverseJoinColumns = @JoinColumn(name = "unconv_role_id"))
    @JsonIgnore
    private Set<UnconvRole> unconvRoles = new HashSet<>();

    @Override
    @JsonDeserialize(using = UnconvAuthorityDeserializer.class)
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (UnconvRole unconvRole : unconvRoles) {
            authorities.add(new SimpleGrantedAuthority(unconvRole.getName()));
        }
        return authorities;
    }

    /**
     * Constructs a new instance of {@link UnconvUser} with the specified attributes.
     *
     * @param id the unique identifier for the user
     * @param username the username of the user
     * @param email the email address of the user
     * @param password the password of the user
     */
    public UnconvUser(UUID id, String username, String email, String password) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
