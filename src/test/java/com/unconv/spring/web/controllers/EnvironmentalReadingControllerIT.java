package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_FORMAT_ERROR;
import static com.unconv.spring.consts.MessageConstants.ENVT_FILE_REJ_ERR;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_DLTD;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_INAT;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_SENS;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;
import static com.unconv.spring.utils.AppConstants.DEFAULT_PAGE_SIZE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.service.UnconvUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class EnvironmentalReadingControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private EnvironmentalReadingService environmentalReadingService;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private UnconvUserRepository unconvUserRepository;

    private List<EnvironmentalReading> environmentalReadingList = null;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

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
                    .ignore(field(EnvironmentalReading::getId))
                    .toModel();

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReading")
                                        .with(user("UnconvUser").roles("USER")))
                        .apply(springSecurity())
                        .build();

        environmentalReadingRepository.deleteAllInBatch();

        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        environmentalReadingList =
                Instancio.ofList(environemntalReadingModel)
                        .size(15)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                () -> savedSensorSystem)
                        .create();

        totalPages = (int) Math.ceil((double) environmentalReadingList.size() / defaultPageSize);

        environmentalReadingList = environmentalReadingRepository.saveAll(environmentalReadingList);
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(defaultPageSize)))
                .andExpect(jsonPath("$.totalElements", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(
                        jsonPath("$.isLast", is(environmentalReadingList.size() < defaultPageSize)))
                .andExpect(
                        jsonPath(
                                "$.hasNext", is(environmentalReadingList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsOfSpecificSensorInAscendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(null, "Specific Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                () -> savedSensorSystem)
                        .create();

        List<EnvironmentalReading> savedEnvironmentalReadingsOfSpecificSensor =
                environmentalReadingRepository.saveAll(environmentalReadingsOfSpecificSensor);

        int dataSize = savedEnvironmentalReadingsOfSpecificSensor.size();

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(dataSize)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/EnvironmentalReading").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(defaultPageSize)))
                .andExpect(jsonPath("$.totalElements", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(
                        jsonPath("$.isLast", is(environmentalReadingList.size() < defaultPageSize)))
                .andExpect(
                        jsonPath(
                                "$.hasNext", is(environmentalReadingList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllEnvironmentalReadingsOfSpecificSensorInDescendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(null, "Specific Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                () -> savedSensorSystem)
                        .create();

        List<EnvironmentalReading> savedEnvironmentalReadingsOfSpecificSensor =
                environmentalReadingRepository.saveAll(environmentalReadingsOfSpecificSensor);

        int dataSize = savedEnvironmentalReadingsOfSpecificSensor.size();

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(dataSize)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindEnvironmentalReadingById() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);
        UUID environmentalReadingId = environmentalReading.getId();

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldCreateNewEnvironmentalReading() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        3L,
                        56L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 17, 7, 9), ZoneOffset.UTC),
                        savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(
                        jsonPath("$.entity.temperature", is(environmentalReading.getTemperature())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()));
    }

    @Test
    void shouldCreateNewEnvironmentalReadingWithMinimalInfo() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        SensorSystem minimalSensorSystem = new SensorSystem();
        minimalSensorSystem.setId(savedSensorSystem.getId());

        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        3L,
                        56L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 17, 7, 9), ZoneOffset.UTC),
                        minimalSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(
                        jsonPath("$.entity.temperature", is(environmentalReading.getTemperature())))
                .andExpect(
                        jsonPath(
                                "$.entity.sensorSystem.id",
                                is(savedSensorSystem.getId().toString())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()));
    }

    @Test
    void shouldCreateNewEnvironmentalReadingWithoutTimestamp() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(null, 3L, 56L, null, savedSensorSystem);
        ResultActions resultActions =
                this.mockMvc
                        .perform(
                                post("/EnvironmentalReading")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        environmentalReadingDTO)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.entity.id", notNullValue()))
                        .andExpect(
                                jsonPath(
                                        "$.entity.temperature",
                                        is(environmentalReadingDTO.getTemperature())))
                        .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()))
                        .andExpect(jsonPath("$.entity.timestamp", notNullValue()));

        // Get the response body
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();

        // Use Jackson ObjectMapper to parse the response body
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Extract the specific attribute from the JSON node
        String extractedValue = jsonNode.get("entity").get("timestamp").asText();

        OffsetDateTime responseDateTime =
                OffsetDateTime.parse(extractedValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Verify that the parsed datetime has the UTC zone offset
        assertEquals(ZoneOffset.UTC, responseDateTime.getOffset());
    }

    @Test
    void shouldReturn400WhenCreatingNewEnvironmentalReadingWithValuesLowerThanLimits()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        -100000,
                        -5,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 17, 7, 9), ZoneOffset.UTC),
                        savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("humidity")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must be greater than or equal to 0.0")))
                .andExpect(jsonPath("$.violations[1].field", is("temperature")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("must be greater than or equal to -9999.000")));
    }

    @Test
    void shouldReturn400WhenCreatingNewEnvironmentalReadingWithValuesHigherThanLimits()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        100000,
                        105,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 17, 7, 9), ZoneOffset.UTC),
                        savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("humidity")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must be less than or equal to 100.00")))
                .andExpect(jsonPath("$.violations[1].field", is("temperature")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("must be less than or equal to 9999.000")));
    }

    @Test
    void shouldCreateNewEnvironmentalReadingWhenUploadingAsBulk() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(5)
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
                        .supply(
                                field(EnvironmentalReadingDTO::getSensorSystem),
                                () -> savedSensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("temperature,humidity,timestamp\n");
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {
            // stringBuilder.append(environmentalReadingDTO.toCSVString());

            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse =
                "Uploaded the file successfully: test.csv with "
                        + environmentalReadingDTOsOfSpecificSensorForBulkData.size()
                        + " records";

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
                                        savedSensorSystem.getId())
                                .file(csvFile)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", is(expectedResponse)));
    }

    @Test
    void shouldreturn417WhenUploadingNewEnvironmentalReadingsAsBulkWithoutHeader()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(5)
                        .supply(
                                field(EnvironmentalReadingDTO::getSensorSystem),
                                () -> savedSensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {

            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse = String.format(ENVT_FILE_REJ_ERR, "test.csv");

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
                                        savedSensorSystem.getId())
                                .file(csvFile)
                                .with(csrf()))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$", is(expectedResponse)));
    }

    @Test
    void shouldReturn404WhenUploadingNewEnvironmentalReadingsInBulkWithInvalidSensorSystem()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        System.out.println(savedSensorSystem.getId());

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(5)
                        .supply(
                                field(EnvironmentalReadingDTO::getSensorSystem),
                                () -> savedSensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("temperature,humidity,timestamp\n");
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {
            // stringBuilder.append(environmentalReadingDTO.toCSVString());

            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse = ENVT_RECORD_REJ_SENS;

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
                                        UUID.randomUUID())
                                .file(csvFile)
                                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", is(expectedResponse)));
    }

    @Test
    void shouldReturn400WhenUploadingNewEnvironmentalReadingsWithInvalidFile() throws Exception {

        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReadingDTO> environmentalReadingDTOsOfSpecificSensorForBulkData =
                Instancio.ofList(EnvironmentalReadingDTO.class)
                        .size(5)
                        .supply(
                                field(EnvironmentalReadingDTO::getSensorSystem),
                                () -> savedSensorSystem)
                        .ignore(field(EnvironmentalReadingDTO::getId))
                        .create();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("temperature,humidity,timestamp\n");
        for (EnvironmentalReadingDTO environmentalReadingDTO :
                environmentalReadingDTOsOfSpecificSensorForBulkData) {
            stringBuilder.append(environmentalReadingDTO.toCSVString()).append("\n");
        }

        String expectedResponse = ENVT_FILE_FORMAT_ERROR;

        // Create a MockMultipartFile with the CSV content
        MockMultipartFile csvFile =
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        stringBuilder.toString().getBytes(StandardCharsets.UTF_8));

        this.mockMvc
                .perform(
                        multipart(
                                        "/EnvironmentalReading/Bulk/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .file(csvFile)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", is(expectedResponse)));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithoutText() throws Exception {
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(UUID.randomUUID(), 0L, 0L, null, null);

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
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
    void shouldReturn401WhenCreateNewEnvironmentalReadingForUnauthenticatedUser() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Some other user", "someonelse@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null, 3L, 56L, OffsetDateTime.now(ZoneOffset.UTC), savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_REJ_USER)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()))
                .andExpect(jsonPath("$.entity.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn404WhenCreateNewEnvironmentalReadingWithoutValidSensorSystem()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(UUID.randomUUID(), "Sensor system", null, savedUnconvUser);
        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null, 3L, 56L, OffsetDateTime.now(ZoneOffset.UTC), sensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_REJ_SENS)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()))
                .andExpect(jsonPath("$.entity.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingForDeletedSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);

        sensorSystem.setDeleted(true);

        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null, 3L, 56L, OffsetDateTime.now(ZoneOffset.UTC), savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_REJ_DLTD)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()))
                .andExpect(jsonPath("$.entity.timestamp", notNullValue()));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingForInactiveSensorSystem()
            throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor system", null, savedUnconvUser);

        sensorSystem.setSensorStatus(SensorStatus.INACTIVE);

        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        null, 3L, 56L, OffsetDateTime.now(ZoneOffset.UTC), savedSensorSystem);
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_REJ_INAT)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())))
                .andExpect(jsonPath("$.entity.sensorSystem", notNullValue()))
                .andExpect(jsonPath("$.entity.timestamp", notNullValue()));
    }

    @Test
    void shouldUpdateEnvironmentalReading() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);
        environmentalReading.setTemperature(45L);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldDeleteEnvironmentalReading() throws Exception {
        EnvironmentalReading environmentalReading = environmentalReadingList.get(0);

        this.mockMvc
                .perform(
                        delete("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(environmentalReading.getId().toString())))
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading = environmentalReadingList.get(1);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/EnvironmentalReading/{id}", environmentalReadingId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures =
                setupTestDataForQuarterHourly(savedSensorSystem);
        averageTemperatures.size();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/QuarterHourly/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForQuarterHourly(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(environemntalReadingModel)
                            .supply(
                                    field(EnvironmentalReading::getSensorSystem),
                                    () -> sensorSystem)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusHours(3)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 180)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForQuarterHourly(sensorSystem.getId());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForHourly() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForHourly(savedSensorSystem);
        averageTemperatures.size();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/Hourly/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForHourly(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(environemntalReadingModel)
                            .supply(
                                    field(EnvironmentalReading::getSensorSystem),
                                    () -> sensorSystem)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusHours(24)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 1440)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForHourly(sensorSystem.getId());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForDaily() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForDaily(savedSensorSystem);
        averageTemperatures.size();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReading/Daily/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForDaily(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            EnvironmentalReading environmentalReading =
                    Instancio.of(environemntalReadingModel)
                            .supply(
                                    field(EnvironmentalReading::getSensorSystem),
                                    () -> sensorSystem)
                            .supply(
                                    field(EnvironmentalReading::getTimestamp),
                                    random ->
                                            ZonedDateTime.of(
                                                            LocalDateTime.now()
                                                                    .minusDays(7)
                                                                    .plusMinutes(
                                                                            random.intRange(
                                                                                    0, 10080)),
                                                            ZoneId.systemDefault())
                                                    .toOffsetDateTime())
                            .create();
            environmentalReadings.add(environmentalReading);
        }
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingService.getAverageTempsForDaily(sensorSystem.getId());
    }

    @AfterEach
    void tearDown() {
        environmentalReadingRepository.deleteAll();
        sensorSystemRepository.deleteAll();
        unconvUserRepository.deleteAll();
    }
}
