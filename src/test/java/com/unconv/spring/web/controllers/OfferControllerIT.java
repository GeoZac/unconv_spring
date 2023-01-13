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
        this.offerList.add(new Offer(1L, "0xffc62828", "50% OFF"));
        this.offerList.add(new Offer(2L, "0xff00aa4f", "OFFER"));
        this.offerList.add(new Offer(3L, "0xff000000", "FREE"));
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
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldCreateNewOffer() throws Exception {
        Offer offer = new Offer(1L, "0xff000000", "25% OFF");
        this.mockMvc
                .perform(
                        post("/Offer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldReturn400WhenCreateNewOfferWithoutText() throws Exception {
        Offer offer = new Offer(null, null, null);

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
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("badgeColor")))
                .andExpect(jsonPath("$.violations[0].message", is("Badge color cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenUpdatingOfferWithInvalidColor() throws Exception {
        Offer updatedOffer = offerList.get(1);
        updatedOffer.setBadgeColor("0x00aa4f");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", updatedOffer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedOffer)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("badgeColor")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must match \"^0x(?:[0-9a-fA-F]{4}){1,2}$\"")))
                .andReturn();
    }

    @Test
    void shouldUpdateOffer() throws Exception {
        Offer offer = offerList.get(0);
        offer.setBadgeColor("0xff000000");
        offer.setDescription("33% OFF");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldDeleteOffer() throws Exception {
        Offer offer = offerList.get(0);

        this.mockMvc
                .perform(delete("/Offer/{id}", offer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOffer() throws Exception {
        Long offerId = 0L;
        this.mockMvc.perform(get("/Offer/{id}", offerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOffer() throws Exception {
        Long offerId = 0L;
        Offer offer = new Offer(offerId, "0xff000000", "25% OFF");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOffer() throws Exception {
        Long offerId = 0L;
        this.mockMvc.perform(delete("/Offer/{id}", offerId)).andExpect(status().isNotFound());
    }
}
