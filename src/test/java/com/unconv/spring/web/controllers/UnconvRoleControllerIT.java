package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.enums.DefaultUserRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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

    private static final int DEFAULT_PAGE_SIZE_INT = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvRole")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        List<UnconvRole> existingRoles = unconvRoleRepository.findAll();
        int actualCount = existingRoles.size();

        assertEquals(
                defaultUserRoleCount,
                actualCount,
                () ->
                        "Expected "
                                + defaultUserRoleCount
                                + " default roles, but found "
                                + actualCount
                                + ".\nExisting roles:\n"
                                + existingRoles.stream()
                                        .map(
                                                role ->
                                                        String.format(
                                                                "[Name: %s, Origin: %s, Reason: %s]",
                                                                role.getName(),
                                                                role.getCreatedBy(),
                                                                role.getCreatedReason()))
                                        .collect(Collectors.joining(",\n")));

        unconvRoleList =
                Instancio.ofList(UnconvRole.class)
                        .size(30)
                        .ignore(field(UnconvRole::getId))
                        .supply(field(UnconvRole::getCreatedReason), testInfo::getDisplayName)
                        .supply(
                                field(UnconvRole::getCreatedBy),
                                () ->
                                        testInfo.getTestClass()
                                                .map(Class::getSimpleName)
                                                .orElse("UnknownTestClass"))
                        .create();
        unconvRoleList = unconvRoleRepository.saveAll(unconvRoleList);
    }

    // TODO Add test with USER access
    @Test
    void shouldFetchAllUnconvRolesInAscendingOrder() throws Exception {

        totalPages =
                (int)
                        Math.ceil(
                                (double) (unconvRoleList.size() + defaultUserRoleCount)
                                        / DEFAULT_PAGE_SIZE_INT);

        this.mockMvc
                .perform(
                        get("/UnconvRole")
                                .param("sortDir", "asc")
                                .with(user("username").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE_INT)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(unconvRoleList.size() < DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasNext", is(unconvRoleList.size() > DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    // TODO Add test with USER access
    @Test
    void shouldFetchAllUnconvRolesInDescendingOrder() throws Exception {

        totalPages =
                (int)
                        Math.ceil(
                                (double) (unconvRoleList.size() + defaultUserRoleCount)
                                        / DEFAULT_PAGE_SIZE_INT);

        this.mockMvc
                .perform(
                        get("/UnconvRole")
                                .param("sortDir", "desc")
                                .with(user("username").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE_INT)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(unconvRoleList.size() + defaultUserRoleCount)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(unconvRoleList.size() < DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasNext", is(unconvRoleList.size() > DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    // TODO Add test with USER access
    @Test
    void shouldFindUnconvRoleById() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);
        UUID unconvRoleId = unconvRole.getId();

        this.mockMvc
                .perform(
                        get("/UnconvRole/{id}", unconvRoleId)
                                .with(user("username").roles("TENANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    // TODO Add test with USER access
    @Test
    void shouldCreateNewUnconvRole() throws Exception {
        UnconvRole unconvRole = UnconvRole.create(null, "New UnconvRole", this.getClass());
        this.mockMvc
                .perform(
                        post("/UnconvRole")
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn400WhenCreateNewUnconvRoleWithNullValues() throws Exception {
        UnconvRole unconvRole = new UnconvRole();

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

    // TODO Add test with USER access
    @Test
    void shouldUpdateUnconvRole() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);
        unconvRole.setName("Updated UnconvRole");

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    // TODO Add test with USER access
    @Test
    void shouldDeleteUnconvRole() throws Exception {
        UnconvRole unconvRole = unconvRoleList.get(0);

        this.mockMvc
                .perform(
                        delete("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .with(user("username").roles("MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvRole.getId().toString())))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    // TODO Add test with USER access
    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        this.mockMvc
                .perform(
                        get("/UnconvRole/{id}", unconvRoleId)
                                .with(user("username").roles("TENANT")))
                .andExpect(status().isNotFound());
    }

    // TODO Add test with USER access
    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = unconvRoleList.get(1);

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isNotFound());
    }

    // TODO Add test with USER access
    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        this.mockMvc
                .perform(
                        delete("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .with(user("username").roles("MANAGER")))
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
