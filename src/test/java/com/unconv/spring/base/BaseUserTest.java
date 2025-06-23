package com.unconv.spring.base;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class BaseUserTest {

    private final BaseUser baseUser = new BaseUser();

    @Test
    void testGetAuthoritiesReturnsEmptyList() {
        Collection<? extends GrantedAuthority> authorities = baseUser.getAuthorities();
        assertNotNull(authorities, "Authorities should not be null");
        assertTrue(authorities.isEmpty(), "Authorities should be empty");
    }
}
