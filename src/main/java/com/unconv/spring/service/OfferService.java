package com.unconv.spring.service;

import com.unconv.spring.domain.Offer;
import java.util.List;
import java.util.Optional;

public interface OfferService {
    List<Offer> findAllOffers();

    Optional<Offer> findOfferById(Long id);

    Offer saveOffer(Offer offer);

    void deleteOfferById(Long id);
}
