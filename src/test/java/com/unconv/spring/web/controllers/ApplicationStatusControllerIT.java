package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class ApplicationStatusControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldFetchAppVersion() throws Exception {
        this.mockMvc
                .perform(get("/public/status/version"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", not("application/json")))
                .andExpect(
                        jsonPath(
                                "$",
                                is(
                                        "0.1.2\nThis endpoint is deprecated and will be removed in future version. Please use /v1/version.")))
                .andReturn();
    }

    @Test
    void shouldFetchAppVersionWithJSONResponse() throws Exception {
        this.mockMvc
                .perform(get("/public/status/v1/version"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andExpect(jsonPath("$.version", is("0.1.2")))
                .andReturn();
    }
}
