package com.unconv.spring.persistence;

import com.unconv.spring.domain.Route;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {}
