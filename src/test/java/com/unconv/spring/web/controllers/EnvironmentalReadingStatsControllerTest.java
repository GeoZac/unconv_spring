package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
import static org.instancio.Select.field;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.consts.SensorLocationType;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.domain.SensorLocation;
import com.unconv.spring.domain.SensorSystem;
import com.unconv.spring.service.EnvironmentalReadingStatsService;
import com.unconv.spring.web.rest.EnvironmentalReadingController;
import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = EnvironmentalReadingController.class)
@ActiveProfiles(PROFILE_TEST)
class EnvironmentalReadingStatsControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private EnvironmentalReadingStatsService environmentalReadingStatsService;

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
                                MockMvcRequestBuilders.get("/EnvironmentalReadingStats")
                                        .with(user("username").roles("USER")))
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
}
