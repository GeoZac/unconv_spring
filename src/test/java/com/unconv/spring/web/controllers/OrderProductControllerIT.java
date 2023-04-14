package com.unconv.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractIntegrationTest;
import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.persistence.OrderProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class OrderProductControllerIT extends AbstractIntegrationTest {

    @Autowired private OrderProductRepository orderProductRepository;

    private List<OrderProduct> orderProductList = null;

    @BeforeEach
    void setUp() {
        orderProductRepository.deleteAllInBatch();

        orderProductList = new ArrayList<>();
        orderProductList.add(new OrderProduct(null, "First OrderProduct"));
        orderProductList.add(new OrderProduct(null, "Second OrderProduct"));
        orderProductList.add(new OrderProduct(null, "Third OrderProduct"));
        orderProductList = orderProductRepository.saveAll(orderProductList);
    }

    @Test
    void shouldFetchAllBookingsInAscendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/OrderProduct").param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(orderProductList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFetchAllBookingsInDescendingOrder() throws Exception {
        this.mockMvc
                .perform(get("/OrderProduct").param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(orderProductList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindOrderProductById() throws Exception {
        OrderProduct orderProduct = orderProductList.get(0);
        UUID orderProductId = orderProduct.getId();

        this.mockMvc
                .perform(get("/OrderProduct/{id}", orderProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderProduct.getId().toString())))
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldCreateNewOrderProduct() throws Exception {
        OrderProduct orderProduct = new OrderProduct(null, "New OrderProduct");
        this.mockMvc
                .perform(
                        post("/OrderProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldReturn400WhenCreateNewOrderProductWithoutText() throws Exception {
        OrderProduct orderProduct = new OrderProduct(null, null);

        this.mockMvc
                .perform(
                        post("/OrderProduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("text")))
                .andExpect(jsonPath("$.violations[0].message", is("Text cannot be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateOrderProduct() throws Exception {
        OrderProduct orderProduct = orderProductList.get(0);
        orderProduct.setText("Updated OrderProduct");

        this.mockMvc
                .perform(
                        put("/OrderProduct/{id}", orderProduct.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderProduct.getId().toString())))
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldDeleteOrderProduct() throws Exception {
        OrderProduct orderProduct = orderProductList.get(0);

        this.mockMvc
                .perform(delete("/OrderProduct/{id}", orderProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderProduct.getId().toString())))
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        this.mockMvc
                .perform(get("/OrderProduct/{id}", orderProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        OrderProduct orderProduct = orderProductList.get(1);

        this.mockMvc
                .perform(
                        put("/OrderProduct/{id}", orderProductId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        this.mockMvc
                .perform(delete("/OrderProduct/{id}", orderProductId))
                .andExpect(status().isNotFound());
    }
}
