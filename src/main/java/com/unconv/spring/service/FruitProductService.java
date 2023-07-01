package com.unconv.spring.service;

import com.unconv.spring.domain.FruitProduct;
import java.util.List;
import java.util.Optional;

public interface FruitProductService {
    List<FruitProduct> findAllFruitProducts();

    Optional<FruitProduct> findFruitProductById(Long id);

    FruitProduct saveFruitProduct(FruitProduct fruitProduct);

    void deleteFruitProductById(Long id);
}
