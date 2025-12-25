package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_TOKEN_GEN_FAILED;
import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_TOKEN_GEN_SUCCESS;
import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_PREFIX;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.matchers.SensorAuthTokenMatcher.validSensorAuthToken;
import static com.unconv.spring.utils.AccessTokenGenerator.generateAccessToken;
import static com.unconv.spring.utils.SaltedSuffixGenerator.generateSaltedSuffix;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorAuthToken;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorAuthTokenDTO;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.SensorAuthTokenService;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.web.rest.SensorAuthTokenController;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
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

@WebMvcTest(controllers = SensorAuthTokenController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/SensorAuthToken")
@Import(MethodSecurityConfig.class)
class SensorAuthTokenControllerTest extends AbstractControllerTest {
    @MockBean private SensorAuthTokenService sensorAuthTokenService;

    @MockBean private SensorSystemService sensorSystemService;

    private List<SensorAuthToken> sensorAuthTokenList;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final UnconvUser mUnconvUser =
            new UnconvUser(
                    UUID.randomUUID(), "NewUnconvUser", "newuser@email.com", "1StrongPas$word");

    private final SensorSystem mSensorSystem =
            SensorSystem.builder()
                    .id(UUID.randomUUID())
                    .sensorName("Workspace sensor system")
                    .description("Monitors temperature and humidity for personal workspace")
                    .deleted(false)
                    .sensorStatus(SensorStatus.ACTIVE)
                    .sensorLocation(sensorLocation)
                    .unconvUser(mUnconvUser)
                    .humidityThreshold(new HumidityThreshold(UUID.randomUUID(), 75, 23))
                    .temperatureThreshold(new TemperatureThreshold(UUID.randomUUID(), 100, 0))
                    .createdDate(OffsetDateTime.now().minusDays(new Random().nextLong(365)))
                    .updatedDate(OffsetDateTime.now().minusHours(new Random().nextLong(24)))
                    .build();

    private static final int DEFAULT_PAGE_SIZE = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    private static int totalPages;

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
                        .size(15)
                        .ignore(field(SensorAuthToken::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());

        totalPages = (int) Math.ceil((double) sensorAuthTokenList.size() / DEFAULT_PAGE_SIZE);
    }

    @Test
    void shouldFetchAllSensorAuthTokens() throws Exception {
        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE, sort);

        int dataSize = sensorAuthTokenList.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE, dataSize);
        List<SensorAuthToken> pagedReadings = sensorAuthTokenList.subList(start, end);

        Page<SensorAuthToken> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);

        PagedResult<SensorAuthToken> sensorAuthTokenPagedResult = new PagedResult<>(page);
        given(
                        sensorAuthTokenService.findAllSensorAuthTokens(
                                pageNo, DEFAULT_PAGE_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION))
                .willReturn(sensorAuthTokenPagedResult);

        this.mockMvc
                .perform(get("/SensorAuthToken"))
                .andDo(document("shouldFetchAllSensorAuthTokens", preprocessResponse(prettyPrint)))
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
    void shouldFindSensorAuthTokenById() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthTokenDTO sensorAuthToken =
                new SensorAuthTokenDTO(
                        sensorAuthTokenId,
                        TOKEN_PREFIX + "*".repeat(19) + generateSaltedSuffix(),
                        OffsetDateTime.now().plusDays(30),
                        mSensorSystem);
        given(sensorAuthTokenService.findSensorAuthTokenDTOById(sensorAuthTokenId))
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
                        .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
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
        given(sensorAuthTokenService.generateSensorAuthToken(any(SensorSystem.class), isNull()))
                .willAnswer(
                        invocation ->
                                new SensorAuthTokenDTO(
                                        UUID.randomUUID(),
                                        generateAccessToken() + generateSaltedSuffix(),
                                        OffsetDateTime.now().plusDays(10),
                                        invocation.getArgument(0)));

        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        null,
                        TOKEN_PREFIX + "*".repeat(19) + generateSaltedSuffix(),
                        OffsetDateTime.now().plusDays(30),
                        mSensorSystem);

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(false)))
                .andReturn();
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
                                "shouldReturn400WhenCreateNewSensorAuthTokenWithNullValues",
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
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("authToken")))
                .andExpect(jsonPath("$.violations[0].message", is("Auth token cannot be empty")))
                .andExpect(jsonPath("$.violations[1].field", is("expiry")))
                .andExpect(jsonPath("$.violations[1].message", is("Expiry cannot be empty")))
                .andExpect(jsonPath("$.violations[2].field", is("sensorSystem")))
                .andExpect(jsonPath("$.violations[2].message", is("Sensor system cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthToken sensorAuthToken =
                new SensorAuthToken(
                        sensorAuthTokenId,
                        generateAccessToken(),
                        OffsetDateTime.now().plusDays(30),
                        mSensorSystem);
        given(sensorAuthTokenService.findSensorAuthTokenById(sensorAuthTokenId))
                .willReturn(Optional.of(sensorAuthToken));
        given(
                        sensorAuthTokenService.generateSensorAuthToken(
                                any(SensorSystem.class), any(UUID.class)))
                .willAnswer(
                        invocation ->
                                new SensorAuthTokenDTO(
                                        UUID.randomUUID(),
                                        generateAccessToken() + generateSaltedSuffix(),
                                        OffsetDateTime.now().plusDays(10),
                                        invocation.getArgument(0)));

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
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(false)))
                .andReturn();
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
                        OffsetDateTime.now().plusDays(30),
                        mSensorSystem);

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
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorAuthToken() throws Exception {
        UUID sensorAuthTokenId = UUID.randomUUID();
        SensorAuthTokenDTO sensorAuthToken =
                new SensorAuthTokenDTO(
                        sensorAuthTokenId,
                        TOKEN_PREFIX + "*".repeat(19) + generateSaltedSuffix(),
                        OffsetDateTime.now().plusDays(30),
                        mSensorSystem);
        given(sensorAuthTokenService.findSensorAuthTokenDTOById(sensorAuthTokenId))
                .willReturn((Optional.of(sensorAuthToken)));
        //
        // doNothing().when(sensorAuthTokenService).deleteSensorAuthTokenById(sensorAuthToken.getId());
        //
        this.mockMvc
                .perform(delete("/SensorAuthToken/{id}", sensorAuthToken.getId()).with(csrf()))
                .andDo(document("shouldDeleteSensorAuthToken", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
                .andReturn();
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

    @Test
    void shouldGenerateAndReturnNewSensorAuthTokenForASensorSystem() throws Exception {

        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");

        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Test sensor", null, unconvUser);

        SensorAuthTokenDTO sensorAuthTokenDTO =
                new SensorAuthTokenDTO(
                        UUID.randomUUID(),
                        generateAccessToken() + generateSaltedSuffix(),
                        OffsetDateTime.now().plusDays(60),
                        sensorSystem);

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.of(sensorSystem));
        given(sensorSystemService.isActiveSensorSystem(sensorSystem)).willReturn(true);
        given(sensorAuthTokenService.generateSensorAuthToken(sensorSystem, null))
                .willReturn(sensorAuthTokenDTO);

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldGenerateAndReturnNewSensorAuthTokenForASensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is(SENS_AUTH_TOKEN_GEN_SUCCESS)))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.authToken", validSensorAuthToken(false)))
                .andExpect(
                        jsonPath("$.entity.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenRequestingTokenForANonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldReturn404WhenRequestingTokenForANonExistingSensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturn400WhenRequestingTokenForAInactiveSensorSystem() throws Exception {

        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");

        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Test sensor", null, unconvUser);
        sensorSystem.setDeleted(true);
        sensorSystem.setSensorStatus(SensorStatus.INACTIVE);

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.of(sensorSystem));

        this.mockMvc
                .perform(
                        get(
                                        "/SensorAuthToken/GenerateToken/SensorSystem/{sensorSystemId}",
                                        sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldReturn400WhenRequestingTokenForAInactiveSensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(SENS_AUTH_TOKEN_GEN_FAILED)))
                .andExpect(jsonPath("$.entity", nullValue()))
                .andReturn();
    }

    @Test
    void shouldReturnSensorTokenInfoForAValidSensorSystemWithSensorAuthToken() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");

        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Test sensor", null, unconvUser);

        SensorAuthTokenDTO sensorAuthTokenDTO =
                new SensorAuthTokenDTO(
                        UUID.randomUUID(),
                        TOKEN_PREFIX + "*".repeat(19) + generateSaltedSuffix(),
                        OffsetDateTime.now().plusDays(60),
                        sensorSystem);

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.of(sensorSystem));
        given(sensorAuthTokenService.getSensorAuthTokenInfo(sensorSystem))
                .willReturn(sensorAuthTokenDTO);

        this.mockMvc
                .perform(
                        get("/SensorAuthToken/SensorSystem/{sensorSystemId}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldReturnSensorTokenInfoForAValidSensorSystemWithSensorAuthToken",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", is(sensorAuthTokenDTO.getId().toString())))
                .andExpect(jsonPath("$.authToken", validSensorAuthToken(true)))
                .andExpect(jsonPath("$.expiry", notNullValue(OffsetDateTime.class)))
                .andExpect(jsonPath("$.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldReturnSensorTokenInfoForAValidSensorSystemWithoutSensorAuthToken() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");

        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Test sensor", null, unconvUser);

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.of(sensorSystem));
        given(sensorAuthTokenService.getSensorAuthTokenInfo(sensorSystem)).willReturn(null);

        this.mockMvc
                .perform(
                        get("/SensorAuthToken/SensorSystem/{sensorSystemId}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldReturnSensorTokenInfoForAValidSensorSystemWithoutSensorAuthToken",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id", nullValue()))
                .andExpect(jsonPath("$.authToken", nullValue()))
                .andExpect(jsonPath("$.expiry", nullValue()))
                .andExpect(jsonPath("$.sensorSystem.id", is(sensorSystem.getId().toString())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenRequestingTokenInfoForANonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get("/SensorAuthToken/SensorSystem/{sensorSystemId}", sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "shouldReturn404WhenRequestingTokenInfoForANonExistingSensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
