package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.MessageConstants.USER_NAME_IN_USE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
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
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class UnconvUserControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    private List<UnconvUser> unconvUserList = null;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvUser")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        unconvUserRepository.deleteAllInBatch();

        unconvUserList = new ArrayList<>();
        this.unconvUserList =
                Instancio.ofList(UnconvUser.class)
                        .size(7)
                        .ignore(field(UnconvUser::getId))
                        .supply(field(UnconvUser::getUsername), random -> random.alphanumeric(10))
                        .supply(
                                field(UnconvUser::getEmail),
                                random ->
                                        random.alphanumeric(5)
                                                + "@"
                                                + random.alphanumeric(5)
                                                + "."
                                                + random.alphanumeric(3))
                        .create();

        unconvUserList = unconvUserRepository.saveAll(unconvUserList);
        totalPages = (int) Math.ceil((double) unconvUserList.size() / defaultPageSize);
    }

    @Test
    void shouldFetchAllUnconvUsersInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/UnconvUser").param("sortDir", "asc"))
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
    void shouldFetchAllUnconvUsersInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/UnconvUser").param("sortDir", "desc"))
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
        UnconvUser unconvUser = unconvUserList.get(0);
        UUID unconvUserId = unconvUser.getId();

        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvUser.getId().toString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldCreateNewUnconvUser() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "NewUnconvUser", "newuser@email.com", "1StrongPas$word");

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldCreateNewUnconvUserEvenIfAlreadyExistingPrimaryKeyInRequest() throws Exception {
        UUID alreadyExistingUUID = unconvUserList.get(0).getId();
        UnconvUser unconvUser =
                new UnconvUser(
                        alreadyExistingUUID,
                        "NewUnconvUser",
                        "newuser@email.com",
                        "1StrongPas$word");

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.id", not(alreadyExistingUUID.toString())))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturn400WhenAlreadyRegisterAsUser() throws Exception {
        String rawPassword = "new_password";
        UnconvUser unconvUser = new UnconvUser(null, "New_User", "newuser@gmail.com", rawPassword);
        unconvUserService.saveUnconvUser(unconvUser, rawPassword);

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        unconvUserDTO.setId(null);
        unconvUserDTO.setPassword("Another1Pas$word");

        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(USER_NAME_IN_USE)))
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(jsonPath("$.entity.email", is(unconvUserDTO.getEmail())));
    }

    @Test
    void shouldLoginAsAuthenticatedUserAndReceiveJWToken() throws Exception {
        String rawPassword = "password";
        UnconvUser unconvUser = new UnconvUser(null, "new_user", "testuser@gmail.com", rawPassword);
        UnconvUser savedUnconvUser = unconvUserService.saveUnconvUser(unconvUser, rawPassword);
        assert savedUnconvUser.getId().version() == 4;

        UnconvUser userToLogin = new UnconvUser();
        userToLogin.setUsername(unconvUser.getUsername());
        userToLogin.setPassword(rawPassword);

        UnconvUserDTO unconvUserDTO = modelMapper.map(userToLogin, UnconvUserDTO.class);

        this.mockMvc
                .perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void shouldFailLoginAsUserAndNotReceiveJWToken() throws Exception {

        UnconvUser userToLogin = new UnconvUser();
        userToLogin.setUsername(unconvUserList.get(0).getUsername());
        userToLogin.setPassword("JTIzIdXRoh");

        UnconvUserDTO unconvUserDTO = modelMapper.map(userToLogin, UnconvUserDTO.class);

        this.mockMvc
                .perform(
                        post("/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$", is("User Not Authenticated")));
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
                .andExpect(jsonPath("$.violations", hasSize(5)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(jsonPath("$.violations[0].message", is("E-mail cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewUnconvUserWithInvalidValues() throws Exception {
        UnconvUser unconvUser = new UnconvUser(null, "unconv user", "what_is_this", "let me pass");

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
                .andExpect(jsonPath("$.violations", hasSize(4)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Must be a well-formed email address")))
                .andReturn();
    }

    @Test
    void shouldUpdateUnconvUser() throws Exception {
        UnconvUser unconvUser = unconvUserList.get(0);
        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        unconvUserDTO.setUsername("UpdatedUnconvUser");
        unconvUserDTO.setPassword("UpdatedPas$w0rd");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvUserDTO.getId().toString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUserDTO.getUsername())));
    }

    @Test
    void shouldDeleteUnconvUser() throws Exception {
        UnconvUser unconvUser = unconvUserList.get(0);

        this.mockMvc
                .perform(delete("/UnconvUser/{id}", unconvUser.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvUser.getId().toString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser = unconvUserList.get(1);
        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        unconvUserDTO.setPassword("NewPas$w0rd");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
