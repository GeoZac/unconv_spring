package com.unconv.spring.persistence;

import com.unconv.spring.domain.Heater;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HeaterRepository extends JpaRepository<Heater, Long> {}
