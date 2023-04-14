package com.unconv.spring.persistence;

import com.unconv.spring.domain.OrderProduct;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {}
