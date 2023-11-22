package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_ER_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_ER_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_VALID_SENSOR_SYSTEM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
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
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.dto.EnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.web.rest.EnvironmentalReadingController;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = EnvironmentalReadingController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/EnvironmentalReading")
class EnvironmentalReadingControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @Autowired private MockMvcRestDocumentationConfigurer mockMvcRestDocumentationConfigurer;

    @MockBean private EnvironmentalReadingService environmentalReadingService;

    @Autowired private ObjectMapper objectMapper;

    private List<EnvironmentalReading> environmentalReadingList;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final SensorSystem sensorSystem =
            new SensorSystem(UUID.randomUUID(), "Sensor ABCD", sensorLocation, null);

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReading")
                                        .with(user("username").roles("USER")))
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
    }

    @Test
    void shouldFetchAllEnvironmentalReadings() throws Exception {
        Page<EnvironmentalReading> page = new PageImpl<>(environmentalReadingList);
        PagedResult<EnvironmentalReading> environmentalReadingPagedResult = new PagedResult<>(page);
        given(
                        environmentalReadingService.findAllEnvironmentalReadings(
                                0, 10, DEFAULT_ER_SORT_BY, DEFAULT_ER_SORT_DIRECTION))
                .willReturn(environmentalReadingPagedResult);

        this.mockMvc
                .perform(get("/EnvironmentalReading"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.totalElements", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindEnvironmentalReadingById() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        null,
                        13L,
                        75L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 1, 17, 17, 39), ZoneOffset.UTC),
                        sensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andDo(document("shouldFindEnvironmentalReadingById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(environmentalReading.getTemperature())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingEnvironmentalReading() throws Exception {
        UUID environmentalReadingId = UUID.randomUUID();
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindLatestEnvironmentalReadingsForASpecificUnconvUserId() throws Exception {
        List<EnvironmentalReading> environmentalReadings =
                Instancio.ofList(EnvironmentalReading.class)
                        .size(9)
                        .ignore(field(EnvironmentalReading::getId))
                        .create();

        UUID unconvUserId = UUID.randomUUID();
        given(
                        environmentalReadingService.findLatestEnvironmentalReadingsByUnconvUserId(
                                unconvUserId))
                .willReturn(environmentalReadings);

        this.mockMvc
                .perform(
                        get("/EnvironmentalReading/Latest/UnconvUser/{unconvUserId}", unconvUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", is(instanceOf(List.class))))
                .andExpect(jsonPath("$.size()", is(9)));
    }

    @Test
    void shouldCreateNewEnvironmentalReading() throws Exception {

        EnvironmentalReadingDTO environmentalReadingDTO =
                new EnvironmentalReadingDTO(
                        UUID.randomUUID(),
                        -3L,
                        53L,
                        OffsetDateTime.of(LocalDateTime.of(2023, 3, 7, 7, 56), ZoneOffset.UTC),
                        sensorSystem);

        MessageResponse<EnvironmentalReadingDTO> environmentalReadingDTOMessageResponse =
                new MessageResponse<>(environmentalReadingDTO, ENVT_RECORD_ACCEPTED);

        ResponseEntity<MessageResponse<EnvironmentalReadingDTO>>
                environmentalReadingDTOMessageResponseResponseEntity =
                        new ResponseEntity<>(
                                environmentalReadingDTOMessageResponse, HttpStatus.CREATED);

        given(
                        environmentalReadingService
                                .generateTimestampIfRequiredAndValidatedUnconvUserAndSaveEnvironmentalReading(
                                        any(EnvironmentalReadingDTO.class),
                                        any(Authentication.class)))
                .willReturn(environmentalReadingDTOMessageResponseResponseEntity);

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReadingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.temperature",
                                is(environmentalReadingDTO.getTemperature())));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithoutText() throws Exception {
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(null, 0L, 0L, OffsetDateTime.now().plusDays(1), null);

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
                .andExpect(jsonPath("$.violations[0].field", is("sensorSystem")))
                .andExpect(jsonPath("$.violations[0].message", is(ENVT_VALID_SENSOR_SYSTEM)))
                .andExpect(jsonPath("$.violations[1].field", is("timestamp")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
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
                        sensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        given(environmentalReadingService.saveEnvironmentalReading(any(EnvironmentalReading.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
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
                        sensorSystem);

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
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
                        sensorSystem);
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        doNothing()
                .when(environmentalReadingService)
                .deleteEnvironmentalReadingById(environmentalReading.getId());

        this.mockMvc
                .perform(
                        delete("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf()))
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
                .andExpect(status().isNotFound());
    }
}
