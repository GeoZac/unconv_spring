package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.EnvironmentalReading;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.EnvironmentalReadingService;
import com.unconv.spring.web.rest.EnvironmentalReadingController;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = EnvironmentalReadingController.class)
@ActiveProfiles(PROFILE_TEST)
class EnvironmentalReadingControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private EnvironmentalReadingService environmentalReadingService;

    @Autowired private ObjectMapper objectMapper;

    private List<EnvironmentalReading> environmentalReadingList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/EnvironmentalReading")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.environmentalReadingList = new ArrayList<>();
        this.environmentalReadingList.add(new EnvironmentalReading(1L, "text 1"));
        this.environmentalReadingList.add(new EnvironmentalReading(2L, "text 2"));
        this.environmentalReadingList.add(new EnvironmentalReading(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllEnvironmentalReadings() throws Exception {
        Page<EnvironmentalReading> page = new PageImpl<>(environmentalReadingList);
        PagedResult<EnvironmentalReading> environmentalReadingPagedResult = new PagedResult<>(page);
        given(environmentalReadingService.findAllEnvironmentalReadings(0, 10, "id", "asc"))
                .willReturn(environmentalReadingPagedResult);

        this.mockMvc
                .perform(get("/EnvironmentalReading"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(environmentalReadingList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindEnvironmentalReadingById() throws Exception {
        Long environmentalReadingId = 1L;
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(environmentalReadingId, "text 1");
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(environmentalReading.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingEnvironmentalReading() throws Exception {
        Long environmentalReadingId = 1L;
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/EnvironmentalReading/{id}", environmentalReadingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewEnvironmentalReading() throws Exception {
        given(environmentalReadingService.saveEnvironmentalReading(any(EnvironmentalReading.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        EnvironmentalReading environmentalReading = new EnvironmentalReading(1L, "some text");
        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(environmentalReading.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewEnvironmentalReadingWithoutText() throws Exception {
        EnvironmentalReading environmentalReading = new EnvironmentalReading(null, null);

        this.mockMvc
                .perform(
                        post("/EnvironmentalReading")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateEnvironmentalReading() throws Exception {
        Long environmentalReadingId = 1L;
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(environmentalReadingId, "Updated text");
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        given(environmentalReadingService.saveEnvironmentalReading(any(EnvironmentalReading.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(environmentalReading.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingEnvironmentalReading() throws Exception {
        Long environmentalReadingId = 1L;
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(environmentalReadingId, "Updated text");

        this.mockMvc
                .perform(
                        put("/EnvironmentalReading/{id}", environmentalReadingId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(environmentalReading)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteEnvironmentalReading() throws Exception {
        Long environmentalReadingId = 1L;
        EnvironmentalReading environmentalReading =
                new EnvironmentalReading(environmentalReadingId, "Some text");
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.of(environmentalReading));
        doNothing()
                .when(environmentalReadingService)
                .deleteEnvironmentalReadingById(environmentalReading.getId());

        this.mockMvc
                .perform(
                        delete("/EnvironmentalReading/{id}", environmentalReading.getId())
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(environmentalReading.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingEnvironmentalReading() throws Exception {
        Long environmentalReadingId = 1L;
        given(environmentalReadingService.findEnvironmentalReadingById(environmentalReadingId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/EnvironmentalReading/{id}", environmentalReadingId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
