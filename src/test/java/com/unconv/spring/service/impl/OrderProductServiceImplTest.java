package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.OrderProductRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OrderProductServiceImplTest {

    @Mock private OrderProductRepository orderProductRepository;

    @InjectMocks private OrderProductServiceImpl orderProductService;

    private OrderProduct orderProduct;
    private UUID orderProductId;

    @BeforeEach
    void setUp() {
        orderProductId = UUID.randomUUID();
        orderProduct = new OrderProduct();
        orderProduct.setId(orderProductId);
    }

    @Test
    void findAllOrderProducts() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
        List<OrderProduct> orderProductList = Collections.singletonList(orderProduct);
        Page<OrderProduct> orderProductPage = new PageImpl<>(orderProductList);

        when(orderProductRepository.findAll(any(Pageable.class))).thenReturn(orderProductPage);

        PagedResult<OrderProduct> result =
                orderProductService.findAllOrderProducts(pageNo, pageSize, sortBy, sortDir);

        assertEquals(orderProductList.size(), result.data().size());
        assertEquals(orderProductList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findOrderProductById() {
        when(orderProductRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(orderProduct));

        Optional<OrderProduct> result = orderProductService.findOrderProductById(orderProductId);

        assertEquals(orderProduct.getId(), result.get().getId());
    }

    @Test
    void saveOrderProduct() {
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(orderProduct);

        OrderProduct result = orderProductService.saveOrderProduct(orderProduct);

        assertEquals(orderProduct.getId(), result.getId());
    }

    @Test
    void deleteOrderProductById() {
        orderProductService.deleteOrderProductById(orderProductId);

        verify(orderProductRepository, times(1)).deleteById(orderProductId);
    }
}
