package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
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
import com.unconv.spring.domain.Route;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.RouteService;
import com.unconv.spring.web.rest.RouteController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

@WebMvcTest(controllers = RouteController.class)
@ActiveProfiles(PROFILE_TEST)
class RouteControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private RouteService routeService;

    @Autowired private ObjectMapper objectMapper;

    private List<Route> routeList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Route")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.routeList = new ArrayList<>();
        this.routeList.add(new Route(1L, "text 1"));
        this.routeList.add(new Route(2L, "text 2"));
        this.routeList.add(new Route(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllRoutes() throws Exception {
        Page<Route> page = new PageImpl<>(routeList);
        PagedResult<Route> routePagedResult = new PagedResult<>(page);
        given(routeService.findAllRoutes(0, 10, "id", "asc")).willReturn(routePagedResult);

        this.mockMvc
                .perform(get("/Route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(routeList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindRouteById() throws Exception {
        Long routeId = 1L;
        Route route = new Route(routeId, "text 1");
        given(routeService.findRouteById(routeId)).willReturn(Optional.of(route));

        this.mockMvc
                .perform(get("/Route/{id}", routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingRoute() throws Exception {
        Long routeId = 1L;
        given(routeService.findRouteById(routeId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Route/{id}", routeId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewRoute() throws Exception {
        given(routeService.saveRoute(any(Route.class)))
                .willAnswer(
                        (invocation) -> {
                            Route route = invocation.getArgument(0);
                            route.setId(1L);
                            return route;
                        });

        Route route = new Route(1L, "some text");
        this.mockMvc
                .perform(
                        post("/Route")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewRouteWithoutText() throws Exception {
        Route route = new Route(null, null);

        this.mockMvc
                .perform(
                        post("/Route")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(route)))
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
    void shouldUpdateRoute() throws Exception {
        Long routeId = 1L;
        Route route = new Route(routeId, "Updated text");
        given(routeService.findRouteById(routeId)).willReturn(Optional.of(route));
        given(routeService.saveRoute(any(Route.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Route/{id}", route.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRoute() throws Exception {
        Long routeId = 1L;
        given(routeService.findRouteById(routeId)).willReturn(Optional.empty());
        Route route = new Route(routeId, "Updated text");

        this.mockMvc
                .perform(
                        put("/Route/{id}", routeId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRoute() throws Exception {
        Long routeId = 1L;
        Route route = new Route(routeId, "Some text");
        given(routeService.findRouteById(routeId)).willReturn(Optional.of(route));
        doNothing().when(routeService).deleteRouteById(route.getId());

        this.mockMvc
                .perform(delete("/Route/{id}", route.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingRoute() throws Exception {
        Long routeId = 1L;
        given(routeService.findRouteById(routeId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/Route/{id}", routeId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
