package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvRoleService;
import com.unconv.spring.web.rest.UnconvRoleController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = UnconvRoleController.class)
@ActiveProfiles(PROFILE_TEST)
class UnconvRoleControllerTest extends AbstractControllerTest {
    @MockBean private UnconvRoleService unconvRoleService;

    private List<UnconvRole> unconvRoleList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvRole")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.unconvRoleList = new ArrayList<>();
        this.unconvRoleList.add(new UnconvRole(null, "text 1"));
        this.unconvRoleList.add(new UnconvRole(null, "text 2"));
        this.unconvRoleList.add(new UnconvRole(null, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllUnconvRoles() throws Exception {
        Page<UnconvRole> page = new PageImpl<>(unconvRoleList);
        PagedResult<UnconvRole> unconvRolePagedResult = new PagedResult<>(page);
        given(unconvRoleService.findAllUnconvRoles(0, 10, "id", "asc"))
                .willReturn(unconvRolePagedResult);

        this.mockMvc
                .perform(get("/UnconvRole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(unconvRoleList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindUnconvRoleById() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = new UnconvRole(unconvRoleId, "text 1");
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));

        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewUnconvRole() throws Exception {
        given(unconvRoleService.saveUnconvRole(any(UnconvRole.class)))
                .willAnswer(
                        (invocation) -> {
                            UnconvRole unconvRole = invocation.getArgument(0);
                            unconvRole.setId(UUID.randomUUID());
                            return unconvRole;
                        });

        UnconvRole unconvRole = new UnconvRole(UUID.randomUUID(), "some text");
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
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = new UnconvRole(unconvRoleId, "Updated text");
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));
        given(unconvRoleService.saveUnconvRole(any(UnconvRole.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());
        UnconvRole unconvRole = new UnconvRole(unconvRoleId, "Updated text");

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = new UnconvRole(unconvRoleId, "Some text");
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));
        doNothing().when(unconvRoleService).deleteUnconvRoleById(unconvRole.getId());

        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRole.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRoleId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
