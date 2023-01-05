package com.unconv.spring.web.rest;

import com.unconv.spring.domain.Heater;
import com.unconv.spring.dto.HeaterDTO;
import com.unconv.spring.service.HeaterService;
import java.util.List;
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
@RequestMapping("/Heater")
@Slf4j
public class HeaterController {

    @Autowired private HeaterService heaterService;

    @Autowired private ModelMapper modelMapper;

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
    public Heater createHeater(@RequestBody @Validated HeaterDTO heaterDTO) {
        return heaterService.saveHeater(modelMapper.map(heaterDTO, Heater.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Heater> updateHeater(@PathVariable Long id, @RequestBody HeaterDTO heaterDTO) {
        return heaterService
                .findHeaterById(id)
                .map(
                        heaterObj -> {
                            heaterDTO.setId(id);
                            return ResponseEntity.ok(heaterService.saveHeater(modelMapper.map(heaterDTO, Heater.class)));
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
