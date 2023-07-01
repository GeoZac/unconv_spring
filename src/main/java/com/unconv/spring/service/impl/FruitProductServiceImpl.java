package com.unconv.spring.service.impl;

import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.persistence.FruitProductRepository;
import com.unconv.spring.service.FruitProductService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FruitProductServiceImpl implements FruitProductService {

    @Autowired private FruitProductRepository fruitProductRepository;

    @Override
    public List<FruitProduct> findAllFruitProducts() {
        return fruitProductRepository.findAll();
    }

    @Override
    public Optional<FruitProduct> findFruitProductById(Long id) {
        return fruitProductRepository.findById(id);
    }

    @Override
    public FruitProduct saveFruitProduct(FruitProduct fruitProduct) {
        return fruitProductRepository.save(fruitProduct);
    }

    @Override
    public void deleteFruitProductById(Long id) {
        fruitProductRepository.deleteById(id);
    }
}
