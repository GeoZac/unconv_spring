package com.unconv.spring.web.controllers;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
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

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.Route;
import com.unconv.spring.persistence.RouteRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

class RouteControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private RouteRepository routeRepository;

    private List<Route> routeList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Route")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        routeRepository.deleteAll();

        routeList = new ArrayList<>();
        routeList.add(new Route(null, "First Route"));
        routeList.add(new Route(null, "Second Route"));
        routeList.add(new Route(null, "Third Route"));
        routeList = routeRepository.saveAll(routeList);
    }

    @Test
    void shouldFetchAllRoutesInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Route").param("sortDir", "asc"))
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
    void shouldFetchAllRoutesInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/Route").param("sortDir", "desc"))
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
        Route route = routeList.get(0);
        Long routeId = route.getId();

        this.mockMvc
                .perform(get("/Route/{id}", routeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldCreateNewRoute() throws Exception {
        Route route = new Route(null, "New Route");
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
    void shouldReturn400WhenCreateNewRouteWithNullValues() throws Exception {
        Route route = new Route();

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
        Route route = routeList.get(0);
        route.setText("Updated Route");

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
    void shouldDeleteRoute() throws Exception {
        Route route = routeList.get(0);

        this.mockMvc
                .perform(delete("/Route/{id}", route.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(route.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingRoute() throws Exception {
        Long routeId = 0L;
        this.mockMvc.perform(get("/Route/{id}", routeId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRoute() throws Exception {
        Long routeId = 0L;
        Route route = new Route(routeId, "Updated route");

        this.mockMvc
                .perform(
                        put("/Route/{id}", routeId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(route)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUpdatingInvalidRoute() throws Exception {
        Route updatedRoute = routeList.get(0);
        updatedRoute.setText(null);

        this.mockMvc
                .perform(
                        put("/Route/{id}", updatedRoute.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedRoute)))
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
    void shouldReturn404WhenDeletingNonExistingRoute() throws Exception {
        Long routeId = 0L;
        this.mockMvc
                .perform(delete("/Route/{id}", routeId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
