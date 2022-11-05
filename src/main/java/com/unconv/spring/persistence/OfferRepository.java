package com.unconv.spring.persistence;

import com.unconv.spring.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {}
