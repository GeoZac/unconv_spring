package com.unconv.spring.service;

import com.unconv.spring.domain.Route;
import com.unconv.spring.model.response.PagedResult;
import java.util.Optional;

public interface RouteService {
    PagedResult<Route> findAllRoutes(int pageNo, int pageSize, String sortBy, String sortDir);

    Optional<Route> findRouteById(Long id);

    Route saveRoute(Route route);

    void deleteRouteById(Long id);
}
