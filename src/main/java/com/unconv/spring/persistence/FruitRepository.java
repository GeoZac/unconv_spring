package com.unconv.spring.persistence;

import com.unconv.spring.domain.Fruit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FruitRepository extends JpaRepository<Fruit, Long> {}
