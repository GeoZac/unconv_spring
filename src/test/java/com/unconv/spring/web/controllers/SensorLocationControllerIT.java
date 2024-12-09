package com.unconv.spring.web.controllers;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
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
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.enums.DefaultUserRole;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.persistence.SensorLocationRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvRoleRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvUserService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class SensorLocationControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private SensorLocationRepository sensorLocationRepository;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private UnconvRoleRepository unconvRoleRepository;

    @Autowired private UnconvUserRepository unconvUserRepository;

    private List<SensorLocation> sensorLocationList = null;

    private final Set<UnconvRole> unconvRoleSet = new HashSet<>();

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/SensorLocation")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        sensorLocationRepository.deleteAllInBatch();

        UnconvRole unconvRole = new UnconvRole(null, "ROLE_USER");
        UnconvRole savedUnconvRole = unconvRoleRepository.save(unconvRole);
        unconvRoleSet.add(savedUnconvRole);

        sensorLocationList = new ArrayList<>();
        sensorLocationList.add(
                new SensorLocation(
                        null,
                        "Great Pyramid of Giza",
                        29.9792,
                        31.1342,
                        SensorLocationType.INDOOR));
        sensorLocationList.add(
                new SensorLocation(
                        null, "Stonehenge", 51.1789, -1.8262, SensorLocationType.OUTDOOR));
        sensorLocationList.add(
                new SensorLocation(
                        null, "Machu Picchu", -13.1631, -72.5450, SensorLocationType.INDOOR));
        sensorLocationList = sensorLocationRepository.saveAll(sensorLocationList);
    }

    @Test
    void shouldFetchAllSensorLocationsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorLocation").param("sortDir", "asc"))
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
    void shouldFetchAllSensorLocationsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorLocation").param("sortDir", "desc"))
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
    void shouldReturn400WhenFetchAllSensorLocationsWithIncorrectParameter() throws Exception {
        String mismatchedParameter = "sensorName";
        String requestPath = "/SensorLocation";

        this.mockMvc
                .perform(get(requestPath).param("sortBy", mismatchedParameter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(
                        jsonPath(
                                "$.detail",
                                is("Invalid property reference: " + mismatchedParameter)))
                .andExpect(
                        jsonPath(
                                "$.timestamp",
                                matchesPattern(
                                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z")))
                .andExpect(jsonPath("$.path", is(requestPath)))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenFetchAllSensorLocationsWithNegativePageNumber() throws Exception {
        String requestPath = "/SensorLocation";

        this.mockMvc
                .perform(get(requestPath).param("pageNo", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Page index must not be less than zero")))
                .andReturn();
    }

    @Test
    void shouldFindSensorLocationById() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);
        UUID sensorLocationId = sensorLocation.getId();

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn400WhenFetchingSensorLocationByMalformedId() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);
        String sensorLocationId = sensorLocation.getId().toString().replace("-", "");

        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andReturn();
    }

    @Test
    void shouldCreateNewSensorLocation() throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(null, "Petra", 30.3285, 35.4414, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldCreateNewSensorLocationEvenIfAlreadyExistingPrimaryKeyInRequest() throws Exception {
        UUID alreadyExistingUUID = sensorLocationList.get(0).getId();

        SensorLocation sensorLocation =
                new SensorLocation(
                        alreadyExistingUUID, "Petra", 30.3285, 35.4414, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id", not(alreadyExistingUUID.toString())))
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
    void shouldReturn400WhenCreateNewSensorLocationWithImproperCoordinatesInNegativeRange()
            throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(null, "Heaven", -100.0, -200.0, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("latitude")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must be greater than or equal to -90.0")))
                .andExpect(jsonPath("$.violations[1].field", is("longitude")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("must be greater than or equal to -180.0")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewSensorLocationWithImproperCoordinatesInPositiveRange()
            throws Exception {
        SensorLocation sensorLocation =
                new SensorLocation(null, "Heaven", 100.0, 200.0, SensorLocationType.OUTDOOR);
        this.mockMvc
                .perform(
                        post("/SensorLocation")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("latitude")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must be less than or equal to 90.0")))
                .andExpect(jsonPath("$.violations[1].field", is("longitude")))
                .andExpect(
                        jsonPath(
                                "$.violations[1].message",
                                is("must be less than or equal to 180.0")))
                .andReturn();
    }

    @Test
    void shouldUpdateSensorLocation() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);
        sensorLocation.setSensorLocationText("Updated SensorLocation");

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocation.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldDeleteSensorLocation() throws Exception {
        SensorLocation sensorLocation = sensorLocationList.get(0);

        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocation.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorLocation.getId().toString())))
                .andExpect(
                        jsonPath(
                                "$.sensorLocationText",
                                is(sensorLocation.getSensorLocationText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/SensorLocation/{id}", sensorLocationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        SensorLocation sensorLocation = sensorLocationList.get(1);

        this.mockMvc
                .perform(
                        put("/SensorLocation/{id}", sensorLocationId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorLocation)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorLocation() throws Exception {
        UUID sensorLocationId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/SensorLocation/{id}", sensorLocationId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFetchAllSensorLocationsAssociatedWithAnUnconvUser() throws Exception {
        List<SensorLocation> sensorLocations =
                Instancio.ofList(SensorLocation.class)
                        .size(3)
                        .supply(
                                field(SensorLocation::getLatitude),
                                random -> random.doubleRange(-90.0, 90.0))
                        .supply(
                                field(SensorLocation::getLongitude),
                                random -> random.doubleRange(-180, 180))
                        .create();

        List<SensorLocation> savedSensorLocations =
                sensorLocationRepository.saveAll(sensorLocations);

        UnconvUser unconvUser =
                new UnconvUser(null, "Specific UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        List<SensorSystem> sensorSystemsOfSpecificUnconvUser =
                Instancio.ofList(SensorSystem.class)
                        .size(5)
                        .supply(field(SensorSystem::getUnconvUser), () -> savedUnconvUser)
                        .ignore(field(SensorSystem::getId))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .supply(
                                field(SensorSystem::getSensorLocation),
                                random -> random.oneOf(savedSensorLocations))
                        .create();

        for (SensorLocation sensorLocation : savedSensorLocations) {
            SensorSystem sensorSystem =
                    Instancio.of(SensorSystem.class)
                            .supply(field(SensorSystem::getUnconvUser), () -> savedUnconvUser)
                            .ignore(field(SensorSystem::getId))
                            .supply(field(SensorSystem::getSensorLocation), () -> sensorLocation)
                            .ignore(field(SensorSystem::getHumidityThreshold))
                            .ignore(field(SensorSystem::getTemperatureThreshold))
                            .create();
            sensorSystemsOfSpecificUnconvUser.add(sensorSystem);
        }

        List<SensorSystem> savedSensorSystems =
                sensorSystemRepository.saveAllAndFlush(sensorSystemsOfSpecificUnconvUser);

        assert !savedSensorSystems.isEmpty();

        this.mockMvc
                .perform(
                        get(
                                "/SensorLocation/UnconvUser/{unconvUserId}",
                                savedUnconvUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(savedSensorLocations.size())));
    }

    @Test
    void shouldReturn404FetchAllSensorLocationsAssociatedWithAnNonExistentUnconvUser()
            throws Exception {
        UUID unconvUserId = UUID.randomUUID();

        this.mockMvc
                .perform(get("/SensorLocation/UnconvUser/{unconvUserId}", unconvUserId.toString()))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @AfterEach
    void tearDown() {
        sensorSystemRepository.deleteAll();
        sensorLocationRepository.deleteAll();

        unconvUserRepository.deleteAll();
        List<UnconvRole> unconvRoles = unconvRoleRepository.findAll();
        for (UnconvRole unconvRole : unconvRoles) {
            if (EnumSet.allOf(DefaultUserRole.class).toString().contains(unconvRole.getName()))
                continue;
            unconvRoleRepository.delete(unconvRole);
        }
    }
}
