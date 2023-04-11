package com.unconv.spring.service;

import com.unconv.spring.domain.Fruit;
import com.unconv.spring.persistence.FruitRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FruitService {

    @Autowired private FruitRepository fruitRepository;

    public List<Fruit> findAllFruits() {
        return fruitRepository.findAll();
    }

    public Optional<Fruit> findFruitById(Long id) {
        return fruitRepository.findById(id);
    }

    public Fruit saveFruit(Fruit fruit) {
        return fruitRepository.save(fruit);
    }

    public void deleteFruitById(Long id) {
        fruitRepository.deleteById(id);
    }
}
