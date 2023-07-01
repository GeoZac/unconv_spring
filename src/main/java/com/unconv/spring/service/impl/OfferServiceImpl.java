package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Offer;
import com.unconv.spring.persistence.OfferRepository;
import com.unconv.spring.service.OfferService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OfferServiceImpl implements OfferService {

    @Autowired private OfferRepository offerRepository;

    @Override
    public List<Offer> findAllOffers() {
        return offerRepository.findAll();
    }

    @Override
    public Optional<Offer> findOfferById(Long id) {
        return offerRepository.findById(id);
    }

    @Override
    public Offer saveOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    @Override
    public void deleteOfferById(Long id) {
        offerRepository.deleteById(id);
    }
}
