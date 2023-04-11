package com.unconv.spring.persistence;

import com.unconv.spring.domain.FruitProduct;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FruitProductRepository extends JpaRepository<FruitProduct, Long> {}
