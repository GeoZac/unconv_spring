package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Fruit;
import com.unconv.spring.persistence.FruitRepository;
import com.unconv.spring.service.FruitService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FruitServiceImpl implements FruitService {

    @Autowired private FruitRepository fruitRepository;

    @Override
    public List<Fruit> findAllFruits() {
        return fruitRepository.findAll();
    }

    @Override
    public Optional<Fruit> findFruitById(Long id) {
        return fruitRepository.findById(id);
    }

    @Override
    public Fruit saveFruit(Fruit fruit) {
        return fruitRepository.save(fruit);
    }

    @Override
    public void deleteFruitById(Long id) {
        fruitRepository.deleteById(id);
    }
}
