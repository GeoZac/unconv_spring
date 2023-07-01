package com.unconv.spring.service.impl;

import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.OrderProductRepository;
import com.unconv.spring.service.OrderProductService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderProductServiceImpl implements OrderProductService {

    @Autowired private OrderProductRepository orderProductRepository;

    @Override
    public PagedResult<OrderProduct> findAllOrderProducts(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<OrderProduct> orderProductsPage = orderProductRepository.findAll(pageable);

        return new PagedResult<>(orderProductsPage);
    }

    @Override
    public Optional<OrderProduct> findOrderProductById(UUID id) {
        return orderProductRepository.findById(id);
    }

    @Override
    public OrderProduct saveOrderProduct(OrderProduct orderProduct) {
        return orderProductRepository.save(orderProduct);
    }

    @Override
    public void deleteOrderProductById(UUID id) {
        orderProductRepository.deleteById(id);
    }
}
