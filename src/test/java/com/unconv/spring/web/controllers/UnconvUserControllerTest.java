package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.MessageConstants.USER_CREATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_NAME_IN_USE;
import static com.unconv.spring.consts.MessageConstants.USER_PROVIDE_PASSWORD;
import static com.unconv.spring.consts.MessageConstants.USER_UPDATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_WRONG_PASSWORD;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
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

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvUserService;
import com.unconv.spring.utils.UnconvAuthorityDeserializer;
import com.unconv.spring.web.rest.UnconvUserController;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
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

@WebMvcTest(controllers = UnconvUserController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/UnconvUser")
class UnconvUserControllerTest extends AbstractControllerTest {
    @MockBean private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    private List<UnconvUser> unconvUserList;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/UnconvUser")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
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

        SimpleModule authDeserializerModule = new SimpleModule();
        authDeserializerModule.addDeserializer(Collection.class, new UnconvAuthorityDeserializer());
        objectMapper.registerModule(authDeserializerModule);
    }

    @Test
    void shouldFetchAllUnconvUsers() throws Exception {
        Page<UnconvUser> page = new PageImpl<>(unconvUserList);
        PagedResult<UnconvUser> unconvUserPagedResult = new PagedResult<>(page);
        given(unconvUserService.findAllUnconvUsers(0, 10, "id", "asc"))
                .willReturn(unconvUserPagedResult);

        this.mockMvc
                .perform(get("/UnconvUser"))
                .andDo(document("shouldFetchAllUnconvUsers", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(unconvUserList.size())))
                .andExpect(jsonPath("$.totalElements", is(unconvUserList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(unconvUserList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(unconvUserList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andReturn();
    }

    @Test
    void shouldFindUnconvUserById() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "NewUnconvUser", "newuser@email.com", "1StrongPas$word");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));

        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andDo(document("shouldFindUnconvUserById", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturnTrueWhenAnUnregisteredUnconvUserIsCheckedIfAvailable() throws Exception {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String randomGeneratedString = RandomStringUtils.random(length, useLetters, useNumbers);

        given(unconvUserService.isUsernameUnique(randomGeneratedString)).willReturn(true);

        this.mockMvc
                .perform(get("/UnconvUser/Username/Available/{username}", randomGeneratedString))
                .andDo(
                        document(
                                "shouldReturnTrueWhenAnUnregisteredUnconvUserIsCheckedIfAvailable",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is("true"), String.class))
                .andExpect(jsonPath("$.username", is(randomGeneratedString), String.class))
                .andReturn();
    }

    @Test
    void shouldReturnFalseWhenRegisteredUnconvUserIsCheckedIfAvailable() throws Exception {
        String existingUserName = unconvUserList.get(0).getUsername();

        given(unconvUserService.isUsernameUnique(existingUserName)).willReturn(false);

        this.mockMvc
                .perform(get("/UnconvUser/Username/Available/{username}", existingUserName))
                .andDo(
                        document(
                                "shouldReturnFalseWhenRegisteredUnconvUserIsCheckedIfAvailable",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is("false"), String.class))
                .andExpect(jsonPath("$.username", is(existingUserName), String.class))
                .andReturn();
    }

    @Test
    void shouldCreateNewUnconvUser() throws Exception {
        UnconvUserDTO unconvUserDTO =
                new UnconvUserDTO(null, "SomeUserName", "email@provider.com", "$ecreT123");

        given(unconvUserService.isUsernameUnique(any(String.class))).willReturn(true);

        given(unconvUserService.createUnconvUser(any(UnconvUserDTO.class)))
                .willAnswer(
                        (invocation -> {
                            UnconvUserDTO unconvUserDTOArg = invocation.getArgument(0);

                            UnconvRole userUnconvRole =
                                    new UnconvRole(UUID.randomUUID(), "ROLE_USER");
                            Set<UnconvRole> unconvRoleSet = new HashSet<>();
                            unconvRoleSet.add(userUnconvRole);

                            unconvUserDTOArg.setId(UUID.randomUUID());
                            unconvUserDTOArg.setUnconvRoles(unconvRoleSet);
                            unconvUserDTOArg.setPassword(null);
                            return unconvUserDTOArg;
                        }));

        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldCreateNewUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is(USER_CREATE_SUCCESS)))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())))
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
    void shouldReturn400WhenCreateNewUnconvUserWithUsernameAlreadyInUse() throws Exception {

        UnconvUserDTO unconvUserDTO =
                new UnconvUserDTO(
                        UUID.randomUUID(), "SomeUserName", "email@provider.com", "$ecreT123");

        UnconvUser unconvUser = modelMapper.map(unconvUserDTO, UnconvUser.class);
        unconvUser.setPassword(null);
        UnconvUserDTO unconvUserDTOWithPasswordObscured =
                modelMapper.map(unconvUser, UnconvUserDTO.class);

        given(unconvUserService.isUsernameUnique(any(String.class))).willReturn(false);

        given(unconvUserService.createUnconvUser(any(UnconvUserDTO.class)))
                .willReturn(unconvUserDTOWithPasswordObscured);

        this.mockMvc
                .perform(
                        post("/UnconvUser")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewUnconvUserWithUsernameAlreadyInUse",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(USER_NAME_IN_USE)))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.password").doesNotExist())
                .andExpect(jsonPath("$.entity.username", is(unconvUserDTO.getUsername())))
                .andReturn();
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
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewUnconvUserWithNullValues",
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
                .andExpect(jsonPath("$.violations", hasSize(5)))
                .andExpect(jsonPath("$.violations[0].field", is("email")))
                .andExpect(jsonPath("$.violations[0].message", is("E-mail cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Updated-username", "newemail@provider.com", "new!1Password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));
        given(unconvUserService.checkPasswordMatch(any(UUID.class), any(String.class)))
                .willReturn(true);
        given(unconvUserService.saveUnconvUser(any(UnconvUser.class), any(String.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));
        given(unconvUserService.updateUnconvUser((any(UnconvUser.class)), any(UnconvUserDTO.class)))
                .willAnswer(
                        (invocation) -> {
                            UnconvUserDTO unconvUserDTO = invocation.getArgument(1);

                            UnconvRole userUnconvRole =
                                    new UnconvRole(UUID.randomUUID(), "ROLE_USER");
                            Set<UnconvRole> unconvRoleSet = new HashSet<>();
                            unconvRoleSet.add(userUnconvRole);

                            unconvUserDTO.setUnconvRoles(unconvRoleSet);
                            unconvUserDTO.setPassword(null);
                            return unconvUserDTO;
                        });

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        unconvUserDTO.setCurrentPassword(unconvUserDTO.getPassword());

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldUpdateUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(USER_UPDATE_SUCCESS)))
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
    void shouldReturn401AndFailToUpdateUnconvUserWhenProvidedPasswordDoNotMatch() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Updated-username", "newemail@provider.com", "new!1Password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));
        given(unconvUserService.checkPasswordMatch(any(UUID.class), any(String.class)))
                .willReturn(false);
        given(unconvUserService.saveUnconvUser(any(UnconvUser.class), any(String.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);
        unconvUserDTO.setCurrentPassword(unconvUserDTO.getPassword());

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(Charset.defaultCharset())
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldReturn401AndFailToUpdateUnconvUserWhenProvidedPasswordDoNotMatch",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(USER_WRONG_PASSWORD)))
                .andExpect(jsonPath("$.entity.password", is(unconvUser.getPassword())))
                .andExpect(jsonPath("$.entity.username", is(unconvUser.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn400FailToUpdateUnconvUserWhenCurrentPasswordIsNotProvided() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        unconvUserId, "Updated-username", "newemail@provider.com", "new!1Password");
        given(unconvUserService.findUnconvUserById(unconvUserId))
                .willReturn(Optional.of(unconvUser));
        given(unconvUserService.checkPasswordMatch(any(UUID.class), any(String.class)))
                .willReturn(false);
        given(unconvUserService.saveUnconvUser(any(UnconvUser.class), any(String.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);

        assert unconvUserDTO.getCurrentPassword() == null;

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUser.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(Charset.defaultCharset())
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldReturn400FailToUpdateUnconvUserWhenCurrentPasswordIsNotProvided",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(USER_PROVIDE_PASSWORD)))
                .andExpect(jsonPath("$.entity.password", is(unconvUser.getPassword())))
                .andExpect(jsonPath("$.entity.username", is(unconvUser.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());
        UnconvUserDTO unconvUserDTO =
                new UnconvUserDTO(
                        unconvUserId, "NonExistentUser", "nonexistant@email.com", "404Pas$word");

        this.mockMvc
                .perform(
                        put("/UnconvUser/{id}", unconvUserId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(unconvUserDTO)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingUnconvUser",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
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
                .andDo(document("shouldDeleteUnconvUser", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.username", is(unconvUser.getUsername())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/UnconvUser/{id}", unconvUserId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
