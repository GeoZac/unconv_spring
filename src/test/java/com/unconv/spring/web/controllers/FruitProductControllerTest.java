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
import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.domain.Offer;
import com.unconv.spring.service.FruitProductService;
import com.unconv.spring.service.FruitService;
import com.unconv.spring.service.OfferService;
import com.unconv.spring.web.rest.FruitProductController;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = FruitProductController.class)
@ActiveProfiles(PROFILE_TEST)
class FruitProductControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private FruitProductService fruitProductService;

    @MockBean private FruitService fruitService;

    @MockBean private OfferService offerService;

    @Autowired private ObjectMapper objectMapper;

    private List<FruitProduct> fruitProductList;

    @BeforeEach
    void setUp() {
        this.fruitProductList = new ArrayList<>();
        Fruit fruit =
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        fruitService.saveFruit(fruit);
        Offer offer = new Offer(1L, "0xffc62828", "50% OFF");
        offerService.saveOffer(offer);

        fruitProductList = new ArrayList<>();
        fruitProductList.add(new FruitProduct(1L, 100.0f, fruit, offer, "1kg", 95.0f));
        fruitProductList.add(new FruitProduct(2L, 200f, fruit, null, "2kg", 195f));
        fruitProductList.add(new FruitProduct(3L, 150f, fruit, offer, "5kg", 135f));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllFruitProducts() throws Exception {
        given(fruitProductService.findAllFruitProducts()).willReturn(this.fruitProductList);

        this.mockMvc
                .perform(get("/FruitProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(fruitProductList.size())));
    }

    @Test
    void shouldFindFruitProductById() throws Exception {
        Long fruitProductId = 1L;
        Fruit fruit =
                new Fruit(
                        1L,
                        "https://raw.githubusercontent.com/GeoZac/static_iamge_dump/master/apple_image.png",
                        "Apple",
                        "Daily Fresh");
        Offer offer = new Offer(1L, "0xffc62828", "50% OFF");
        FruitProduct fruitProduct =
                new FruitProduct(fruitProductId, 100.0f, fruit, offer, "1kg", 95.0f);
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.of(fruitProduct));

        this.mockMvc
                .perform(get("/FruitProduct/{id}", fruitProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/FruitProduct/{id}", fruitProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewFruitProduct() throws Exception {
        given(fruitProductService.saveFruitProduct(any(FruitProduct.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Fruit fruit = new Fruit();
        Offer offer = new Offer();
        FruitProduct fruitProduct = new FruitProduct(1L, 100.0f, fruit, offer, "1kg", 95.0f);
        this.mockMvc
                .perform(
                        post("/FruitProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn400WhenCreateNewFruitProductWithoutText() throws Exception {
        FruitProduct fruitProduct = new FruitProduct(null, 0.0f, null, null, null, 0.0f);

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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("fruit")))
                .andExpect(jsonPath("$.violations[0].message", is("Fruit cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        Fruit fruit = new Fruit();
        Offer offer = new Offer();
        FruitProduct fruitProduct =
                new FruitProduct(fruitProductId, 100.0f, fruit, offer, "1kg", 95.0f);
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.of(fruitProduct));
        given(fruitProductService.saveFruitProduct(any(FruitProduct.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", fruitProduct.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        Fruit fruit = new Fruit();
        Offer offer = new Offer();
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.empty());
        FruitProduct fruitProduct =
                new FruitProduct(fruitProductId, 100.0f, fruit, offer, "1kg", 95.0f);

        this.mockMvc
                .perform(
                        put("/FruitProduct/{id}", fruitProductId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(fruitProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        Fruit fruit = new Fruit();
        Offer offer = new Offer();
        FruitProduct fruitProduct =
                new FruitProduct(fruitProductId, 100.0f, fruit, offer, "1kg", 95.0f);
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.of(fruitProduct));
        doNothing().when(fruitProductService).deleteFruitProductById(fruitProduct.getId());

        this.mockMvc
                .perform(delete("/FruitProduct/{id}", fruitProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costPrice", is(fruitProduct.getCostPrice()), Float.class));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingFruitProduct() throws Exception {
        Long fruitProductId = 1L;
        given(fruitProductService.findFruitProductById(fruitProductId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/FruitProduct/{id}", fruitProductId))
                .andExpect(status().isNotFound());
    }
}
