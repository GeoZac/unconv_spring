package com.unconv.spring.web.controllers;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
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
import com.unconv.spring.domain.Fruit;
import com.unconv.spring.persistence.FruitRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FruitControllerIT extends AbstractIntegrationTest {

    @Autowired private FruitRepository fruitRepository;

    private List<Fruit> fruitList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Fruit")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        fruitRepository.deleteAll();

        fruitList = new ArrayList<>();
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
        fruitList = fruitRepository.saveAll(fruitList);
    }

    @Test
    void shouldFetchAllFruits() throws Exception {
        this.mockMvc
                .perform(get("/Fruit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(fruitList.size())));
    }

    @Test
    void shouldFindFruitById() throws Exception {
        Fruit fruit = fruitList.get(0);
        Long fruitId = fruit.getId();

        this.mockMvc
                .perform(get("/Fruit/{id}", fruitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldCreateNewFruit() throws Exception {
        Fruit fruit =
                new Fruit(
                        null,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        this.mockMvc
                .perform(
                        post("/Fruit")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitWithNullValues() throws Exception {
        Fruit fruit = new Fruit();

        this.mockMvc
                .perform(
                        post("/Fruit")
                                .with(csrf())
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
        Fruit fruit = fruitList.get(0);
        fruit.setFruitName("Watermelon");

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", fruit.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldDeleteFruit() throws Exception {
        Fruit fruit = fruitList.get(0);

        this.mockMvc
                .perform(delete("/Fruit/{id}", fruit.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fruitName", is(fruit.getFruitName())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingFruit() throws Exception {
        Long fruitId = 0L;

        this.mockMvc.perform(get("/Fruit/{id}", fruitId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingFruit() throws Exception {
        Long fruitId = 0L;

        Fruit fruit =
                new Fruit(
                        fruitId,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", fruitId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUpdatingInvalidFruit() throws Exception {
        Fruit updatedFruit = fruitList.get(0);
        updatedFruit.setFruitName(null);
        updatedFruit.setFruitVendor(null);
        updatedFruit.setFruitImageUrl(null);

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", updatedFruit.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedFruit)))
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
    void shouldReturn404WhenDeletingNonExistingFruit() throws Exception {
        Long fruitId = 0L;

        this.mockMvc
                .perform(delete("/Fruit/{id}", fruitId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
