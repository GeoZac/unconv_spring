package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Offer;
import com.unconv.spring.dto.OfferDTO;
import com.unconv.spring.service.OfferService;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Offer")
@Slf4j
public class OfferController {

    @Autowired private OfferService offerService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public List<Offer> getAllOffers() {
        return offerService.findAllOffers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        return offerService
                .findOfferById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Offer createOffer(@RequestBody @Validated OfferDTO offerDTO) {
        offerDTO.setId(null);
        return offerService.saveOffer(modelMapper.map(offerDTO, Offer.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(
            @PathVariable Long id, @RequestBody @Valid OfferDTO offerDTO) {
        return offerService
                .findOfferById(id)
                .map(
                        offerObj -> {
                            offerDTO.setId(id);
                            return ResponseEntity.ok(
                                    offerService.saveOffer(modelMapper.map(offerDTO, Offer.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Offer> deleteOffer(@PathVariable Long id) {
        return offerService
                .findOfferById(id)
                .map(
                        offer -> {
                            offerService.deleteOfferById(id);
                            return ResponseEntity.ok(offer);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
