package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
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
import com.unconv.spring.domain.Heater;
import com.unconv.spring.service.HeaterService;
import com.unconv.spring.web.rest.HeaterController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = HeaterController.class)
@ActiveProfiles(PROFILE_TEST)
class HeaterControllerTest extends AbstractControllerTest {

    @MockBean private HeaterService heaterService;

    private List<Heater> heaterList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Heater")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        this.heaterList = new ArrayList<>();
        heaterList.add(new Heater(1L, 34F, 2F));
        heaterList.add(new Heater(2L, 40F, 1F));
        heaterList.add(new Heater(3L, 35F, 5F));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllHeaters() throws Exception {
        given(heaterService.findAllHeaters()).willReturn(this.heaterList);

        this.mockMvc
                .perform(get("/Heater"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(heaterList.size())));
    }

    @Test
    void shouldFindHeaterById() throws Exception {
        Long heaterId = 1L;
        Heater heater = new Heater(heaterId, 27F, 0.3F);
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.of(heater));

        this.mockMvc
                .perform(get("/Heater/{id}", heaterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingHeater() throws Exception {
        Long heaterId = 1L;
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Heater/{id}", heaterId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewHeater() throws Exception {
        given(heaterService.saveHeater(any(Heater.class)))
                .willAnswer(
                        (invocation) -> {
                            Heater heater = invocation.getArgument(0);
                            heater.setId(1L);
                            return heater;
                        });

        Heater heater = new Heater(1L, 30F, .5F);
        this.mockMvc
                .perform(
                        post("/Heater")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn400WhenCreateNewHeaterWithNullValues() throws Exception {
        Heater heater = new Heater();

        this.mockMvc
                .perform(
                        post("/Heater")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("tempTolerance")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Heater temperature tolerance cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateHeater() throws Exception {
        Long heaterId = 1L;
        Heater heater = new Heater(heaterId, 27F, .7F);
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.of(heater));
        given(heaterService.saveHeater(any(Heater.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Heater/{id}", heater.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingHeater() throws Exception {
        Long heaterId = 1L;
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.empty());
        Heater heater = new Heater(heaterId, 27F, .4F);

        this.mockMvc
                .perform(
                        put("/Heater/{id}", heaterId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(heater)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHeater() throws Exception {
        Long heaterId = 1L;
        Heater heater = new Heater(heaterId, 30F, 0.2F);
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.of(heater));
        doNothing().when(heaterService).deleteHeaterById(heater.getId());

        this.mockMvc
                .perform(delete("/Heater/{id}", heater.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature", is(heater.getTemperature()), Float.class));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingHeater() throws Exception {
        Long heaterId = 1L;
        given(heaterService.findHeaterById(heaterId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/Heater/{id}", heaterId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
