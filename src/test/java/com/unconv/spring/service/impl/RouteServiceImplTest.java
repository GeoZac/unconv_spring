package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.Route;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.RouteRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock private RouteRepository routeRepository;

    @InjectMocks private RouteServiceImpl routeService;

    private Route route;
    private Long routeId;

    @BeforeEach
    void setUp() {
        routeId = 1L;
        route = new Route();
        route.setId(routeId);
    }

    @Test
    void findAllRoutesInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<Route> routeList = Collections.singletonList(route);
        Page<Route> routePage = new PageImpl<>(routeList);

        when(routeRepository.findAll(any(Pageable.class))).thenReturn(routePage);

        PagedResult<Route> result = routeService.findAllRoutes(pageNo, pageSize, sortBy, sortDir);

        assertEquals(routeList.size(), result.data().size());
        assertEquals(routeList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllRoutesInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<Route> routeList = Collections.singletonList(route);
        Page<Route> routePage = new PageImpl<>(routeList);

        when(routeRepository.findAll(any(Pageable.class))).thenReturn(routePage);

        PagedResult<Route> result = routeService.findAllRoutes(pageNo, pageSize, sortBy, sortDir);

        assertEquals(routeList.size(), result.data().size());
        assertEquals(routeList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findRouteById() {
        when(routeRepository.findById(any(Long.class))).thenReturn(Optional.of(route));

        Optional<Route> result = routeService.findRouteById(routeId);

        assertTrue(result.isPresent());
        assertEquals(route.getId(), result.get().getId());
    }

    @Test
    void saveRoute() {
        when(routeRepository.save(any(Route.class))).thenReturn(route);

        Route result = routeService.saveRoute(route);

        assertEquals(route.getId(), result.getId());
    }

    @Test
    void deleteRouteById() {
        routeService.deleteRouteById(routeId);

        verify(routeRepository, times(1)).deleteById(routeId);
    }
}
