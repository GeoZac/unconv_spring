package com.unconv.spring.web.rest;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.Route;
import com.unconv.spring.dto.RouteDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.RouteService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Route")
@Slf4j
public class RouteController {

    @Autowired private RouteService routeService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<Route> getAllRoutes(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        return routeService.findAllRoutes(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        return routeService
                .findRouteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Route createRoute(@RequestBody @Validated RouteDTO routeDTO) {
        routeDTO.setId(null);
        return routeService.saveRoute(modelMapper.map(routeDTO, Route.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> updateRoute(
            @PathVariable Long id, @RequestBody @Valid RouteDTO routeDTO) {
        return routeService
                .findRouteById(id)
                .map(
                        routeObj -> {
                            routeDTO.setId(id);
                            return ResponseEntity.ok(
                                    routeService.saveRoute(modelMapper.map(routeDTO, Route.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Route> deleteRoute(@PathVariable Long id) {
        return routeService
                .findRouteById(id)
                .map(
                        route -> {
                            routeService.deleteRouteById(id);
                            return ResponseEntity.ok(route);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
