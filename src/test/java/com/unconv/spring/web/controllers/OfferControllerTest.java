package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.consts.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.domain.Offer;
import com.unconv.spring.service.OfferService;
import com.unconv.spring.web.rest.OfferController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = OfferController.class)
@ActiveProfiles(PROFILE_TEST)
class OfferControllerTest extends AbstractControllerTest {

    @MockBean private OfferService offerService;

    private List<Offer> offerList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/Offer")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();

        this.offerList = new ArrayList<>();
        this.offerList.add(new Offer(1L, "0xffc62828", "50% OFF"));
        this.offerList.add(new Offer(2L, "0xff00aa4f", "OFFER"));
        this.offerList.add(new Offer(3L, "0xff000000", "FREE"));

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
        Offer offer = new Offer(offerId, "0xff000000", "25% OFF");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));

        this.mockMvc
                .perform(get("/Offer/{id}", offerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
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
                .willAnswer(
                        (invocation) -> {
                            Offer offer = invocation.getArgument(0);
                            offer.setId(1L);
                            return offer;
                        });

        Offer offer = new Offer(1L, "0xff000000", "25% OFF");
        this.mockMvc
                .perform(
                        post("/Offer")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldReturn400WhenCreateNewOfferWithoutText() throws Exception {
        Offer offer = new Offer(null, null, null);

        this.mockMvc
                .perform(
                        post("/Offer")
                                .with(csrf())
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
    void shouldUpdateOffer() throws Exception {
        Long offerId = 1L;
        Offer offer = new Offer(offerId, "0xff000000", "25% OFF");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));
        given(offerService.saveOffer(any(Offer.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offer.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOffer() throws Exception {
        Long offerId = 1L;
        given(offerService.findOfferById(offerId)).willReturn(Optional.empty());
        Offer offer = new Offer(offerId, "0xff000000", "25% OFF");

        this.mockMvc
                .perform(
                        put("/Offer/{id}", offerId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(offer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOffer() throws Exception {
        Long offerId = 1L;
        Offer offer = new Offer(offerId, "0xff000000", "25% OFF");
        given(offerService.findOfferById(offerId)).willReturn(Optional.of(offer));
        doNothing().when(offerService).deleteOfferById(offer.getId());

        this.mockMvc
                .perform(delete("/Offer/{id}", offer.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeColor", is(offer.getBadgeColor())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOffer() throws Exception {
        Long offerId = 1L;
        given(offerService.findOfferById(offerId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/Offer/{id}", offerId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenBadgeColorIsInvalid() throws Exception {
        Offer offer = new Offer(1L, "ffffff", "Buy 1 Get 1 Free");
        this.mockMvc
                .perform(
                        post("/Offer")
                                .with(csrf())
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
                .andExpect(jsonPath("$.violations[0].field", is("badgeColor")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("must match \"^0x(?:[0-9a-fA-F]{4}){1,2}$\"")))
                .andReturn();
    }
}
