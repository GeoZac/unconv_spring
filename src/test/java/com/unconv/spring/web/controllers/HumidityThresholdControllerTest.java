package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.HumidityThresholdService;
import com.unconv.spring.web.rest.HumidityThresholdController;
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

@WebMvcTest(controllers = HumidityThresholdController.class)
@ActiveProfiles(PROFILE_TEST)
class HumidityThresholdControllerTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private HumidityThresholdService humidityThresholdService;

    @Autowired private ObjectMapper objectMapper;

    private List<HumidityThreshold> humidityThresholdList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/HumidityThreshold")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.humidityThresholdList = new ArrayList<>();
        humidityThresholdList =
                Instancio.ofList(HumidityThreshold.class)
                        .size(5)
                        .ignore(field(HumidityThreshold::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllSensorLocations() throws Exception {
        Page<HumidityThreshold> page = new PageImpl<>(humidityThresholdList);
        PagedResult<HumidityThreshold> humidityThresholdPagedResult = new PagedResult<>(page);
        given(humidityThresholdService.findAllHumidityThresholds(0, 10, "id", "asc"))
                .willReturn(humidityThresholdPagedResult);

        this.mockMvc
                .perform(get("/HumidityThreshold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(humidityThresholdList.size())))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindHumidityThresholdById() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewHumidityThreshold() throws Exception {
        given(humidityThresholdService.saveHumidityThreshold(any(HumidityThreshold.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        HumidityThreshold humidityThreshold = new HumidityThreshold(UUID.randomUUID(), 0, 100);
        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldUpdateHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));
        given(humidityThresholdService.saveHumidityThreshold(any(HumidityThreshold.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));
        doNothing()
                .when(humidityThresholdService)
                .deleteHumidityThresholdById(humidityThreshold.getId());

        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThreshold.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
