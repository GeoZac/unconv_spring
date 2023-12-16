package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Fruit;
import com.unconv.spring.dto.FruitDTO;
import com.unconv.spring.service.FruitService;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Fruit")
@Slf4j
public class FruitController {

    @Autowired private FruitService fruitService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public List<Fruit> getAllFruits() {
        return fruitService.findAllFruits();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fruit> getFruitById(@PathVariable Long id) {
        return fruitService
                .findFruitById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Fruit createFruit(@RequestBody @Validated FruitDTO fruitDTO) {
        fruitDTO.setId(null);
        return fruitService.saveFruit(modelMapper.map(fruitDTO, Fruit.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fruit> updateFruit(
            @PathVariable Long id, @RequestBody @Valid FruitDTO fruitDTO) {
        return fruitService
                .findFruitById(id)
                .map(
                        fruitObj -> {
                            fruitDTO.setId(id);
                            return ResponseEntity.ok(
                                    fruitService.saveFruit(modelMapper.map(fruitDTO, Fruit.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Fruit> deleteFruit(@PathVariable Long id) {
        return fruitService
                .findFruitById(id)
                .map(
                        fruit -> {
                            fruitService.deleteFruitById(id);
                            return ResponseEntity.ok(fruit);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
