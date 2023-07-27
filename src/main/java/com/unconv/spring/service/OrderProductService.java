package com.unconv.spring.service;

import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;
import java.util.UUID;

public interface OrderProductService {
    PagedResult<OrderProduct> findAllOrderProducts(
            int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<OrderProduct> findOrderProductById(UUID id);

    OrderProduct saveOrderProduct(OrderProduct orderProduct);

    void deleteOrderProductById(UUID id);
}
