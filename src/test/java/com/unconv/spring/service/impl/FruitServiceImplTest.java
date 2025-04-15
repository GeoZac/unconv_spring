package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Fruit;
import com.unconv.spring.persistence.FruitRepository;
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
class FruitServiceImplTest {

    @Mock private FruitRepository fruitRepository;

    @InjectMocks private FruitServiceImpl fruitService;

    private Fruit fruit;
    private Long fruitId;

    @BeforeEach
    void setUp() {
        fruitId = 1L;
        fruit = new Fruit();
        fruit.setId(fruitId);
    }

    @Test
    void findAllFruits() {

        List<Fruit> fruitList = Collections.singletonList(fruit);

        when(fruitRepository.findAll()).thenReturn(fruitList);

        List<Fruit> result = fruitService.findAllFruits();

        assertEquals(fruitList.size(), result.size());
        assertEquals(fruitList.get(0).getId(), result.get(0).getId());
    }

    @Test
    void findFruitById() {
        when(fruitRepository.findById(any(Long.class))).thenReturn(Optional.of(fruit));

        Optional<Fruit> result = fruitService.findFruitById(fruitId);

        assertTrue(result.isPresent());
        assertEquals(fruit.getId(), result.get().getId());
    }

    @Test
    void saveFruit() {
        when(fruitRepository.save(any(Fruit.class))).thenReturn(fruit);

        Fruit result = fruitService.saveFruit(fruit);

        assertEquals(fruit.getId(), result.getId());
    }

    @Test
    void deleteFruitById() {
        fruitService.deleteFruitById(fruitId);

        verify(fruitRepository, times(1)).deleteById(fruitId);
    }
}
