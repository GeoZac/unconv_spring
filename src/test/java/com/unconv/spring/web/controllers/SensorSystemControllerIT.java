package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_ACCEPTED;
import static com.unconv.spring.consts.MessageConstants.ENVT_RECORD_REJ_USER;
import static com.unconv.spring.consts.MessageConstants.SENS_RECORD_REJ_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.consts.DefaultUserRole;
import com.unconv.spring.consts.SensorStatus;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.SensorSystemDTO;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvRoleRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.UnconvUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class SensorSystemControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private UnconvRoleRepository unconvRoleRepository;

    @Autowired private UnconvUserService unconvUserService;

    private static final int defaultPageSize = Integer.parseInt(DEFAULT_PAGE_SIZE);

    private static int totalPages;

    private List<SensorSystem> sensorSystemList = null;

    private List<SensorLocation> sensorLocationList = null;

    private List<UnconvUser> unconvUserList = null;

    private final Set<UnconvRole> unconvRoleSet = new HashSet<>();

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
                                MockMvcRequestBuilders.get("/SensorSystem")
                                        .with(user("UnconvUser").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        sensorSystemRepository.deleteAllInBatch();

        UnconvRole unconvRole = new UnconvRole(null, "ROLE_USER");
        UnconvRole savedUnconvRole = unconvRoleRepository.save(unconvRole);
        unconvRoleSet.add(savedUnconvRole);

        unconvUserList = new ArrayList<>();
        this.unconvUserList =
                Instancio.ofList(UnconvUser.class)
                        .size(7)
                        .supply(field(UnconvUser::getUnconvRoles), () -> unconvRoleSet)
                        .ignore(field(UnconvUser::getId))
                        .create();

        unconvUserList = unconvUserRepository.saveAll(unconvUserList);

        Random randomUtil = new Random();

        sensorLocationList =
                Instancio.ofList(SensorLocation.class)
                        .size(12)
                        .supply(
                                field(SensorLocation::getLatitude),
                                random -> random.doubleRange(-90.0, 90.0))
                        .supply(
                                field(SensorLocation::getLongitude),
                                random -> random.doubleRange(-180, 180))
                        .create();

        sensorSystemList =
                Instancio.ofList(SensorSystem.class)
                        .size(6)
                        .supply(
                                field(SensorSystem::getSensorLocation),
                                () -> {
                                    int randomIndex = randomUtil.nextInt(sensorLocationList.size());
                                    return sensorLocationList.get(randomIndex);
                                })
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .supply(
                                field(SensorSystem::getUnconvUser),
                                () -> {
                                    int randomIndex = randomUtil.nextInt(unconvUserList.size());
                                    return unconvUserList.get(randomIndex);
                                })
                        .create();
        totalPages = (int) Math.ceil((double) sensorSystemList.size() / defaultPageSize);
        sensorSystemList = sensorSystemRepository.saveAll(sensorSystemList);
    }

    @Test
    void shouldFetchAllSensorSystemsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorSystem").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.data[0].readingCount", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].latestReading").hasJsonPath())
                .andExpect(jsonPath("$.totalElements", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(sensorSystemList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(sensorSystemList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorSystemsOfSpecificUnconvUserInAscendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Specific UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        List<SensorSystem> sensorSystemsOfSpecificUnconvUser =
                Instancio.ofList(SensorSystem.class)
                        .size(5)
                        .supply(field(SensorSystem::isDeleted), () -> false)
                        .supply(field(SensorSystem::getUnconvUser), () -> savedUnconvUser)
                        .ignore(field(SensorSystem::getId))
                        .ignore(field(SensorSystem::getSensorLocation))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        List<SensorSystem> savedSensorSystemsOfSpecificUnconvUser =
                sensorSystemRepository.saveAll(sensorSystemsOfSpecificUnconvUser);

        List<EnvironmentalReading> environmentalReadingsOfSomeSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                random -> random.oneOf(savedSensorSystemsOfSpecificUnconvUser))
                        .create();

        environmentalReadingRepository.saveAll(environmentalReadingsOfSomeSensor);

        int dataSize = savedSensorSystemsOfSpecificUnconvUser.size();

        this.mockMvc
                .perform(
                        get("/SensorSystem/UnconvUser/{unconvUserId}", savedUnconvUser.getId())
                                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(dataSize)))
                .andExpect(jsonPath("$.data[0].readingCount", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].latestReading").hasJsonPath())
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorSystemsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/SensorSystem").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.data[0].readingCount", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].latestReading").hasJsonPath())
                .andExpect(jsonPath("$.totalElements", is(sensorSystemList.size())))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(sensorSystemList.size() < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(sensorSystemList.size() > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllSensorSystemsOfSpecificUnconvUserInDescendingOrder() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Specific UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        List<SensorSystem> sensorSystemsOfSpecificUnconvUser =
                Instancio.ofList(SensorSystem.class)
                        .size(5)
                        .supply(field(SensorSystem::isDeleted), () -> false)
                        .supply(field(SensorSystem::getUnconvUser), () -> savedUnconvUser)
                        .ignore(field(SensorSystem::getId))
                        .ignore(field(SensorSystem::getSensorLocation))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        List<SensorSystem> savedSensorSystemsOfSpecificUnconvUser =
                sensorSystemRepository.saveAll(sensorSystemsOfSpecificUnconvUser);

        List<EnvironmentalReading> environmentalReadingsOfSomeSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                random -> random.oneOf(savedSensorSystemsOfSpecificUnconvUser))
                        .create();

        environmentalReadingRepository.saveAll(environmentalReadingsOfSomeSensor);

        int dataSize = savedSensorSystemsOfSpecificUnconvUser.size();

        this.mockMvc
                .perform(
                        get("/SensorSystem/UnconvUser/{unconvUserId}", savedUnconvUser.getId())
                                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(dataSize)))
                .andExpect(jsonPath("$.data[0].readingCount", is(notNullValue())))
                .andExpect(jsonPath("$.data[0].latestReading").hasJsonPath())
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < defaultPageSize)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > defaultPageSize)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindSensorSystemDTOById() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        UUID sensorSystemId = sensorSystem.getId();

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.latestReading", is(nullValue())))
                .andExpect(jsonPath("$.readingCount", is(0)))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldFindSensorSystemDTOByIdWithReadingsPresent() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .create();

        List<EnvironmentalReading> savedEnvironmentalReadingsOfSpecificSensor =
                environmentalReadingRepository.saveAll(environmentalReadingsOfSpecificSensor);

        assert !savedEnvironmentalReadingsOfSpecificSensor.isEmpty();

        UUID sensorSystemId = sensorSystem.getId();

        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.latestReading.temperature", is(notNullValue())))
                .andExpect(
                        jsonPath(
                                "$.readingCount",
                                is(savedEnvironmentalReadingsOfSpecificSensor.size())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldFindSensorSystemBySensorName() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        String sensorSystemSensorName = sensorSystem.getSensorName();

        this.mockMvc
                .perform(get("/SensorSystem/SensorName/{sensorName}", sensorSystemSensorName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldFindSensorSystemOfSpecificUnconvUserBySensorName() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        String sensorSystemSensorName = sensorSystem.getSensorName();
        UUID unconvUserId = sensorSystem.getUnconvUser().getId();

        this.mockMvc
                .perform(
                        get(
                                "/SensorSystem/SensorName/{sensorName}/UnconvUser/{unconvUserId}",
                                sensorSystemSensorName,
                                unconvUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)));
    }

    @Test
    void shouldFindSensorSystemsBySensorName() throws Exception {
        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(4)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen -> gen.ints().range(0, 10).as(num -> "Sensor" + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .supply(
                                field(SensorSystem::getUnconvUser),
                                random -> random.oneOf(unconvUserList))
                        .create();

        List<SensorSystem> savedSensorSystems = sensorSystemRepository.saveAll(sensorSystems);

        this.mockMvc
                .perform(get("/SensorSystem/SensorName/{sensorName}", "Sensor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(savedSensorSystems.size())));
    }

    @Test
    void shouldFindSensorSystemsOfSpecificUnconvUserBySensorName() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Specific UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        List<SensorSystem> sensorSystems =
                Instancio.ofList(SensorSystem.class)
                        .size(4)
                        .ignore(field(SensorSystem::getSensorLocation))
                        .supply(field(SensorSystem::getUnconvUser), () -> savedUnconvUser)
                        .generate(
                                field(SensorSystem.class, "sensorName"),
                                gen -> gen.ints().range(0, 10).as(num -> "Sensor" + num.toString()))
                        .ignore(field(SensorSystem::getHumidityThreshold))
                        .ignore(field(SensorSystem::getTemperatureThreshold))
                        .create();

        List<SensorSystem> savedSensorSystems = sensorSystemRepository.saveAll(sensorSystems);

        this.mockMvc
                .perform(
                        get(
                                "/SensorSystem/SensorName/{sensorName}",
                                "Sensor",
                                savedUnconvUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(savedSensorSystems.size())));
    }

    @Test
    void shouldFetchRecentSensorReadingCountsWithReadingsPresent() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(75)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .supply(
                                field(EnvironmentalReading::getTimestamp),
                                random ->
                                        ZonedDateTime.of(
                                                        LocalDateTime.now()
                                                                .minusHours(
                                                                        random.intRange(0, 167)),
                                                        ZoneId.systemDefault())
                                                .toOffsetDateTime())
                        .create();

        List<EnvironmentalReading> savedEnvironmentalReadingsOfSpecificSensor =
                environmentalReadingRepository.saveAll(environmentalReadingsOfSpecificSensor);

        assert !savedEnvironmentalReadingsOfSpecificSensor.isEmpty();

        UUID sensorSystemId = sensorSystem.getId();

        this.mockMvc
                .perform(get("/SensorSystem/ReadingsCount/{sensorSystemId}", sensorSystemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)))
                .andExpect(jsonPath("$.1", notNullValue(long.class)))
                .andExpect(jsonPath("$.3", notNullValue(long.class)))
                .andExpect(jsonPath("$.8", notNullValue(long.class)))
                .andExpect(jsonPath("$.24", notNullValue(long.class)))
                .andExpect(jsonPath("$.168", is(75)));
    }

    @Test
    void shouldCreateNewSensorSystem() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(
                        null,
                        "New SensorSystem",
                        "Fully qualified sensor",
                        false,
                        SensorStatus.ACTIVE,
                        null,
                        savedUnconvUser,
                        new HumidityThreshold(null, 100, 0),
                        new TemperatureThreshold(null, 100, 0));

        assert sensorSystem.getHumidityThreshold().getId() == null;
        assert sensorSystem.getTemperatureThreshold().getId() == null;

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_ACCEPTED)))
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.entity.humidityThreshold", notNullValue()))
                .andExpect(jsonPath("$.entity.temperatureThreshold", notNullValue()))
                .andExpect(
                        jsonPath(
                                "$.entity.unconvUser.username", is(savedUnconvUser.getUsername())));
    }

    @Test
    void shouldCreateNewSensorSystemWithMinimalInfo() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        UnconvUser minimalUnconvUser = new UnconvUser();
        minimalUnconvUser.setId(savedUnconvUser.getId());

        SensorSystem sensorSystem =
                new SensorSystem(null, "New SensorSystem", null, minimalUnconvUser);

        assert sensorSystem.getUnconvUser().getUsername() == null;

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(
                        jsonPath("$.entity.unconvUser.id", is(savedUnconvUser.getId().toString())));
    }

    @Test
    void shouldCreateNewSensorSystemEvenIfAlreadyExistingPrimaryKeyInRequest() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "UnconvUser", "unconvuser@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        UnconvUser minimalUnconvUser = new UnconvUser();
        minimalUnconvUser.setId(savedUnconvUser.getId());

        UUID alreadyExistingUUID = sensorSystemList.get(0).getId();

        SensorSystem sensorSystem =
                new SensorSystem(alreadyExistingUUID, "New SensorSystem", null, minimalUnconvUser);

        assert sensorSystem.getUnconvUser().getUsername() == null;

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .characterEncoding(Charset.defaultCharset())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entity.id", notNullValue()))
                .andExpect(jsonPath("$.entity.id", not(alreadyExistingUUID.toString())))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(
                        jsonPath("$.entity.unconvUser.id", is(savedUnconvUser.getId().toString())));
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
                .andExpect(jsonPath("$.violations[1].field", is("sensorStatus")))
                .andExpect(jsonPath("$.violations[1].message", is("Sensor status cannot be null")))
                .andExpect(jsonPath("$.violations[2].field", is("unconvUser")))
                .andExpect(jsonPath("$.violations[2].message", is("UnconvUser cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn401WhenCreateNewSensorSystemForUnauthenticatedUser() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(null, "Some other user", "someonelse@email.com", "password");
        unconvUser.setUnconvRoles(unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem =
                new SensorSystem(null, "New SensorSystem", null, savedUnconvUser);
        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(ENVT_RECORD_REJ_USER)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(
                        jsonPath(
                                "$.entity.unconvUser.username", is(savedUnconvUser.getUsername())));
    }

    @Test
    void shouldReturn404WhenCreateNewSensorSystemWithoutValidUnconvUser() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(UUID.randomUUID(), "UnconvUser", "unconvuser@email.com", "password");
        SensorSystemDTO sensorSystemDTO =
                new SensorSystemDTO(null, "New SensorSystem", null, unconvUser);

        this.mockMvc
                .perform(
                        post("/SensorSystem")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystemDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(SENS_RECORD_REJ_USER)))
                .andExpect(jsonPath("$.entity.id", nullValue()))
                .andExpect(jsonPath("$.entity.sensorName", is(sensorSystemDTO.getSensorName())))
                .andExpect(jsonPath("$.entity.unconvUser.username", is(unconvUser.getUsername())));
    }

    @Test
    void shouldUpdateSensorSystem() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        sensorSystem.setSensorName("Updated SensorSystem");

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())));
    }

    @Test
    void shouldUpdateExistingSensorSystemWithNewThresholds() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);

        assert sensorSystem.getHumidityThreshold() == null;
        assert sensorSystem.getTemperatureThreshold() == null;

        sensorSystem.setHumidityThreshold(new HumidityThreshold(null, 100, 0));
        sensorSystem.setTemperatureThreshold(new TemperatureThreshold(null, 50, -50));

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystem.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.humidityThreshold", notNullValue()))
                .andExpect(jsonPath("$.temperatureThreshold", notNullValue()));
    }

    @Test
    void shouldDeleteSensorSystem() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystem.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystem.getId().toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.deleted", is(true)));
    }

    @Test
    void shouldMarkSensorSystemAsDeletedWithReadingsPresent() throws Exception {
        SensorSystem sensorSystem = sensorSystemList.get(0);
        List<EnvironmentalReading> environmentalReadingsOfSpecificSensor =
                Instancio.ofList(environemntalReadingModel)
                        .size(5)
                        .supply(field(EnvironmentalReading::getSensorSystem), () -> sensorSystem)
                        .create();

        List<EnvironmentalReading> savedEnvironmentalReadingsOfSpecificSensor =
                environmentalReadingRepository.saveAll(environmentalReadingsOfSpecificSensor);

        assert !savedEnvironmentalReadingsOfSpecificSensor.isEmpty();

        UUID sensorSystemId = sensorSystem.getId();

        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystemId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sensorSystemId.toString())))
                .andExpect(jsonPath("$.sensorName", is(sensorSystem.getSensorName())))
                .andExpect(jsonPath("$.deleted", is(true)));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/SensorSystem/{id}", sensorSystemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        SensorSystem sensorSystem = sensorSystemList.get(1);

        this.mockMvc
                .perform(
                        put("/SensorSystem/{id}", sensorSystemId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sensorSystem)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingSensorSystem() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/SensorSystem/{id}", sensorSystemId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        List<UnconvUser> unconvUsers = unconvUserRepository.findAll();
        for (UnconvUser unconvUser : unconvUsers) {
            Set<UnconvRole> unconvRoleSet = unconvUser.getUnconvRoles();
            unconvUser.getUnconvRoles().removeAll(unconvRoleSet);
            unconvUserRepository.save(unconvUser);
        }
        environmentalReadingRepository.deleteAll();
        sensorSystemRepository.deleteAll();
        List<UnconvRole> unconvRoles = unconvRoleRepository.findAll();
        for (UnconvRole unconvRole : unconvRoles) {
            if (EnumSet.allOf(DefaultUserRole.class).toString().contains(unconvRole.getName()))
                continue;
            unconvRoleRepository.delete(unconvRole);
        }
        unconvUserRepository.deleteAll();
    }
}
