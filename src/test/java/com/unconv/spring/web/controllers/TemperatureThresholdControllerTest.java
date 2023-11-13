package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.TemperatureThresholdService;
import com.unconv.spring.web.rest.TemperatureThresholdController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = TemperatureThresholdController.class)
@ActiveProfiles(PROFILE_TEST)
class TemperatureThresholdControllerTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private TemperatureThresholdService temperatureThresholdService;

    @Autowired private ObjectMapper objectMapper;

    private List<TemperatureThreshold> temperatureThresholdList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/TemperatureThreshold")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.temperatureThresholdList = new ArrayList<>();
        temperatureThresholdList =
                Instancio.ofList(TemperatureThreshold.class)
                        .size(5)
                        .ignore(field(TemperatureThreshold::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllTemperatureThresholds() throws Exception {
        Page<TemperatureThreshold> page = new PageImpl<>(temperatureThresholdList);
        PagedResult<TemperatureThreshold> temperatureThresholdPagedResult = new PagedResult<>(page);
        given(temperatureThresholdService.findAllTemperatureThresholds(0, 10, "id", "asc"))
                .willReturn(temperatureThresholdPagedResult);

        this.mockMvc
                .perform(get("/TemperatureThreshold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(temperatureThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindTemperatureThresholdById() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));

        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewTemperatureThreshold() throws Exception {
        given(temperatureThresholdService.saveTemperatureThreshold(any(TemperatureThreshold.class)))
                .willAnswer(
                        (invocation) -> {
                            TemperatureThreshold temperatureThreshold = invocation.getArgument(0);
                            temperatureThreshold.setId(UUID.randomUUID());
                            return temperatureThreshold;
                        });

        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(UUID.randomUUID(), 100, 0);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldUpdateTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));
        given(temperatureThresholdService.saveTemperatureThreshold(any(TemperatureThreshold.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));
        doNothing()
                .when(temperatureThresholdService)
                .deleteTemperatureThresholdById(temperatureThreshold.getId());

        this.mockMvc
                .perform(
                        delete("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
