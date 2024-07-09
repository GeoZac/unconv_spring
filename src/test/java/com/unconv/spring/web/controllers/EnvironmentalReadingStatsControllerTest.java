package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.calculateAverageTempsForDaily;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.calculateAverageTempsForHourly;
import static com.unconv.spring.utils.EnvironmentalReadingStatsUtils.calculateAverageTempsForQuarterHourly;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.enums.SensorLocationType;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import com.unconv.spring.service.SensorSystemService;
import com.unconv.spring.web.rest.EnvironmentalReadingStatsController;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = EnvironmentalReadingStatsController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/EnvironmentalReadingStats")
class EnvironmentalReadingStatsControllerTest extends AbstractControllerTest {

    @MockBean private EnvironmentalReadingStatsService environmentalReadingStatsService;

    @MockBean private SensorSystemService sensorSystemService;

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
                                MockMvcRequestBuilders.get("/EnvironmentalReadingStats")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly() throws Exception {
        UUID sensorSystemId = sensorSystem.getId();

        Map<OffsetDateTime, Double> averageTemperatures =
                calculateAverageTempsForQuarterHourly(
                        environmentalReadingStatsService, sensorSystem);

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
                .andDo(
                        document(
                                "shouldReturn200AndAverageTemperaturesAsMapForQuarterHourly",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
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
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingQuarterHourlyStatsForNonExistentSensorSystem",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForHourly() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        Map<OffsetDateTime, Double> averageTemperatures =
                calculateAverageTempsForHourly(environmentalReadingStatsService, sensorSystem);

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
                .andDo(
                        document(
                                "shouldReturn200AndAverageTemperaturesAsMapForHourly",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }

    @Test
    void shouldReturn200AndAverageTemperaturesAsMapForDaily() throws Exception {
        UUID sensorSystemId = UUID.randomUUID();

        Map<OffsetDateTime, Double> averageTemperatures =
                calculateAverageTempsForDaily(environmentalReadingStatsService, sensorSystem);

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
                .andDo(
                        document(
                                "shouldReturn200AndAverageTemperaturesAsMapForDaily",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", instanceOf(JSONArray.class)));
    }
}
