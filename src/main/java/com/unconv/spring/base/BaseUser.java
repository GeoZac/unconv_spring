package com.unconv.spring.base;

import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A base implementation of {@link UserDetails} that provides default behavior.
 *
 * <p>This class can be extended to create custom user representations in Spring Security. By
 * default, all security flags are set to {@code false}, and no authorities, username, or password
 * are defined.
 */
public class BaseUser implements UserDetails {

    /**
     * Returns the authorities granted to the user. By default, returns {@code null}.
     *
     * @return the authorities, or {@code null} if none are defined
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    /**
     * Returns the password used to authenticate the user. By default, returns {@code null}.
     *
     * @return the password, or {@code null} if not defined
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Returns the username used to authenticate the user. By default, returns {@code null}.
     *
     * @return the username, or {@code null} if not defined
     */
    @Override
    public String getUsername() {
        return null;
    }

    /**
     * Indicates whether the user's account has expired. By default, returns {@code false}.
     *
     * @return {@code false}, indicating the account is expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    /**
     * Indicates whether the user is locked or unlocked. By default, returns {@code false}.
     *
     * @return {@code false}, indicating the account is locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    /**
     * Indicates whether the user's credentials (password) have expired. By default, returns {@code
     * false}.
     *
     * @return {@code false}, indicating the credentials are expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    /**
     * Indicates whether the user is enabled or disabled. By default, returns {@code false}.
     *
     * @return {@code false}, indicating the user is disabled
     */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
