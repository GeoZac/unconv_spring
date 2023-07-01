package com.unconv.spring.service.impl;

import com.unconv.spring.domain.Route;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.RouteRepository;
import com.unconv.spring.service.RouteService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RouteServiceImpl implements RouteService {

    @Autowired private RouteRepository routeRepository;

    @Override
    public PagedResult<Route> findAllRoutes(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Route> routesPage = routeRepository.findAll(pageable);

        return new PagedResult<>(routesPage);
    }

    @Override
    public Optional<Route> findRouteById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public void deleteRouteById(Long id) {
        routeRepository.deleteById(id);
    }
}
