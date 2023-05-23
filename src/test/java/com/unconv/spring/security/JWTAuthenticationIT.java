package com.unconv.spring.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.security.filter.JWTUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JWTAuthenticationIT {

    @Autowired private MockMvc mockMvc;

    @Autowired private JWTUtil jwtUtil;

    @Test
    public void testAuthorizedRequest() throws Exception {
        // Generate a valid JWT token
        String token = jwtUtil.generateToken("Test User");

        // Send a request with the token in the Authorization header
        mockMvc.perform(get("/EnvironmentalReading").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        //            .andExpect(content().string("Success")); // Assert the expected response
    }

    @Test
    public void testUnauthorizedRequest() throws Exception {
        // Generate an invalid JWT token
        String token = "RANDOM_STRING";

        // Send a request with the token in the Authorization header
        mockMvc.perform(get("/EnvironmentalReading").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized")); // Assert the expected response
    }
}
