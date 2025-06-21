package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
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
import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.UnconvRoleService;
import com.unconv.spring.web.rest.UnconvRoleController;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = UnconvRoleController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/UnconvRole")
@Import(MethodSecurityConfig.class)
class UnconvRoleControllerTest extends AbstractControllerTest {
    @MockBean private UnconvRoleService unconvRoleService;

    private List<UnconvRole> unconvRoleList;

    private static final int DEFAULT_PAGE_SIZE = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvRole")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        unconvRoleList = Instancio.ofList(UnconvRole.class).size(30).create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());

        totalPages = (int) Math.ceil((double) unconvRoleList.size() / DEFAULT_PAGE_SIZE);
    }

    @Test
    void shouldFetchAllUnconvRoles() throws Exception {
        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE, sort);

        int dataSize = unconvRoleList.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE, dataSize);
        List<UnconvRole> pagedReadings = unconvRoleList.subList(start, end);

        Page<UnconvRole> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);

        PagedResult<UnconvRole> unconvRolePagedResult = new PagedResult<>(page);
        given(
                        unconvRoleService.findAllUnconvRoles(
                                pageNo, DEFAULT_PAGE_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION))
                .willReturn(unconvRolePagedResult);

        this.mockMvc
                .perform(get("/UnconvRole").with(user("username").roles("TENANT")))
                .andDo(document("shouldFetchAllUnconvRoles", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldReturn403WhenFetchingAllUnconvRolesAsUnconvAdmin() throws Exception {
        this.mockMvc
                .perform(get("/UnconvRole").with(user("username").roles("ADMIN")))
                .andDo(
                        document(
                                "shouldReturn403WhenFetchingAllUnconvRolesAsUnconvAdmin",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldReturn403WhenFetchingAllUnconvRolesAsUnconvUser() throws Exception {
        this.mockMvc
                .perform(get("/UnconvRole"))
                .andDo(
                        document(
                                "shouldReturn403WhenFetchingAllUnconvRolesAsUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldFindUnconvRoleById() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = UnconvRole.create(unconvRoleId, "ROLE_X", this.getClass());
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));

        this.mockMvc
                .perform(
                        get("/UnconvRole/{id}", unconvRoleId)
                                .with(user("username").roles("TENANT")))
                .andDo(document("shouldFindUnconvRoleById", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn403WhenFindingUnconvRoleByIdAsUnconvAdmin() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();

        this.mockMvc
                .perform(
                        get("/UnconvRole/{id}", unconvRoleId).with(user("username").roles("ADMIN")))
                .andDo(
                        document(
                                "shouldReturn403WhenFindingUnconvRoleByIdAsUnconvAdmin",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldReturn403WhenFindingUnconvRoleByIdAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();

        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andDo(
                        document(
                                "shouldReturn403WhenFindingUnconvRoleByIdAsUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get("/UnconvRole/{id}", unconvRoleId)
                                .with(user("username").roles("TENANT")))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingUnconvRole",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenFetchingNonExistingUnconvRoleAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/UnconvRole/{id}", unconvRoleId))
                .andDo(
                        document(
                                "shouldReturn403WhenFetchingNonExistingUnconvRoleAsUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
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

        UnconvRole unconvRole = UnconvRole.create(null, "ROLE_NEW", this.getClass());
        this.mockMvc
                .perform(
                        post("/UnconvRole")
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldCreateNewUnconvRole",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn403WhenCreatingNewUnconvRoleAsUnconvUser() throws Exception {
        UnconvRole unconvRole = UnconvRole.create(null, "ROLE_NEW", this.getClass());
        this.mockMvc
                .perform(
                        post("/UnconvRole")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldReturn403WhenCreatingNewUnconvRoleAsUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
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
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewUnconvRoleWithNullValues",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
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
        UnconvRole unconvRole =
                UnconvRole.create(unconvRoleId, "Updated ROLE name", this.getClass());
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));
        given(unconvRoleService.saveUnconvRole(any(UnconvRole.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldUpdateUnconvRole",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn403WhenUpdatingUnconvRoleAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole =
                UnconvRole.create(unconvRoleId, "Updated ROLE name", this.getClass());
        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldReturn403WhenUpdatingUnconvRoleAsUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());
        UnconvRole unconvRole = UnconvRole.create(unconvRoleId, "Updated text", this.getClass());

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .with(user("username").roles("MANAGER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingUnconvRole",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturn403WhenUpdatingNonExistingUnconvRoleAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());
        UnconvRole unconvRole = UnconvRole.create(unconvRoleId, "Updated text", this.getClass());

        this.mockMvc
                .perform(
                        put("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvRole)))
                .andDo(
                        document(
                                "shouldReturn403WhenUpdatingNonExistingUnconvRoleAsUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldDeleteUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        UnconvRole unconvRole = UnconvRole.create(unconvRoleId, "ROLE_KING", this.getClass());
        given(unconvRoleService.findUnconvRoleById(unconvRoleId))
                .willReturn(Optional.of(unconvRole));
        doNothing().when(unconvRoleService).deleteUnconvRoleById(unconvRole.getId());

        this.mockMvc
                .perform(
                        delete("/UnconvRole/{id}", unconvRole.getId())
                                .with(csrf())
                                .with(user("username").roles("MANAGER")))
                .andDo(document("shouldDeleteUnconvRole", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(unconvRole.getName())));
    }

    @Test
    void shouldReturn403WhenDeletingUnconvRoleAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();

        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRoleId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn403WhenDeletingUnconvRoleAsUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvRole() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        delete("/UnconvRole/{id}", unconvRoleId)
                                .with(csrf())
                                .with(user("username").roles("MANAGER")))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingUnconvRole",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn403WhenDeletingNonExistingUnconvRoleAsUnconvUser() throws Exception {
        UUID unconvRoleId = UUID.randomUUID();
        given(unconvRoleService.findUnconvRoleById(unconvRoleId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/UnconvRole/{id}", unconvRoleId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn403WhenDeletingNonExistingUnconvRoleAsUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
