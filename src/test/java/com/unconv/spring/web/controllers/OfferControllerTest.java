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
import com.unconv.spring.domain.Offer;
import com.unconv.spring.service.OfferService;
import com.unconv.spring.web.rest.OfferController;
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

@WebMvcTest(controllers = OfferController.class)
@ActiveProfiles(PROFILE_TEST)
class OfferControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OfferService offerService;

    @Autowired private ObjectMapper objectMapper;

    private List<Offer> offerList;

    @BeforeEach
    void setUp() {
        this.offerList = new ArrayList<>();
        this.offerList.add(new Offer(1L, "text 1"));
        this.offerList.add(new Offer(2L, "text 2"));
        this.offerList.add(new Offer(3L, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllOffers() throws Exception {
        given(offerService.findAllOffers()).willReturn(this.offerList);

        this.mockMvc
                .perform(get("/Offer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(offerList.size())));
    }

    @Test
    void shouldFindOfferById() throws Exception {
        Long offerId = 1L;
        Offer offer = new Offer(offerId, "text 1");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));

        this.mockMvc
                .perform(get("/Offer/{id}", offerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOffer() throws Exception {
        Long offerId = 1L;
        given(offerService.findOfferById(offerId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/Offer/{id}", offerId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOffer() throws Exception {
        given(offerService.saveOffer(any(Offer.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        Offer offer = new Offer(1L, "some text");
        this.mockMvc
                .perform(
                        post("/Offer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
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
        Long offerId = 1L;
        Offer offer = new Offer(offerId, "Updated text");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));
        given(offerService.saveOffer(any(Offer.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offer.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOffer() throws Exception {
        Long offerId = 1L;
        given(offerService.findOfferById(offerId)).willReturn(Optional.empty());
        Offer offer = new Offer(offerId, "Updated text");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOffer() throws Exception {
        Long offerId = 1L;
        Offer offer = new Offer(offerId, "Some text");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));
        doNothing().when(offerService).deleteOfferById(offer.getId());

        this.mockMvc
                .perform(delete("/Offer/{id}", offer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(offer.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOffer() throws Exception {
        Long offerId = 1L;
        given(offerService.findOfferById(offerId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/Offer/{id}", offerId)).andExpect(status().isNotFound());
    }
}
