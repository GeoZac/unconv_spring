package com.unconv.spring.service;

import com.unconv.spring.domain.Fruit;
import java.util.List;
import java.util.Optional;

public interface FruitService {
    List<Fruit> findAllFruits();

    Optional<Fruit> findFruitById(Long id);

    Fruit saveFruit(Fruit fruit);

    void deleteFruitById(Long id);
}
