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
import com.unconv.spring.domain.Offer;
import com.unconv.spring.persistence.OfferRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class OfferControllerIT extends AbstractIntegrationTest {

    @Autowired private OfferRepository offerRepository;

    private List<Offer> offerList = null;

    @BeforeEach
    void setUp() {
        offerRepository.deleteAll();

        offerList = new ArrayList<>();
        offerList.add(new Offer(1L, "First Offer"));
        offerList.add(new Offer(2L, "Second Offer"));
        offerList.add(new Offer(3L, "Third Offer"));
        offerList = offerRepository.saveAll(offerList);
    }

    @Test
    void shouldFetchAllOffers() throws Exception {
        this.mockMvc
                .perform(get("/Offer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(offerList.size())));
    }

    @Test
    void shouldFindOfferById() throws Exception {
        Offer offer = offerList.get(0);
        Long offerId = offer.getId();

        this.mockMvc
                .perform(get("/Offer/{id}", offerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldCreateNewOffer() throws Exception {
        Offer offer = new Offer(null, "New Offer");
        this.mockMvc
                .perform(
                        post("/Offer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOfferWithoutText() throws Exception {
        Offer offer = new Offer(null, null);

        this.mockMvc
                .perform(
                        post("/Offer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
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
    void shouldUpdateOffer() throws Exception {
        Offer offer = offerList.get(0);
        offer.setText("Updated Offer");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldDeleteOffer() throws Exception {
        Offer offer = offerList.get(0);

        this.mockMvc
                .perform(delete("/Offer/{id}", offer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }
}
