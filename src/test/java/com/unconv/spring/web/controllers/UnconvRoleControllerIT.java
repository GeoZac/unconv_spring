package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.consts.DefaultUserRole;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class UnconvRoleControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private UnconvRoleRepository unconvRoleRepository;

    private List<UnconvRole> unconvRoleList = null;

    private static final int defaultUserRoleCount = DefaultUserRole.values().length;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvRole")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        assert unconvRoleRepository.findAll().size() == defaultUserRoleCount;

        unconvRoleList = new ArrayList<>();
        unconvRoleList.add(new UnconvRole(null, "First UnconvRole"));
        unconvRoleList.add(new UnconvRole(null, "Second UnconvRole"));
        unconvRoleList.add(new UnconvRole(null, "Third UnconvRole"));
        unconvRoleList = unconvRoleRepository.saveAll(unconvRoleList);
    }

    @Test
    void shouldFetchAllUnconvRolesInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/UnconvRole").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.size()", is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllUnconvRolesInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/UnconvRole").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.size()", is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindUnconvRoleById() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);
        UUID unconvRoleId = unconvRole.getId();

        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldCreateNewUnconvRole() throws Exception {
        UnconvRole unconvRole = new UnconvRole(null, "New UnconvRole");
        this.mockMvc
                .perform(
                        post("/UnconvRole")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewUnconvRoleWithNullValues() throws Exception {
        UnconvRole unconvRole = new UnconvRole(null, null);

        this.mockMvc
                .perform(
                        post("/UnconvRole")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("name")))
                .andExpect(jsonPath("$.violations[0].message", is("Role name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateUnconvRole() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);
        unconvRole.setName("Updated UnconvRole");

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldDeleteUnconvRole() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);

        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRole.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = unconvRoleList.get(1);

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRoleId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        List<UnconvRole> unconvRoles = unconvRoleRepository.findAll();
        for (UnconvRole unconvRole : unconvRoles) {
            if (EnumSet.allOf(DefaultUserRole.class).toString().contains(unconvRole.getName()))
                continue;
            unconvRoleRepository.delete(unconvRole);
        }
    }
}
