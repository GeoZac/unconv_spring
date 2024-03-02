package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForDailyStats;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForHourlyStats;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.generateMockDataForQuarterHourlyStats;
import static org.hamcrest.Matchers.instanceOf;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.persistence.EnvironmentalReadingRepository;
import com.unconv.spring.persistence.SensorSystemRepository;
import com.unconv.spring.persistence.UnconvRoleRepository;
import com.unconv.spring.persistence.UnconvUserRepository;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import com.unconv.spring.service.UnconvUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class EnvironmentalReadingStatsControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private EnvironmentalReadingRepository environmentalReadingRepository;

    @Autowired private EnvironmentalReadingStatsService environmentalReadingStatsService;

    @Autowired private SensorSystemRepository sensorSystemRepository;

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private UnconvUserRepository unconvUserRepository;

    @Autowired private UnconvRoleRepository unconvRoleRepository;

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
                                MockMvcRequestBuilders.get("/EnvironmentalReadingStats")
                                        .with(user("UnconvUser").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        environmentalReadingRepository.deleteAllInBatch();

        UnconvRole unconvRole = new UnconvRole(null, "ROLE_USER");
        UnconvRole savedUnconvRole = unconvRoleRepository.save(unconvRole);
        unconvRoleSet.add(savedUnconvRole);

        UnconvUser unconvUser =
                new UnconvUser(
                        null, "UnconvUser", "unconvuser@email.com", "password", unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());

        SensorSystem sensorSystem = new SensorSystem(null, "Test sensor", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);

        List<EnvironmentalReading> environmentalReadingList =
                Instancio.ofList(environemntalReadingModel)
                        .size(15)
                        .supply(
                                field(EnvironmentalReading::getSensorSystem),
                                () -> savedSensorSystem)
                        .create();

        environmentalReadingRepository.saveAll(environmentalReadingList);
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        null, "UnconvUser", "unconvuser@email.com", "password", unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures =
                setupTestDataForQuarterHourly(savedSensorSystem);
        assert !averageTemperatures.isEmpty();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/QuarterHourly/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForQuarterHourly(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForQuarterHourlyStats(sensorSystem, 25);
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingStatsService.getAverageTempsForQuarterHourly(
                sensorSystem.getId());
    }

    @Test
    void shouldReturn404WhenFetchingQuarterHourlyStatsForNonExistentSensorSystem()
            throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/QuarterHourly/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForHourly() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        null, "UnconvUser", "unconvuser@email.com", "password", unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForHourly(savedSensorSystem);
        assert !averageTemperatures.isEmpty();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/Hourly/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForHourly(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForHourlyStats(sensorSystem, 75);
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingStatsService.getAverageTempsForHourly(sensorSystem.getId());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForDaily() throws Exception {
        UnconvUser unconvUser =
                new UnconvUser(
                        null, "UnconvUser", "unconvuser@email.com", "password", unconvRoleSet);
        UnconvUser savedUnconvUser =
                unconvUserService.saveUnconvUser(unconvUser, unconvUser.getPassword());
        SensorSystem sensorSystem = new SensorSystem(null, "Sensor System", null, savedUnconvUser);
        SensorSystem savedSensorSystem = sensorSystemRepository.save(sensorSystem);
        Map<OffsetDateTime, Double> averageTemperatures = setupTestDataForDaily(savedSensorSystem);
        assert !averageTemperatures.isEmpty();
        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/Daily/SensorSystem/{sensorSystemId}",
                                        savedSensorSystem.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> setupTestDataForDaily(SensorSystem sensorSystem) {
        List<EnvironmentalReading> environmentalReadings =
                generateMockDataForDailyStats(sensorSystem, 150);
        environmentalReadingRepository.saveAll(environmentalReadings);
        return environmentalReadingStatsService.getAverageTempsForDaily(sensorSystem.getId());
    }

    @AfterEach
    void tearDown() {
        environmentalReadingRepository.deleteAll();
        sensorSystemRepository.deleteAll();
        unconvUserRepository.deleteAll();
    }
}
