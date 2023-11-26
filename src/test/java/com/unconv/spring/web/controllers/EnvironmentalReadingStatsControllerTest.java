package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static org.hamcrest.Matchers.instanceOf;
import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.web.rest.EnvironmentalReadingStatsController;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = EnvironmentalReadingStatsController.class)
@ActiveProfiles(PROFILE_TEST)
class EnvironmentalReadingStatsControllerTest extends AbstractControllerTest {

    @MockBean private EnvironmentalReadingStatsService environmentalReadingStatsService;

    @MockBean private SensorSystemService sensorSystemService;

    private final SensorLocation sensorLocation =
            new SensorLocation(
                    UUID.randomUUID(), "Parthenon", 37.9715, 23.7269, SensorLocationType.OUTDOOR);

    private final SensorSystem sensorSystem =
            new SensorSystem(UUID.randomUUID(), "Sensor ABCD", sensorLocation, null);

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
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReadingStats")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly() throws Exception {
        UUID sensorSystemId = sensorSystem.getId();

        Map<OffsetDateTime, Double> averageTemperatures = generateMockDataForQuarterHourlyStats();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));

        given(environmentalReadingStatsService.getAverageTempsForHourly(sensorSystemId))
                .willReturn(averageTemperatures);

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/QuarterHourly/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> generateMockDataForQuarterHourlyStats() {
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
        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }

    @Test
    void shouldReturn404WhenFetchingQuarterHourlyStatsForNonExistentSensorSystem()
            throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.empty());

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
        UUID sensorSystemId = UUID.randomUUID();

        Map<OffsetDateTime, Double> averageTemperatures = generateMockDataForHourlyStats();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));

        given(environmentalReadingStatsService.getAverageTempsForHourly(sensorSystemId))
                .willReturn(averageTemperatures);

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/Hourly/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    private Map<OffsetDateTime, Double> generateMockDataForHourlyStats() {
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
        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForDaily() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        Map<OffsetDateTime, Double> averageTemperatures = generateMockDataForDailyStats();

        given(sensorSystemService.findSensorSystemById(sensorSystemId))
                .willReturn(Optional.of(sensorSystem));

        given(environmentalReadingStatsService.getAverageTempsForDaily(sensorSystemId))
                .willReturn(averageTemperatures);

        this.mockMvc
                .perform(
                        get(
                                        "/EnvironmentalReadingStats/Daily/SensorSystem/{sensorSystemId}",
                                        sensorSystemId)
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    public Map<OffsetDateTime, Double> generateMockDataForDailyStats() {
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
        return environmentalReadingStatsService.getAverageTempsForHourly(environmentalReadings);
    }
}
