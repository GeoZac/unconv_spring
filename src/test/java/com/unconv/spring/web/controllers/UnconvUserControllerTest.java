package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.MessageConstants.USER_CREATE_SUCCESS;
import static com.unconv.spring.utils.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvUserService;
import com.unconv.spring.web.rest.UnconvUserController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = UnconvUserController.class)
@ActiveProfiles(PROFILE_TEST)
class UnconvUserControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    @Autowired private ObjectMapper objectMapper;

    private List<UnconvUser> unconvUserList;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvUser")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.unconvUserList = new ArrayList<>();
        this.unconvUserList =
                Instancio.ofList(UnconvUser.class)
                        .size(7)
                        .ignore(field(UnconvUser::getId))
                        .create();

        totalPages = (int) Math.ceil((double) this.unconvUserList.size() / defaultPageSize);

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllUnconvUsers() throws Exception {
        Page<UnconvUser> page = new PageImpl<>(unconvUserList);
        PagedResult<UnconvUser> unconvUserPagedResult = new PagedResult<>(page);
        given(unconvUserService.findAllUnconvUsers(0, 10, "id", "asc"))
                .willReturn(unconvUserPagedResult);

        this.mockMvc
                .perform(get("/UnconvUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(unconvUserList.size())))
                .andExpect(jsonPath("$.totalElements", is(unconvUserList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(unconvUserList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(unconvUserList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindUnconvUserById() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(unconvUserId, "text 1", "some@email.com", "password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));

        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewUnconvUser() throws Exception {

        UnconvUserDTO unconvUserDTO =
                new UnconvUserDTO(UUID.randomUUID(), "some text", "email@provider.com", "secret");
        MessageResponse<UnconvUserDTO> messageResponse =
                new MessageResponse<>(USER_CREATE_SUCCESS, unconvUserDTO);

        ResponseEntity<MessageResponse<UnconvUserDTO>> responseEntity =
                new ResponseEntity<>(messageResponse, HttpStatus.CREATED);
        given(
                        unconvUserService.checkUsernameUniquenessAndSaveUnconvUser(
                                any(UnconvUser.class), any(String.class)))
                .willReturn(responseEntity);

        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())));
    }

    @Test
    void shouldReturn400WhenCreateNewUnconvUserWithoutText() throws Exception {
        UnconvUser unconvUser = new UnconvUser(null, null, null, null);

        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUser)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(jsonPath("$.violations[0].message", is("E-mail cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Updated text", "newemail@provider.com", "new_password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));
        given(unconvUserService.saveUnconvUser(any(UnconvUser.class), any(String.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());
        UnconvUserDTO unconvUserDTO =
                new UnconvUserDTO(
                        unconvUserId, "Non existant user", "nonexistant@email.com", "password");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(unconvUserId, "Some text", "valid@email.com", "password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));
        doNothing().when(unconvUserService).deleteUnconvUserById(unconvUser.getId());

        this.mockMvc
                .perform(delete("/UnconvUser/{id}", unconvUser.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
