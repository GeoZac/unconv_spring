package com.unconv.spring.persistence;

import com.unconv.spring.domain.OrderProduct;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {}
