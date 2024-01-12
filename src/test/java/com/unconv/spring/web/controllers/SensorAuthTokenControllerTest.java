package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.web.rest.SensorAuthTokenController;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = SensorAuthTokenController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/SensorAuthToken")
class SensorAuthTokenControllerTest extends AbstractControllerTest {
    @MockBean private SensorAuthTokenService sensorAuthTokenService;

    private List<SensorAuthToken> sensorAuthTokenList;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final UnconvUser unconvUser =
            new UnconvUser(
                    UUID.randomUUID(), "NewUnconvUser", "newuser@email.com", "1StrongPas$word");

    private final UUID sensorSystemId = UUID.randomUUID();
    private final SensorSystem sensorSystem =
            new SensorSystem(
                    sensorSystemId,
                    "Workspace sensor system",
                    "Monitors temperature and humidity for personal workspace",
                    false,
                    SensorStatus.ACTIVE,
                    sensorLocation,
                    unconvUser,
                    new HumidityThreshold(UUID.randomUUID(), 75, 23),
                    new TemperatureThreshold(UUID.randomUUID(), 100, 0));

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorAuthToken")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        this.sensorAuthTokenList = new ArrayList<>();
        this.sensorAuthTokenList =
                Instancio.ofList(SensorAuthToken.class)
                        .size(5)
                        .ignore(field(SensorAuthToken::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllSensorAuthTokens() throws Exception {
        Page<SensorAuthToken> page = new PageImpl<>(sensorAuthTokenList);
        PagedResult<SensorAuthToken> sensorAuthTokenPagedResult = new PagedResult<>(page);
        given(sensorAuthTokenService.findAllSensorAuthTokens(0, 10, "id", "asc"))
                .willReturn(sensorAuthTokenPagedResult);

        this.mockMvc
                .perform(get("/SensorAuthToken"))
                .andDo(document("shouldFetchAllSensorAuthTokens", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.totalElements", is(sensorAuthTokenList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorAuthTokenById() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        sensorAuthTokenId,
                        RandomStringUtils.random(25),
                        OffsetDateTime.now().plusDays(30));
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.of(sensorAuthToken));

        String responseJson =
                this.mockMvc
                        .perform(get("/SensorAuthToken/{id}", sensorAuthTokenId))
                        .andDo(
                                document(
                                        "shouldFindSensorAuthTokenById",
                                        preprocessResponse(prettyPrint)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(sensorAuthTokenId.toString())))
                        .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Object> fieldValues = JsonPath.read(responseJson, "$..*");
        for (Object fieldValue : fieldValues) {
            assertNotNull(fieldValue, "Field value should not be null");
        }
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/SensorAuthToken/{id}", sensorAuthTokenId))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingSensorAuthToken",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewSensorAuthToken() throws Exception {
        given(sensorAuthTokenService.saveSensorAuthToken(any(SensorAuthToken.class)))
                .willAnswer(
                        (invocation) -> {
                            SensorAuthToken sensorAuthToken = invocation.getArgument(0);
                            sensorAuthToken.setId(UUID.randomUUID());
                            return sensorAuthToken;
                        });

        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        null, RandomStringUtils.random(25), OffsetDateTime.now().plusDays(30));
        this.mockMvc
                .perform(
                        post("/SensorAuthToken")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(Charset.defaultCharset())
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andDo(
                        document(
                                "shouldCreateNewSensorAuthToken",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
    }

    @Test
    void shouldReturn400WhenCreateNewSensorAuthTokenWithNullValues() throws Exception {
        SensorAuthToken sensorAuthToken = new SensorAuthToken();

        this.mockMvc
                .perform(
                        post("/SensorAuthToken")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewSensorAuthTokenWithoutText",
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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("authToken")))
                .andExpect(jsonPath("$.violations[0].message", is("Auth token cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("expiry")))
                .andExpect(jsonPath("$.violations[1].message", is("Expiry cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        sensorAuthTokenId,
                        RandomStringUtils.random(25),
                        OffsetDateTime.now().plusDays(30));
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.of(sensorAuthToken));
        given(sensorAuthTokenService.saveSensorAuthToken(any(SensorAuthToken.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/SensorAuthToken/{id}", sensorAuthToken.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andDo(
                        document(
                                "shouldUpdateSensorAuthToken",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.empty());
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        sensorAuthTokenId,
                        RandomStringUtils.random(25),
                        OffsetDateTime.now().plusDays(30));

        this.mockMvc
                .perform(
                        put("/SensorAuthToken/{id}", sensorAuthTokenId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(Charset.defaultCharset())
                                .content(objectMapper.writeValueAsString(sensorAuthToken)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingSensorAuthToken",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        sensorAuthTokenId,
                        RandomStringUtils.random(25),
                        OffsetDateTime.now().plusDays(30));
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.of(sensorAuthToken));
        //
        // doNothing().when(sensorAuthTokenService).deleteSensorAuthTokenById(sensorAuthToken.getId());
        //
        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthToken.getId()).with(csrf()))
                .andDo(document("shouldDeleteSensorAuthToken", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken", is(sensorAuthToken.getAuthToken())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthTokenId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingSensorAuthToken",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }
}
