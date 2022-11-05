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
import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.persistence.FruitProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class FruitProductControllerIT extends AbstractIntegrationTest {

    @Autowired private FruitProductRepository fruitProductRepository;

    private List<FruitProduct> fruitProductList = null;

    @BeforeEach
    void setUp() {
        fruitProductRepository.deleteAll();

        fruitProductList = new ArrayList<>();
        fruitProductList.add(new FruitProduct(1L, "First FruitProduct"));
        fruitProductList.add(new FruitProduct(2L, "Second FruitProduct"));
        fruitProductList.add(new FruitProduct(3L, "Third FruitProduct"));
        fruitProductList = fruitProductRepository.saveAll(fruitProductList);
    }

    @Test
    void shouldFetchAllFruitProducts() throws Exception {
        this.mockMvc
                .perform(get("/FruitProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(fruitProductList.size())));
    }

    @Test
    void shouldFindFruitProductById() throws Exception {
        FruitProduct fruitProduct = fruitProductList.get(0);
        Long fruitProductId = fruitProduct.getId();

        this.mockMvc
                .perform(get("/FruitProduct/{id}", fruitProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(fruitProduct.getText())));
    }

    @Test
    void shouldCreateNewFruitProduct() throws Exception {
        FruitProduct fruitProduct = new FruitProduct(null, "New FruitProduct");
        this.mockMvc
                .perform(
                        post("/FruitProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(fruitProduct.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitProductWithoutText() throws Exception {
        FruitProduct fruitProduct = new FruitProduct(null, null);

        this.mockMvc
                .perform(
                        post("/FruitProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
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
    void shouldUpdateFruitProduct() throws Exception {
        FruitProduct fruitProduct = fruitProductList.get(0);
        fruitProduct.setText("Updated FruitProduct");

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", fruitProduct.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(fruitProduct.getText())));
    }

    @Test
    void shouldDeleteFruitProduct() throws Exception {
        FruitProduct fruitProduct = fruitProductList.get(0);

        this.mockMvc
                .perform(delete("/FruitProduct/{id}", fruitProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(fruitProduct.getText())));
    }
}
