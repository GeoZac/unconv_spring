package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.SensorLocationService;
import com.unconv.spring.service.UnconvUserService;
import com.unconv.spring.web.rest.SensorLocationController;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = SensorLocationController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/SensorLocation")
@Import(MethodSecurityConfig.class)
class SensorLocationControllerTest extends AbstractControllerTest {
    @MockBean private SensorLocationService sensorLocationService;

    @MockBean private UnconvUserService unconvUserService;

    private List<SensorLocation> sensorLocationList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorLocation")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        sensorLocationList =
                Instancio.ofList(SensorLocation.class)
                        .size(3)
                        .generate(
                                field(SensorLocation::getLatitude),
                                gen -> gen.spatial().coordinate().lat())
                        .generate(
                                field(SensorLocation::getLongitude),
                                gen -> gen.spatial().coordinate().lon())
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllSensorLocations() throws Exception {
        Page<SensorLocation> page = new PageImpl<>(sensorLocationList);
        PagedResult<SensorLocation> sensorLocationPagedResult = new PagedResult<>(page);
        given(sensorLocationService.findAllSensorLocations(0, 10, "id", "asc"))
                .willReturn(sensorLocationPagedResult);

        this.mockMvc
                .perform(get("/SensorLocation"))
                .andDo(document("shouldFetchAllSensorLocations", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorLocationList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldReturn400WhenFetchAllSensorLocationsWithNegativePageNumber() throws Exception {
        String requestPath = "/SensorLocation";

        given(
                        sensorLocationService.findAllSensorLocations(
                                any(Integer.class),
                                any(Integer.class),
                                any(String.class),
                                any(String.class)))
                .willThrow(new IllegalArgumentException("Page index must not be less than zero"));

        this.mockMvc
                .perform(get(requestPath).param("pageNo", "-1"))
                .andDo(
                        document(
                                "shouldReturn400WhenFetchAllSensorLocationsWithNegativePageNumber",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Page index must not be less than zero")))
                .andReturn();
    }

    @Test
    void shouldFindSensorLocationById() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId, "Petra", 30.3285, 35.4414, SensorLocationType.OUTDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));

        String responseJson =
                this.mockMvc
                        .perform(get("/SensorLocation/{id}", sensorLocationId))
                        .andDo(
                                document(
                                        "shouldFindSensorLocationById",
                                        preprocessResponse(prettyPrint)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(sensorLocationId.toString())))
                        .andExpect(
                                jsonPath(
                                        "$.sensorLocationText",
                                        is(sensorLocation.getSensorLocationText())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        List<Object> fieldValues = JsonPath.read(responseJson, "$..*");
        for (Object fieldValue : fieldValues) {
            assertNotNull(fieldValue, "Field value should not be null");
        }
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingSensorLocation",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenFetchingSensorLocationByMalformedId() throws Exception {
        String sensorLocationId = UUID.randomUUID().toString().replace("-", "");

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andDo(
                        document(
                                "shouldReturn400WhenFetchingSensorLocationByMalformedId",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andReturn();
    }

    @Test
    void shouldCreateNewSensorLocation() throws Exception {
        given(sensorLocationService.saveSensorLocation(any(SensorLocation.class)))
                .willAnswer(
                        (invocation) -> {
                            SensorLocation sensorLocation = invocation.getArgument(0);
                            sensorLocation.setId(UUID.randomUUID());
                            return sensorLocation;
                        });

        SensorLocation sensorLocation =
                new SensorLocation(
                        null, "Moai Statues", -27.1212, -109.3667, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andDo(
                        document(
                                "shouldCreateNewSensorLocation",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn400WhenCreateNewSensorLocationWithNullValues() throws Exception {
        SensorLocation sensorLocation = new SensorLocation();

        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewSensorLocationWithNullValues",
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
                .andExpect(jsonPath("$.violations[0].field", is("sensorLocationText")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Sensor location text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId, " Alhambra", 37.1760, -3.5875, SensorLocationType.INDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));
        given(sensorLocationService.saveSensorLocation(any(SensorLocation.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocation.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andDo(
                        document(
                                "shouldUpdateSensorLocation",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId,
                        "Angkor Wat",
                        13.4125,
                        103.8667,
                        SensorLocationType.INDOOR);

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocationId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingSensorLocation",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation =
                new SensorLocation(
                        sensorLocationId,
                        " Hagia Sophia",
                        41.0082,
                        28.9784,
                        SensorLocationType.INDOOR);
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.of(sensorLocation));
        doNothing().when(sensorLocationService).deleteSensorLocationById(sensorLocation.getId());

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocation.getId()).with(csrf()))
                .andDo(document("shouldDeleteSensorLocation", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        given(sensorLocationService.findSensorLocationById(sensorLocationId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocationId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingSensorLocation",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFetchAllSensorLocationOfSpecificUnconvUser() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        UUID.randomUUID(),
                        "Specific UnconvUser",
                        "unconvuser@email.com",
                        "password");

        List<SensorLocation> sensorLocations =
                Instancio.ofList(SensorLocation.class)
                        .size(5)
                        .supply(
                                field(SensorLocation::getLatitude),
                                random -> random.doubleRange(-90.0, 90.0))
                        .supply(
                                field(SensorLocation::getLongitude),
                                random -> random.doubleRange(-180, 180))
                        .create();

        given(unconvUserService.findUnconvUserById(unconvUser.getId()))
                .willReturn(Optional.of(unconvUser));

        given(sensorLocationService.findAllSensorLocationsByUnconvUserId(unconvUser.getId()))
                .willReturn(sensorLocations);

        this.mockMvc
                .perform(get("/SensorLocation/UnconvUser/{unconvUserId}", unconvUser.getId()))
                .andDo(
                        document(
                                "shouldFetchAllSensorLocationOfSpecificUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(sensorLocations.size())));
    }

    @Test
    void shouldReturn404WhenFetchingSensorLocationOfNonExistingUnconvUser() throws Exception {
        UUID unconvUserId = UUID.randomUUID();
        given(unconvUserService.findUnconvUserById(unconvUserId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(
                        get("/SensorLocation/UnconvUser/{unconvUserId}", unconvUserId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingSensorLocationOfNonExistingUnconvUser",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }
}
