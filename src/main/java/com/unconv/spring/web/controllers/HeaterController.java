package com.unconv.spring.web.controllers;

import com.unconv.spring.entities.Heater;
import com.unconv.spring.services.HeaterService;
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
@RequestMapping("/Heater")
@Slf4j
public class HeaterController {

    private final HeaterService heaterService;

    @Autowired
    public HeaterController(HeaterService heaterService) {
        this.heaterService = heaterService;
    }

    @GetMapping
    public List<Heater> getAllHeaters() {
        return heaterService.findAllHeaters();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Heater> getHeaterById(@PathVariable Long id) {
        return heaterService
                .findHeaterById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Heater createHeater(@RequestBody @Validated Heater heater) {
        return heaterService.saveHeater(heater);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Heater> updateHeater(@PathVariable Long id, @RequestBody Heater heater) {
        return heaterService
                .findHeaterById(id)
                .map(
                        heaterObj -> {
                            heater.setId(id);
                            return ResponseEntity.ok(heaterService.saveHeater(heater));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Heater> deleteHeater(@PathVariable Long id) {
        return heaterService
                .findHeaterById(id)
                .map(
                        heater -> {
                            heaterService.deleteHeaterById(id);
                            return ResponseEntity.ok(heater);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
