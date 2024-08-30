package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Offer;
import com.unconv.spring.persistence.OfferRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @Mock private OfferRepository offerRepository;

    @InjectMocks private OfferServiceImpl offerService;

    private Offer offer;
    private Long offerId;

    @BeforeEach
    void setUp() {
        offerId = 1L;
        offer = new Offer();
        offer.setId(offerId);
    }

    @Test
    void findAllOffers() {

        List<Offer> offerList = Collections.singletonList(offer);

        when(offerRepository.findAll()).thenReturn(offerList);

        List<Offer> result = offerService.findAllOffers();

        assertEquals(offerList.size(), result.size());
        assertEquals(offerList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void findOfferById() {
        when(offerRepository.findById(any(Long.class))).thenReturn(Optional.of(offer));

        Optional<Offer> result = offerService.findOfferById(offerId);

        assertEquals(offer.getId(), result.get().getId());
    }

    @Test
    void saveOffer() {
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        Offer result = offerService.saveOffer(offer);

        assertEquals(offer.getId(), result.getId());
    }

    @Test
    void deleteOfferById() {
        offerService.deleteOfferById(offerId);
    }
}
