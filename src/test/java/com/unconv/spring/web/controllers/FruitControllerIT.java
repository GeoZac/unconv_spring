package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
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

class FruitControllerIT extends AbstractIntegrationTest {

    @Autowired private FruitRepository fruitRepository;

    private List<Fruit> fruitList = null;

    @BeforeEach
    void setUp() {
        fruitRepository.deleteAll();

        fruitList = new ArrayList<>();
        fruitList.add(new Fruit(1L, "First Fruit"));
        fruitList.add(new Fruit(2L, "Second Fruit"));
        fruitList.add(new Fruit(3L, "Third Fruit"));
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
                .andExpect(jsonPath("$.text", is(fruit.getText())));
    }

    @Test
    void shouldCreateNewFruit() throws Exception {
        Fruit fruit = new Fruit(null, "New Fruit");
        this.mockMvc
                .perform(
                        post("/Fruit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(fruit.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitWithoutText() throws Exception {
        Fruit fruit = new Fruit(null, null);

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
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateFruit() throws Exception {
        Fruit fruit = fruitList.get(0);
        fruit.setText("Updated Fruit");

        this.mockMvc
                .perform(
                        put("/Fruit/{id}", fruit.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(fruit.getText())));
    }

    @Test
    void shouldDeleteFruit() throws Exception {
        Fruit fruit = fruitList.get(0);

        this.mockMvc
                .perform(delete("/Fruit/{id}", fruit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(fruit.getText())));
    }
}
