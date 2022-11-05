package com.unconv.spring.web.rest;

import com.unconv.spring.domain.FruitProduct;
import com.unconv.spring.service.FruitProductService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/FruitProduct")
@Slf4j
public class FruitProductController {

    private final FruitProductService fruitProductService;

    @Autowired
    public FruitProductController(FruitProductService fruitProductService) {
        this.fruitProductService = fruitProductService;
    }

    @GetMapping
    public List<FruitProduct> getAllFruitProducts() {
        return fruitProductService.findAllFruitProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FruitProduct> getFruitProductById(@PathVariable Long id) {
        return fruitProductService
                .findFruitProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FruitProduct createFruitProduct(@RequestBody @Validated FruitProduct fruitProduct) {
        return fruitProductService.saveFruitProduct(fruitProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FruitProduct> updateFruitProduct(
            @PathVariable Long id, @RequestBody FruitProduct fruitProduct) {
        return fruitProductService
                .findFruitProductById(id)
                .map(
                        fruitProductObj -> {
                            fruitProduct.setId(id);
                            return ResponseEntity.ok(
                                    fruitProductService.saveFruitProduct(fruitProduct));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FruitProduct> deleteFruitProduct(@PathVariable Long id) {
        return fruitProductService
                .findFruitProductById(id)
                .map(
                        fruitProduct -> {
                            fruitProductService.deleteFruitProductById(id);
                            return ResponseEntity.ok(fruitProduct);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
