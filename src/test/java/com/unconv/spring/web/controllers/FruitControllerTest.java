package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.Fruit;
import com.unconv.spring.service.FruitService;
import com.unconv.spring.web.rest.FruitController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = FruitController.class)
@ActiveProfiles(PROFILE_TEST)
class FruitControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private FruitService fruitService;

    @Autowired private ObjectMapper objectMapper;

    private List<Fruit> fruitList;

    @BeforeEach
    void setUp() {
        this.fruitList = new ArrayList<>();
        this.fruitList.add(
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh"));
        this.fruitList.add(
                new Fruit(
                        2L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/mango_image_1.jpg",
                        "Mango",
                        "Daily Fresh"));
        this.fruitList.add(
                new Fruit(
                        3L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apricot_image.jpg",
                        "Apricot",
                        "Daily Fresh"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllFruits() throws Exception {
        given(fruitService.findAllFruits()).willReturn(this.fruitList);

        this.mockMvc
                .perform(get("/Fruit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(fruitList.size())));
    }

    @Test
    void shouldFindFruitById() throws Exception {
        Long fruitId = 1L;
        Fruit fruit =
                new Fruit(
                        fruitId,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.of(fruit));

        this.mockMvc
                .perform(get("/Fruit/{id}", fruitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingFruit() throws Exception {
        Long fruitId = 1L;
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Fruit/{id}", fruitId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewFruit() throws Exception {
        given(fruitService.saveFruit(any(Fruit.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Fruit fruit =
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        this.mockMvc
                .perform(
                        post("/Fruit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitWithoutText() throws Exception {
        Fruit fruit = new Fruit(null, null, null, null);

        this.mockMvc
                .perform(
                        post("/Fruit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(3)))
                .andExpect(jsonPath("$.violations[0].field", is("fruitImageUrl")))
                .andExpect(
                        jsonPath("$.violations[0].message", is("Fruit image URL cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateFruit() throws Exception {
        Long fruitId = 1L;
        Fruit fruit =
                new Fruit(
                        fruitId,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.of(fruit));
        given(fruitService.saveFruit(any(Fruit.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", fruit.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingFruit() throws Exception {
        Long fruitId = 1L;
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.empty());
        Fruit fruit =
                new Fruit(
                        fruitId,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", fruitId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteFruit() throws Exception {
        Long fruitId = 1L;
        Fruit fruit =
                new Fruit(
                        fruitId,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.of(fruit));
        doNothing().when(fruitService).deleteFruitById(fruit.getId());

        this.mockMvc
                .perform(delete("/Fruit/{id}", fruit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingFruit() throws Exception {
        Long fruitId = 1L;
        given(fruitService.findFruitById(fruitId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/Fruit/{id}", fruitId)).andExpect(status().isNotFound());
    }
}
