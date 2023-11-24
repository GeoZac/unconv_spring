package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_SS_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_SS_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.web.rest.SensorSystemController;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minidev.json.JSONArray;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = SensorSystemController.class)
@ActiveProfiles(PROFILE_TEST)
class SensorSystemControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private SensorSystemService sensorSystemService;

    @Autowired private ModelMapper modelMapper;

    @Autowired private ObjectMapper objectMapper;

    private List<SensorSystem> sensorSystemList;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorSystem")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.sensorSystemList = new ArrayList<>();
        this.sensorSystemList.add(new SensorSystem(null, "text 1", sensorLocation, null));
        this.sensorSystemList.add(new SensorSystem(null, "text 2", sensorLocation, null));
        this.sensorSystemList.add(new SensorSystem(null, "text 3", sensorLocation, null));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllSensorSystems() throws Exception {
        Page<SensorSystemDTO> page =
                new PageImpl<>(
                        sensorSystemList.stream()
                                .map((element) -> modelMapper.map(element, SensorSystemDTO.class))
                                .collect(Collectors.toList()));
        PagedResult<SensorSystemDTO> sensorSystemPagedResult = new PagedResult<>(page);
        given(
                        sensorSystemService.findAllSensorSystems(
                                0, 10, DEFAULT_SS_SORT_BY, DEFAULT_SS_SORT_DIRECTION))
                .willReturn(sensorSystemPagedResult);

        this.mockMvc
                .perform(get("/SensorSystem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorSystemById() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        UnconvUser unconvUser =
                new UnconvUser(
                        UUID.randomUUID(), "NewUnconvUser", "newuser@email.com", "1StrongPas$word");
        SensorSystem sensorSystem =
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
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(
                        UUID.randomUUID(), 32.1, 76.5, OffsetDateTime.now(), sensorSystem);
        SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);
        sensorSystemDTO.setReadingCount(new Random().nextLong());
        sensorSystemDTO.setLatestReading(
                modelMapper.map(environmentalReading, BaseEnvironmentalReadingDTO.class));
        given(sensorSystemService.findSensorSystemDTOById(sensorSystemId))
                .willReturn(Optional.of(sensorSystemDTO));

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.latestReading.temperature",
                                is(sensorSystemDTO.getLatestReading().getTemperature())))
                .andExpect(jsonPath("$.readingCount", is(sensorSystemDTO.getReadingCount())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldFetchRecentSensorReadingCountsWithReadingsPresent() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        Map<Integer, Long> recentReadingCounts = new HashMap<>();
        recentReadingCounts.put(1, 0L);
        recentReadingCounts.put(3, 4L);
        recentReadingCounts.put(8, 23L);
        recentReadingCounts.put(24, 40L);
        recentReadingCounts.put(168, 64L);

        given(sensorSystemService.findRecentStatsBySensorSystemId(sensorSystemId))
                .willReturn(recentReadingCounts);

        this.mockMvc
                .perform(get("/SensorSystem/ReadingsCount/{sensorSystemId}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)))
                .andExpect(jsonPath("$.1", is(0)))
                .andExpect(jsonPath("$.3", is(4)))
                .andExpect(jsonPath("$.8", is(23)))
                .andExpect(jsonPath("$.24", is(40)))
                .andExpect(jsonPath("$.168", is(64)));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        given(sensorSystemService.saveSensorSystem(any(SensorSystem.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        SensorSystemDTO sensorSystemDTO =
                new SensorSystemDTO(UUID.randomUUID(), "some text", sensorLocation, unconvUser);

        MessageResponse<SensorSystemDTO> environmentalReadingDTOMessageResponse =
                new MessageResponse<>(sensorSystemDTO, ENVT_RECORD_ACCEPTED);

        ResponseEntity<MessageResponse<SensorSystemDTO>>
                sensorSystemDTOMessageResponseResponseEntity =
                        new ResponseEntity<>(
                                environmentalReadingDTOMessageResponse, HttpStatus.CREATED);

        given(
                        sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                                any(SensorSystemDTO.class), any(Authentication.class)))
                .willReturn(sensorSystemDTOMessageResponseResponseEntity);

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystemDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystemDTO.getSensorName())));
    }

    @Test
    void shouldReturn400WhenCreateNewSensorSystemWithoutText() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        SensorSystem sensorSystem = new SensorSystem(null, null, null, unconvUser);

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("sensorName")))
                .andExpect(jsonPath("$.violations[0].message", is("Sensor name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem =
                new SensorSystem(sensorSystemId, "Updated text", sensorLocation, unconvUser);
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));
        given(sensorSystemService.saveSensorSystem(any(SensorSystem.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystemDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        UUID sensorSystemId = UUID.randomUUID();
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());
        SensorSystem sensorSystem =
                new SensorSystem(sensorSystemId, "Updated text", sensorLocation, unconvUser);

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem =
                new SensorSystem(sensorSystemId, "Some text", sensorLocation, null);
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));
        given(sensorSystemService.deleteSensorSystemById(sensorSystemId)).willReturn(true);

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystem.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystemId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
