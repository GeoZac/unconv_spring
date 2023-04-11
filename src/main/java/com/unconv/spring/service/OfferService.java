package com.unconv.spring.service;

import com.unconv.spring.domain.Offer;
import com.unconv.spring.persistence.OfferRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OfferService {

    @Autowired private OfferRepository offerRepository;

    public List<Offer> findAllOffers() {
        return offerRepository.findAll();
    }

    public Optional<Offer> findOfferById(Long id) {
        return offerRepository.findById(id);
    }

    public Offer saveOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    public void deleteOfferById(Long id) {
        offerRepository.deleteById(id);
    }
}
