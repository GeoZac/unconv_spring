package com.unconv.spring.service;

import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.OrderProductRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderProductService {

    @Autowired private OrderProductRepository orderProductRepository;

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

    public Optional<OrderProduct> findOrderProductById(Long id) {
        return orderProductRepository.findById(id);
    }

    public OrderProduct saveOrderProduct(OrderProduct orderProduct) {
        return orderProductRepository.save(orderProduct);
    }

    public void deleteOrderProductById(Long id) {
        orderProductRepository.deleteById(id);
    }
}
