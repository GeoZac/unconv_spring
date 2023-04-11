package com.unconv.spring.service;

import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.persistence.FruitProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FruitProductService {

    @Autowired private FruitProductRepository fruitProductRepository;

    public List<FruitProduct> findAllFruitProducts() {
        return fruitProductRepository.findAll();
    }

    public Optional<FruitProduct> findFruitProductById(Long id) {
        return fruitProductRepository.findById(id);
    }

    public FruitProduct saveFruitProduct(FruitProduct fruitProduct) {
        return fruitProductRepository.save(fruitProduct);
    }

    public void deleteFruitProductById(Long id) {
        fruitProductRepository.deleteById(id);
    }
}
