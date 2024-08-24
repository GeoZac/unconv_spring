package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.MessageConstants.USER_NAME_IN_USE;
import static com.unconv.spring.consts.MessageConstants.USER_PROVIDE_PASSWORD;
import static com.unconv.spring.consts.MessageConstants.USER_UPDATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_WRONG_PASSWORD;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
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
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.enums.DefaultUserRole;
import com.unconv.spring.persistence.UnconvRoleRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvUserService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired private UnconvRoleRepository unconvRoleRepository;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    private List<UnconvUser> unconvUserList = null;
    private List<UnconvUserDTO> unconvUserDTOList = null;

    Set<UnconvRole> unconvRoleSet = new HashSet<>();

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvUser")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        UnconvRole unconvRole = new UnconvRole(null, "ROLE_USER");
        UnconvRole savedUnconvRole = unconvRoleRepository.save(unconvRole);
        unconvRoleSet.add(savedUnconvRole);

        unconvUserList = new ArrayList<>();
        unconvUserDTOList = new ArrayList<>();
        this.unconvUserList =
                Instancio.ofList(UnconvUser.class)
                        .size(7)
                        .supply(field(UnconvUser::getUnconvRoles), () -> unconvRoleSet)
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

        for (UnconvUser unconvUser : unconvUserList) {
            UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
            UnconvUser savedUnconvUser =
                    unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

            unconvUserDTO.setId(savedUnconvUser.getId());
            unconvUserDTO.setUnconvRoles(unconvRoleSet);
            unconvUserDTOList.add(unconvUserDTO);
        }
        totalPages = (int) Math.ceil((double) unconvUserList.size() / defaultPageSize);
    }

    // TODO Add test with USER access
    @Test
    void shouldFetchAllUnconvUsersInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(
                        get("/UnconvUser")
                                .param("sortDir", "asc")
                                .with(user("username").roles("TENANT")))
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

    // TODO Add test with USER access
    @Test
    void shouldFetchAllUnconvUsersInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(
                        get("/UnconvUser")
                                .param("sortDir", "desc")
                                .with(user("username").roles("TENANT")))
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

    // TODO Add test with USER access
    @Test
    void shouldFindUnconvUserById() throws Exception {
        UnconvUser unconvUser = unconvUserList.get(0);
        UUID unconvUserId = unconvUser.getId();

        this.mockMvc
                .perform(
                        get("/UnconvUser/{id}", unconvUserId.toString())
                                .with(user("username").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvUser.getId().toString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldReturnTrueWhenAnUnregisteredUnconvUserIsCheckedIfAvailable() throws Exception {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String randomGeneratedString = RandomStringUtils.random(length, useLetters, useNumbers);

        this.mockMvc
                .perform(get("/UnconvUser/Username/Available/{username}", randomGeneratedString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is("true"), String.class))
                .andExpect(jsonPath("$.username", is(randomGeneratedString), String.class));
    }

    @Test
    void shouldReturnFalseWhenRegisteredUnconvUserIsCheckedIfAvailable() throws Exception {
        String existingUserName = unconvUserList.get(0).getUsername();

        this.mockMvc
                .perform(get("/UnconvUser/Username/Available/{username}", existingUserName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is("false"), String.class))
                .andExpect(jsonPath("$.username", is(existingUserName), String.class));
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
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", is(unconvUser.getUsername())))
                .andExpect(jsonPath("$.entity.unconvRoles").doesNotExist())
                .andExpect(jsonPath("$.entity.authorities", notNullValue()))
                .andExpect(jsonPath("$.entity.authorities[0]", notNullValue()))
                .andExpect(jsonPath("$.entity.authorities[0].authority", is("ROLE_USER")))
                .andExpect(jsonPath("$.entity.enabled", is(false)))
                .andExpect(jsonPath("$.entity.accountNonLocked", is(false)))
                .andExpect(jsonPath("$.entity.credentialsNonExpired", is(false)))
                .andExpect(jsonPath("$.entity.accountNonExpired", is(false)))
                .andReturn();
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
        unconvUser.setUnconvRoles(unconvRoleSet);
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
        unconvUser.setUnconvRoles(unconvRoleSet);
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
    void shouldFailLoginWithNullUnconvUserAndNotReceiveJWToken() throws Exception {

        UnconvUserDTO unconvUserDTO = new UnconvUserDTO();

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
    void shouldReturn400WhenCreateNewUnconvUserWithNullValues() throws Exception {
        UnconvUser unconvUser = new UnconvUser();

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
        UnconvUserDTO unconvUserDTO = unconvUserDTOList.get(0);
        unconvUserDTO.setUsername("UpdatedUnconvUser");
        unconvUserDTO.setEmail("whodis_newemail@provider.com");
        unconvUserDTO.setCurrentPassword(unconvUserDTO.getPassword());
        unconvUserDTO.setPassword("UpdatedPas$w0rd");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserDTO.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(USER_UPDATE_SUCCESS)))
                .andExpect(jsonPath("$.entity.id", is(unconvUserDTO.getId().toString())))
                .andExpect(jsonPath("$.entity.email", is(unconvUserDTO.getEmail())))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", not(unconvUserDTO.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn401AndFailToUpdateUnconvUserWhenProvidedPasswordDoNotMatch() throws Exception {
        UnconvUserDTO unconvUserDTO = unconvUserDTOList.get(0);
        unconvUserDTO.setUsername("UpdatedUnconvUser");
        unconvUserDTO.setCurrentPassword(
                RandomStringUtils.random(unconvUserDTO.getPassword().length()));
        unconvUserDTO.setPassword("UpdatedPas$w0rd");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserDTO.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(USER_WRONG_PASSWORD)))
                .andExpect(jsonPath("$.entity.password", is(unconvUserDTO.getPassword())))
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn400FailToUpdateUnconvUserWhenCurrentPasswordIsNotProvided() throws Exception {
        UnconvUserDTO unconvUserDTO = unconvUserDTOList.get(0);
        unconvUserDTO.setUsername("UpdatedUnconvUser");
        unconvUserDTO.setCurrentPassword(null);
        unconvUserDTO.setPassword("UpdatedPas$w0rd");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserDTO.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(USER_PROVIDE_PASSWORD)))
                .andExpect(jsonPath("$.entity.password", is(unconvUserDTO.getPassword())))
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())))
                .andReturn();
    }

    // TODO Add test with USER access
    @Test
    void shouldDeleteUnconvUser() throws Exception {
        UnconvUser unconvUser = unconvUserList.get(0);

        this.mockMvc
                .perform(
                        delete("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .with(user("username").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(unconvUser.getId().toString())))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())));
    }

    // TODO Add test with USER access
    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        this.mockMvc
                .perform(
                        get("/UnconvUser/{id}", unconvUserId).with(user("username").roles("ADMIN")))
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

    // TODO Add test with USER access
    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        this.mockMvc
                .perform(
                        delete("/UnconvUser/{id}", unconvUserId)
                                .with(csrf())
                                .with(user("username").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        List<UnconvUser> unconvUsers = unconvUserRepository.findAll();
        for (UnconvUser unconvUser : unconvUsers) {
            Set<UnconvRole> unconvRoleSet = unconvUser.getUnconvRoles();
            unconvUser.getUnconvRoles().removeAll(unconvRoleSet);
            unconvUserRepository.save(unconvUser);
        }
        List<UnconvRole> unconvRoles = unconvRoleRepository.findAll();
        for (UnconvRole unconvRole : unconvRoles) {
            if (EnumSet.allOf(DefaultUserRole.class).toString().contains(unconvRole.getName()))
                continue;
            unconvRoleRepository.delete(unconvRole);
        }
        unconvUserRepository.deleteAllInBatch();
    }
}
