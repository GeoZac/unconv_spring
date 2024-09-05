package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.persistence.FruitProductRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FruitProductServiceImplTest {

    @Mock private FruitProductRepository fruitProductRepository;

    @InjectMocks private FruitProductServiceImpl fruitProductService;

    private FruitProduct fruitProduct;
    private Long fruitProductId;

    @BeforeEach
    void setUp() {
        fruitProductId = 1L;
        fruitProduct = new FruitProduct();
        fruitProduct.setId(fruitProductId);
    }

    @Test
    void findAllFruitProducts() {

        List<FruitProduct> fruitProductList = Collections.singletonList(fruitProduct);

        when(fruitProductRepository.findAll()).thenReturn(fruitProductList);

        List<FruitProduct> result = fruitProductService.findAllFruitProducts();

        assertEquals(fruitProductList.size(), result.size());
        assertEquals(fruitProductList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void findFruitProductById() {
        when(fruitProductRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(fruitProduct));

        Optional<FruitProduct> result = fruitProductService.findFruitProductById(fruitProductId);

        assertTrue(result.isPresent());
        assertEquals(fruitProduct.getId(), result.get().getId());
    }

    @Test
    void saveFruitProduct() {
        when(fruitProductRepository.save(any(FruitProduct.class))).thenReturn(fruitProduct);

        FruitProduct result = fruitProductService.saveFruitProduct(fruitProduct);

        assertEquals(fruitProduct.getId(), result.getId());
    }

    @Test
    void deleteFruitProductById() {
        fruitProductService.deleteFruitProductById(fruitProductId);

        verify(fruitProductRepository, times(1)).deleteById(fruitProductId);
    }
}
