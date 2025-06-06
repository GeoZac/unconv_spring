package com.unconv.spring.security;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.security.filter.JWTUtil;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class JWTAuthenticationIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private JWTUtil jwtUtil;

    @Test
    void testAuthorizedRequest() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "Test user", "testuser@email.com", "password");
        // Generate a valid JWT token
        String token = jwtUtil.generateToken(unconvUser);

        // Send a request with the token in the Authorization header
        mockMvc.perform(get("/EnvironmentalReading").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        //            .andExpect(content().string("Success")); // Assert the expected response
    }

    @Test
    void testAuthorizedRequestWithoutToeknPrefix() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "Test user", "testuser@email.com", "password");
        // Generate a valid JWT token
        String token = jwtUtil.generateToken(unconvUser);

        // Send a request with the token in the Authorization header
        mockMvc.perform(get("/EnvironmentalReading").header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthorizedRequest() throws Exception {
        // Generate an invalid JWT token
        String token = "RANDOM_STRING";

        // Send a request with the token in the Authorization header
        mockMvc.perform(get("/EnvironmentalReading").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("Token validation failed")))
                .andExpect(jsonPath("$.title", is("Unauthorized")))
                .andExpect(jsonPath("$.timestamp", CoreMatchers.notNullValue()))
                .andReturn();
    }

    @Test
    void testAuthorisationContext() throws Exception {
        UnconvRole unconvRole =
                new UnconvRole(
                        null,
                        "ROLE_USER",
                        this.getClass().getName(),
                        this.getClass().getName(),
                        new Date());
        Set<UnconvRole> unconvRoleSet = new HashSet<>();
        unconvRoleSet.add(unconvRole);
        UnconvUser unconvUser =
                new UnconvUser(
                        UUID.randomUUID(),
                        "Test user",
                        "testuser@email.com",
                        "password",
                        unconvRoleSet);

        String token = jwtUtil.generateToken(unconvUser);

        mockMvc.perform(get("/UnconvUser/whoAmI").header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", notNullValue()))
                .andExpect(jsonPath("$.roles", notNullValue()))
                .andExpect(jsonPath("$.roles[0]", notNullValue()))
                .andReturn();
    }
}
