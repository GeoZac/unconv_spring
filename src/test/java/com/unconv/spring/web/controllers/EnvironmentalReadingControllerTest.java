package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_ER_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_ER_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.enums.SensorStatus;
import com.unconv.spring.model.response.ExtremeReadingsResponse;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.projection.EnvironmentalReadingProjection;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.service.UnconvUserService;
import com.unconv.spring.web.rest.EnvironmentalReadingController;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.instancio.Instancio;
import org.instancio.Model;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = EnvironmentalReadingController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/EnvironmentalReading")
@Import(MethodSecurityConfig.class)
class EnvironmentalReadingControllerTest extends AbstractControllerTest {

    @MockBean private EnvironmentalReadingService environmentalReadingService;

    @MockBean private SensorSystemService sensorSystemService;

    @MockBean private UnconvUserService unconvUserService;

    private List<EnvironmentalReading> environmentalReadingList;

    private final UnconvUser mUnconvUser =
            new UnconvUser(UUID.randomUUID(), "SomeUserName", "email@provider.com", "$ecreT123");

    private final SensorLocation mSensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final SensorSystem mSensorSystem =
            SensorSystem.builder()
                    .id(UUID.randomUUID())
                    .sensorName("Workspace sensor system")
                    .description("Monitors temperature and humidity for personal workspace")
                    .deleted(false)
                    .sensorStatus(SensorStatus.ACTIVE)
                    .sensorLocation(mSensorLocation)
                    .unconvUser(mUnconvUser)
                    .humidityThreshold(new HumidityThreshold(UUID.randomUUID(), 75, 23))
                    .temperatureThreshold(new TemperatureThreshold(UUID.randomUUID(), 100, 0))
                    .createdDate(OffsetDateTime.now().minusDays(new Random().nextLong(365)))
                    .updatedDate(OffsetDateTime.now().minusHours(new Random().nextLong(24)))
                    .build();

    private static final Model<EnvironmentalReading> environemntalReadingModel =
            Instancio.of(EnvironmentalReading.class)
                    .supply(
                            field(EnvironmentalReading::getTemperature),
                            random ->
                                    BigDecimal.valueOf(random.doubleRange(-9999.000, 9999.000))
                                            .setScale(3, RoundingMode.HALF_UP)
                                            .doubleValue())
                    .supply(
                            field(EnvironmentalReading::getHumidity),
                            random ->
                                    BigDecimal.valueOf(random.doubleRange(0, 100))
                                            .setScale(3, RoundingMode.HALF_UP)
                                            .doubleValue())
                    .generate(
                            field(EnvironmentalReading::getTimestamp),
                            gen -> gen.temporal().offsetDateTime().past())
                    .toModel();

    private static final int DEFAULT_PAGE_SIZE_INT = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReading")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        environmentalReadingList =
                Instancio.ofList(EnvironmentalReading.class)
                        .size(15)
                        .ignore(field(EnvironmentalReading::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());

        totalPages =
                (int) Math.ceil((double) environmentalReadingList.size() / DEFAULT_PAGE_SIZE_INT);
    }

    @Test
    void shouldFetchAllEnvironmentalReadings() throws Exception {
        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_ER_SORT_DIRECTION, DEFAULT_ER_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE_INT, sort);

        int dataSize = environmentalReadingList.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE_INT, dataSize);
        List<EnvironmentalReading> pagedReadings = environmentalReadingList.subList(start, end);

        Page<EnvironmentalReading> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);

        PagedResult<EnvironmentalReading> environmentalReadingPagedResult = new PagedResult<>(page);
        given(
                        environmentalReadingService.findAllEnvironmentalReadings(
                                pageNo,
                                DEFAULT_PAGE_SIZE_INT,
                                DEFAULT_ER_SORT_BY,
                                DEFAULT_ER_SORT_DIRECTION))
                .willReturn(environmentalReadingPagedResult);

        this.mockMvc
                .perform(get("/EnvironmentalReading"))
                .andDo(
                        document(
                                "shouldFetchAllEnvironmentalReadings",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsOfSpecificSensorInAscendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(15)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .create();

        int dataSize = environmentalReadingsOfSpecificSensor.size();

        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_ER_SORT_DIRECTION, DEFAULT_ER_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE_INT, sort);

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE_INT, dataSize);
        List<EnvironmentalReading> pagedReadings =
                environmentalReadingsOfSpecificSensor.subList(start, end);

        Page<EnvironmentalReading> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);
        PagedResult<EnvironmentalReading> environmentalReadingPagedResult = new PagedResult<>(page);

        given(
                        environmentalReadingService.findAllEnvironmentalReadingsBySensorSystemId(
                                sensorSystem.getId(),
                                pageNo,
                                DEFAULT_PAGE_SIZE_INT,
                                DEFAULT_ER_SORT_BY,
                                DEFAULT_ER_SORT_DIRECTION))
                .willReturn(environmentalReadingPagedResult);

        this.mockMvc
                .perform(
                        get(
                                "/EnvironmentalReading/SensorSystem/{sensorSystemId}",
                                sensorSystem.getId()))
                .andDo(
                        document(
                                "shouldFetchAllEnvironmentalReadingsOfSpecificSensorInAscendingOrder",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > DEFAULT_PAGE_SIZE_INT)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsOfSpecificSensorWithoutSpecifiedInterval()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(15)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .create();

        given(sensorSystemService.findSensorSystemById(any(UUID.class)))
                .willReturn(Optional.of(sensorSystem));

        given(
                        environmentalReadingService.findBySensorSystemIdAndTimestampBetween(
                                any(UUID.class),
                                any(OffsetDateTime.class),
                                any(OffsetDateTime.class)))
                .willReturn(environmentalReadingsOfSpecificSensor);

        this.mockMvc
                .perform(
                        get(
                                "/EnvironmentalReading/Interval/SensorSystem/{sensorSystemId}",
                                sensorSystem.getId()))
                .andDo(
                        document(
                                "shouldFetchAllEnvironmentalReadingsOfSpecificSensorInAscendingOrder",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void
            shouldFetchAllEnvironmentalReadingsOfSpecificSensorWithoutSpecifiedIntervalWithHourSpecified()
                    throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(15)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .create();

        given(sensorSystemService.findSensorSystemById(any(UUID.class)))
                .willReturn(Optional.of(sensorSystem));

        given(
                        environmentalReadingService.findBySensorSystemIdAndTimestampBetween(
                                any(UUID.class),
                                any(OffsetDateTime.class),
                                any(OffsetDateTime.class)))
                .willReturn(environmentalReadingsOfSpecificSensor);

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/Interval/SensorSystem/{sensorSystemId}",
                                        sensorSystem.getId())
                                .param("hours", "24"))
                .andDo(
                        document(
                                "shouldFetchAllEnvironmentalReadingsOfSpecificSensorInAscendingOrder",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingEnvironmentalReadingsOfSpecificSensorWithoutSpecifiedInterval()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        given(sensorSystemService.findSensorSystemById(any(UUID.class)))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get(
                                "/EnvironmentalReading/Interval/SensorSystem/{sensorSystemId}",
                                sensorSystem.getId()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingEnvironmentalReadingsOfSpecificSensorWithoutSpecifiedInterval",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldFindExtremeEnvironmentalReadingsOfSpecificSensor() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        given(sensorSystemService.findSensorSystemById(any(UUID.class)))
                .willReturn(Optional.of(sensorSystem));

        given(
                        environmentalReadingService.getExtremeReadingsResponseBySensorSystemId(
                                any(UUID.class)))
                .willReturn(
                        new ExtremeReadingsResponse(
                                new MockEnvironmentalReadingProjection(
                                        64.0, 34.0, OffsetDateTime.now()),
                                new MockEnvironmentalReadingProjection(
                                        46.0, 34.0, OffsetDateTime.now()),
                                new MockEnvironmentalReadingProjection(
                                        55.0, 76.0, OffsetDateTime.now()),
                                new MockEnvironmentalReadingProjection(
                                        55.0, 67.0, OffsetDateTime.now())));

        this.mockMvc
                .perform(
                        get(
                                "/EnvironmentalReading/Extreme/SensorSystem/{sensorSystemId}",
                                sensorSystem.getId()))
                .andDo(
                        document(
                                "shouldFindExtremeEnvironmentalReadingsOfSpecificSensor",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.size()", is(4)))
                .andExpect(jsonPath("$.maxTemperature.temperature", notNullValue()))
                .andExpect(jsonPath("$.maxHumidity.humidity", notNullValue()))
                .andExpect(jsonPath("$.minTemperature.temperature", notNullValue()))
                .andExpect(jsonPath("$.minHumidity.humidity", notNullValue()))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingExtremeEnvironmentalReadingsOfNonExistentSensor()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Specific Sensor System", null, unconvUser);

        given(sensorSystemService.findSensorSystemById(any(UUID.class)))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get(
                                "/EnvironmentalReading/Extreme/SensorSystem/{sensorSystemId}",
                                sensorSystem.getId()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingExtremeEnvironmentalReadingsOfNonExistentSensor",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldFindEnvironmentalReadingById() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        environmentalReadingId,
                        13L,
                        75L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 1, 17, 17, 39), ZoneOffset.UTC),
                        mSensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));

        String responseJson =
                this.mockMvc
                        .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                        .andDo(
                                document(
                                        "shouldFindEnvironmentalReadingById",
                                        preprocessResponse(prettyPrint)))
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonPath(
                                        "$.temperature", is(environmentalReading.getTemperature())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Object> fieldValues = JsonPath.read(responseJson, "$..*");
        for (Object fieldValue : fieldValues) {
            assertNotNull(fieldValue, "Field value should not be null");
        }
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindLatestEnvironmentalReadingsForASpecificUnconvUserId() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(unconvUserId, "UnconvUser", "unconvuser@email.com", "password");
        List<EnvironmentalReading> environmentalReadings =
                Instancio.ofList(EnvironmentalReading.class).size(9).create();

        given(unconvUserService.findUnconvUserById(any(UUID.class)))
                .willReturn(Optional.of(unconvUser));

        given(
                        environmentalReadingService.findLatestEnvironmentalReadingsByUnconvUserId(
                                unconvUserId))
                .willReturn(environmentalReadings);

        this.mockMvc
                .perform(
                        get("/EnvironmentalReading/Latest/UnconvUser/{unconvUserId}", unconvUserId))
                .andDo(
                        document(
                                "shouldFindLatestEnvironmentalReadingsForASpecificUnconvUserId",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.size()", is(9)));
    }

    @Test
    void shouldReturn404WhenFindingLatestEnvironmentalReadingsForAUnconvUserIdForRandomUnconvUser()
            throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(any(UUID.class))).willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get("/EnvironmentalReading/Latest/UnconvUser/{unconvUserId}", unconvUserId))
                .andDo(
                        document(
                                "shouldReturn404WhenFindingLatestEnvironmentalReadingsForAUnconvUserIdForRandomUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldCreateNewEnvironmentalReading() throws Exception {

        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null,
                        -3L,
                        53L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 7, 7, 56), ZoneOffset.UTC),
                        mSensorSystem);

        given(sensorSystemService.findSensorSystemById(mSensorSystem.getId()))
                .willReturn(Optional.of(mSensorSystem));
        given(
                        environmentalReadingService
                                .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                        any(EnvironmentalReadingDTO.class),
                                        any(Authentication.class)))
                .willAnswer(
                        invocation -> {
                            EnvironmentalReadingDTO environmentalReadingArg =
                                    invocation.getArgument(0);
                            environmentalReadingArg.setId(UUID.randomUUID());

                            MessageResponse<EnvironmentalReadingDTO>
                                    environmentalReadingDTOMessageResponse =
                                            new MessageResponse<>(
                                                    environmentalReadingArg, ENVT_RECORD_ACCEPTED);

                            return new ResponseEntity<>(
                                    environmentalReadingDTOMessageResponse, HttpStatus.CREATED);
                        });

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andDo(
                        document(
                                "shouldCreateNewEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())));
    }

    @Test
    void shouldReturn404WhenCreatingEnvironmentalReadingWithNonExistingSensorSystem()
            throws Exception {

        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null,
                        -3L,
                        53L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 7, 7, 56), ZoneOffset.UTC),
                        mSensorSystem);

        given(sensorSystemService.findSensorSystemById(mSensorSystem.getId()))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andDo(
                        document(
                                "shouldReturn404WhenCreatingEnvironmentalReadingWithNonExistingSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldCreateNewEnvironmentalReadingWhenUploadingAsBulk() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Sensor system", null, unconvUser);

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(15)
                        .supply(
                                field(EnvironmentalReadingDTO::getTemperature),
                                random ->
                                        BigDecimal.valueOf(random.doubleRange(-9999.000, 9999.000))
                                                .setScale(3, RoundingMode.HALF_UP)
                                                .doubleValue())
                        .supply(
                                field(EnvironmentalReadingDTO::getHumidity),
                                random ->
                                        BigDecimal.valueOf(random.doubleRange(0, 100))
                                                .setScale(3, RoundingMode.HALF_UP)
                                                .doubleValue())
                        .generate(
                                field(EnvironmentalReadingDTO::getTimestamp),
                                gen -> gen.temporal().offsetDateTime().past())
                        .supply(field(EnvironmentalReadingDTO::getSensorSystem), () -> sensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("temperature,humidity,timestamp\n");
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {

            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse =
                "Uploaded the file successfully: test.csv with "
                        + environmentalReadingDTOsOfSpecificSensorForBulkData.size()
                        + " records";

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.of(sensorSystem));

        given(
                        environmentalReadingService
                                .verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
                                        any(SensorSystem.class), any(MultipartFile.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(expectedResponse));

        // Create a MockMultipartFile with the CSV content
        MockMultipartFile csvFile =
                new MockMultipartFile(
                        "file",
                        "test.csv",
                        "text/csv",
                        stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

        this.mockMvc
                .perform(
                        multipart(
                                        "/EnvironmentalReading/Bulk/SensorSystem/{sensorSystemId}",
                                        sensorSystem.getId())
                                .file(csvFile)
                                .with(csrf()))
                .andDo(
                        document(
                                "shouldCreateNewEnvironmentalReadingWhenUploadingAsBulk",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", is(expectedResponse)));
    }

    @Test
    void shouldReturn404WhenUploadingAsBulkWithNonExistingSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Sensor system", null, unconvUser);

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(15)
                        .supply(
                                field(EnvironmentalReadingDTO::getTemperature),
                                random ->
                                        BigDecimal.valueOf(random.doubleRange(-9999.000, 9999.000))
                                                .setScale(3, RoundingMode.HALF_UP)
                                                .doubleValue())
                        .supply(
                                field(EnvironmentalReadingDTO::getHumidity),
                                random ->
                                        BigDecimal.valueOf(random.doubleRange(0, 100))
                                                .setScale(3, RoundingMode.HALF_UP)
                                                .doubleValue())
                        .generate(
                                field(EnvironmentalReadingDTO::getTimestamp),
                                gen -> gen.temporal().offsetDateTime().past())
                        .supply(field(EnvironmentalReadingDTO::getSensorSystem), () -> sensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("temperature,humidity,timestamp\n");
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {

            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse =
                "Uploaded the file successfully: test.csv with "
                        + environmentalReadingDTOsOfSpecificSensorForBulkData.size()
                        + " records";

        given(sensorSystemService.findSensorSystemById(sensorSystem.getId()))
                .willReturn(Optional.empty());

        given(
                        environmentalReadingService
                                .verifyCSVFileAndValidateSensorSystemAndParseEnvironmentalReadings(
                                        any(SensorSystem.class), any(MultipartFile.class)))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(expectedResponse));

        // Create a MockMultipartFile with the CSV content
        MockMultipartFile csvFile =
                new MockMultipartFile(
                        "file",
                        "test.csv",
                        "text/csv",
                        stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

        this.mockMvc
                .perform(
                        multipart(
                                        "/EnvironmentalReading/Bulk/SensorSystem/{sensorSystemId}",
                                        sensorSystem.getId())
                                .file(csvFile)
                                .with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenUploadingAsBulkWithNonExistingSensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithNullValues() throws Exception {
        EnvironmentalReading environmentalReading = new EnvironmentalReading();

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewEnvironmentalReadingWithNullValues",
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
                .andExpect(jsonPath("$.violations[0].field", is("sensorSystem")))
                .andExpect(jsonPath("$.violations[0].message", is(ENVT_VALID_SENSOR_SYSTEM)))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithTimestampInFuture() throws Exception {
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null, -3L, 53L, OffsetDateTime.now().plusDays(2), mSensorSystem);

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewEnvironmentalReadingWithTimestampInFuture",
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
                .andExpect(jsonPath("$.violations[0].field", is("timestamp")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Readings has to be in past or present")))
                .andReturn();
    }

    @Test
    void shouldUpdateEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        environmentalReadingId,
                        3L,
                        5L,
                        OffsetDateTime.of(LocalDateTime.of(2021, 12, 25, 1, 15), ZoneOffset.UTC),
                        mSensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        given(environmentalReadingService.saveEnvironmentalReading(any(EnvironmentalReading.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andDo(
                        document(
                                "shouldUpdateEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        environmentalReadingId,
                        13L,
                        75L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 1, 17, 17, 39), ZoneOffset.UTC),
                        mSensorSystem);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        environmentalReadingId,
                        76L,
                        0L,
                        OffsetDateTime.of(LocalDateTime.of(2021, 11, 12, 13, 57), ZoneOffset.UTC),
                        mSensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        doNothing()
                .when(environmentalReadingService)
                .deleteEnvironmentalReadingById(environmentalReading.getId());

        this.mockMvc
                .perform(delete("/EnvironmentalReading/{id}", environmentalReadingId).with(csrf()))
                .andDo(
                        document(
                                "shouldDeleteEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/EnvironmentalReading/{id}", environmentalReadingId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingEnvironmentalReading",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    public record MockEnvironmentalReadingProjection(
            double temperature, double humidity, OffsetDateTime timestamp)
            implements EnvironmentalReadingProjection {
        @Override
        public double getTemperature() {
            return temperature;
        }

        @Override
        public double getHumidity() {
            return humidity;
        }

        @Override
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }
    }
}
