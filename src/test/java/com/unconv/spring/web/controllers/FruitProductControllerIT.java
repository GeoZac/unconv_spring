package com.unconv.spring.web.controllers;

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
import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.domain.Offer;
import com.unconv.spring.persistence.FruitProductRepository;
import com.unconv.spring.persistence.FruitRepository;
import com.unconv.spring.persistence.OfferRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

class FruitProductControllerIT extends AbstractIntegrationTest {
    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private FruitProductRepository fruitProductRepository;

    @Autowired private FruitRepository fruitRepository;

    @Autowired private OfferRepository offerRepository;

    private List<FruitProduct> fruitProductList = null;

    @BeforeEach
    void setUp() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/FruitProduct")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        fruitProductRepository.deleteAll();
        fruitRepository.deleteAll();
        offerRepository.deleteAll();

        Fruit fruit =
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        Fruit persistedFruit = fruitRepository.saveAndFlush(fruit);
        Offer offer = new Offer(1L, "0xffc62828", "50% OFF");
        Offer persistedOffer = offerRepository.saveAndFlush(offer);

        fruitProductList = new ArrayList<>();
        fruitProductList.add(
                new FruitProduct(1L, 100.0f, persistedFruit, persistedOffer, "1kg", 95.0f));
        fruitProductList.add(new FruitProduct(2L, 200f, persistedFruit, null, "2kg", 195f));
        fruitProductList.add(
                new FruitProduct(3L, 150f, persistedFruit, persistedOffer, "5kg", 135f));
        fruitProductList = fruitProductRepository.saveAllAndFlush(fruitProductList);
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
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldCreateNewFruitProduct() throws Exception {
        Fruit fruit =
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        Fruit persistedFruit = fruitRepository.saveAndFlush(fruit);
        Offer offer = new Offer(1L, "0xffc62828", "50% OFF");
        Offer persistedOffer = offerRepository.saveAndFlush(offer);
        FruitProduct fruitProduct =
                new FruitProduct(null, 100.0f, persistedFruit, persistedOffer, "1kg", 95.0f);
        this.mockMvc
                .perform(
                        post("/FruitProduct")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitProductWithoutText() throws Exception {
        FruitProduct fruitProduct = new FruitProduct(null, 0.0f, null, null, null, 0.0f);

        this.mockMvc
                .perform(
                        post("/FruitProduct")
                                .with(csrf())
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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("fruit")))
                .andExpect(jsonPath("$.violations[0].message", is("Fruit cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateFruitProduct() throws Exception {
        FruitProduct fruitProduct = fruitProductList.get(0);
        fruitProduct.setCostPrice(105f);

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", fruitProduct.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldDeleteFruitProduct() throws Exception {
        FruitProduct fruitProduct = fruitProductList.get(0);

        this.mockMvc
                .perform(delete("/FruitProduct/{id}", fruitProduct.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 0L;

        this.mockMvc
                .perform(get("/FruitProduct/{id}", fruitProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenUpdatingwithInvalidFruitProduct() throws Exception {
        FruitProduct updatedFruitProduct = fruitProductList.get(0);
        updatedFruitProduct.setCostPrice(0);
        updatedFruitProduct.setPackageWeight(null);
        updatedFruitProduct.setSellingPrice(0);
        updatedFruitProduct.setFruit(null);

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", updatedFruitProduct.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedFruitProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("fruit")))
                .andExpect(jsonPath("$.violations[0].message", is("Fruit cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        Fruit fruit = new Fruit();
        Offer offer = new Offer();
        FruitProduct fruitProduct =
                new FruitProduct(fruitProductId, 100.0f, fruit, offer, "1kg", 95.0f);

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", fruitProductId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 0L;

        this.mockMvc
                .perform(delete("/FruitProduct/{id}", fruitProductId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @AfterEach
    void tearDown() {
        fruitProductRepository.deleteAll();
        fruitRepository.deleteAll();
        offerRepository.deleteAll();
    }
}
