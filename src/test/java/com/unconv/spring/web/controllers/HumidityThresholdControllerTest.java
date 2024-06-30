package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = HumidityThresholdController.class)
@ActiveProfiles(PROFILE_TEST)
class HumidityThresholdControllerTest extends AbstractControllerTest {

    @MockBean private HumidityThresholdService humidityThresholdService;

    private List<HumidityThreshold> humidityThresholdList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/HumidityThreshold")
                                        .with(user("username").roles(UNCONV_USER.name())))
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
    void shouldFetchAllHumidityThresholds() throws Exception {
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
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 90, 10);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(humidityThresholdId.toString())))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
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
                .willAnswer(
                        (invocation) -> {
                            HumidityThreshold humidityThreshold = invocation.getArgument(0);
                            humidityThreshold.setId(UUID.randomUUID());
                            return humidityThreshold;
                        });

        HumidityThreshold humidityThreshold = new HumidityThreshold(null, 100, 0);
        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewHumidityThresholdWithNullValues() throws Exception {
        HumidityThreshold humidityThreshold = new HumidityThreshold();

        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("humidityThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
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
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
        ;
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
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
        ;
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
