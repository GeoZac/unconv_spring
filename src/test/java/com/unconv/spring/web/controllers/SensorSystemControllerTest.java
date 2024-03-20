package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_SS_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_SS_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
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
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.dto.base.BaseEnvironmentalReadingDTO;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.enums.SensorStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = SensorSystemController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/SensorSystem")
class SensorSystemControllerTest extends AbstractControllerTest {
    @MockBean private SensorSystemService sensorSystemService;

    @Autowired private ModelMapper modelMapper;

    private List<SensorSystem> sensorSystemList;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final UnconvUser unconvUser =
            new UnconvUser(
                    UUID.randomUUID(), "NewUnconvUser", "newuser@email.com", "1StrongPas$word");

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorSystem")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        // TODO Switch to Instancio generated objects
        this.sensorSystemList = new ArrayList<>();
        this.sensorSystemList.add(
                new SensorSystem(UUID.randomUUID(), "text 1", sensorLocation, null));
        this.sensorSystemList.add(
                new SensorSystem(UUID.randomUUID(), "text 2", sensorLocation, null));
        this.sensorSystemList.add(
                new SensorSystem(UUID.randomUUID(), "text 3", sensorLocation, null));

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
                .andDo(
                        document(
                                "shouldFetchAllSensorSystems",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
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
    void shouldFetchAllSensorSystemsOfSpecificUnconvUserInAscendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        UUID.randomUUID(),
                        "Specific UnconvUser",
                        "unconvuser@email.com",
                        "password");

        List<SensorSystem> sensorSystemsOfSpecificUnconvUser =
                Instancio.ofList(SensorSystem.class)
                        .size(5)
                        .supply(field(SensorSystem::isDeleted), () -> false)
                        .supply(field(SensorSystem::getUnconvUser), () -> unconvUser)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        Page<SensorSystemDTO> page =
                new PageImpl<>(
                        sensorSystemsOfSpecificUnconvUser.stream()
                                .map((element) -> modelMapper.map(element, SensorSystemDTO.class))
                                .collect(Collectors.toList()));
        PagedResult<SensorSystemDTO> sensorSystemPagedResult = new PagedResult<>(page);
        given(
                        sensorSystemService.findAllSensorSystemsByUnconvUserId(
                                unconvUser.getId(),
                                0,
                                10,
                                DEFAULT_SS_SORT_BY,
                                DEFAULT_SS_SORT_DIRECTION))
                .willReturn(sensorSystemPagedResult);

        int dataSize = sensorSystemsOfSpecificUnconvUser.size();

        this.mockMvc
                .perform(
                        get("/SensorSystem/UnconvUser/{unconvUserId}", unconvUser.getId())
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(dataSize)))
                .andExpect(jsonPath("$.data[0].readingCount", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].latestReading").hasJsonPath())
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
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

        String responseJson =
                this.mockMvc
                        .perform(get("/SensorSystem/{id}", sensorSystemId))
                        .andDo(
                                document(
                                        "shouldFindSensorSystemById",
                                        preprocessRequest(prettyPrint)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(sensorSystemId.toString())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Object> fieldValues = JsonPath.read(responseJson, "$..*");
        for (Object fieldValue : fieldValues) {
            assertNotNull(fieldValue, "Field value should not be null");
        }
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
                .andDo(
                        document(
                                "shouldFetchRecentSensorReadingCountsWithReadingsPresent",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
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
    void shouldFindSensorSystemBySensorName() throws Exception {
        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(4)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen -> gen.ints().range(0, 10).as(num -> "Sensor" + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        given(sensorSystemService.findAllSensorSystemsBySensorName(any(String.class)))
                .willReturn(sensorSystems);

        this.mockMvc
                .perform(get("/SensorSystem/SensorName/{sensorName}", "Sensor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(4)));
    }

    @Test
    void shouldFindSensorSystemOfSpecificUnconvUserBySensorName() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        UUID.randomUUID(),
                        "Specific UnconvUser",
                        "unconvuser@email.com",
                        "password");

        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(4)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .supply(field(SensorSystem::getUnconvUser), () -> unconvUser)
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen -> gen.ints().range(0, 10).as(num -> "Sensor" + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        given(
                        sensorSystemService.findAllBySensorSystemsBySensorNameAndUnconvUserId(
                                any(String.class), any(UUID.class)))
                .willReturn(sensorSystems);

        this.mockMvc
                .perform(
                        get(
                                "/SensorSystem/SensorName/{sensorName}/UnconvUser/{unconvUserId}",
                                "Sensor",
                                unconvUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(4)));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        given(sensorSystemService.saveSensorSystem(any(SensorSystem.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        SensorSystem sensorSystem =
                new SensorSystem(
                        null,
                        "New sensor system",
                        "A description about the new sensor system",
                        false,
                        SensorStatus.ACTIVE,
                        sensorLocation,
                        unconvUser,
                        new HumidityThreshold(60, 40),
                        new TemperatureThreshold(33, 23));
        SensorSystemDTO sensorSystemDTO = modelMapper.map(sensorSystem, SensorSystemDTO.class);

        given(
                        sensorSystemService.validateUnconvUserAndSaveSensorSystem(
                                any(SensorSystemDTO.class), any(Authentication.class)))
                .willAnswer(
                        (invocation) -> {
                            SensorSystemDTO sensorSystemArg = invocation.getArgument(0);
                            sensorSystemArg.setId(UUID.randomUUID());

                            MessageResponse<SensorSystemDTO>
                                    environmentalReadingDTOMessageResponse =
                                            new MessageResponse<>(
                                                    sensorSystemArg, ENVT_RECORD_ACCEPTED);

                            return new ResponseEntity<>(
                                    environmentalReadingDTOMessageResponse, HttpStatus.CREATED);
                        });

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystemDTO)))
                .andDo(
                        document(
                                "shouldCreateNewSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystemDTO.getSensorName())))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewSensorSystemWithNullValues() throws Exception {
        SensorSystem sensorSystem = new SensorSystem();

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewSensorSystemWithNullValues",
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
                .andExpect(jsonPath("$.violations[0].field", is("sensorName")))
                .andExpect(jsonPath("$.violations[0].message", is("Sensor name cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
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
                .andDo(
                        document(
                                "shouldUpdateSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
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
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem =
                new SensorSystem(
                        sensorSystemId,
                        "Existing sensor system",
                        "Sensor system without any readings associated",
                        false,
                        SensorStatus.ACTIVE,
                        sensorLocation,
                        unconvUser,
                        null,
                        null);
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));
        given(sensorSystemService.deleteSensorSystemById(sensorSystemId)).willReturn(true);

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystem.getId()).with(csrf()))
                .andDo(
                        document(
                                "shouldDeleteSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.deleted", is(true)))
                .andReturn();
    }

    @Test
    void shouldDeleteSensorSystemWithReadingsPresent() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem =
                new SensorSystem(
                        sensorSystemId,
                        "Existing sensor system",
                        "Sensor system with readings associated",
                        true,
                        SensorStatus.ACTIVE,
                        sensorLocation,
                        unconvUser,
                        null,
                        null);
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));
        given(sensorSystemService.deleteSensorSystemById(sensorSystemId)).willReturn(false);

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystem.getId()).with(csrf()))
                .andDo(
                        document(
                                "shouldDeleteSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.deleted", is(true)))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystemId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingSensorSystem",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }
}
