package com.unconv.spring.web.controllers;

import static com.unconv.spring.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.OrderProductService;
import com.unconv.spring.web.rest.OrderProductController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = OrderProductController.class)
@ActiveProfiles(PROFILE_TEST)
class OrderProductControllerTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @Autowired private MockMvc mockMvc;

    @MockBean private OrderProductService orderProductService;

    @Autowired private ObjectMapper objectMapper;

    private List<OrderProduct> orderProductList;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/OrderProduct")
                                        .with(user("username").roles("USER")))
                        .apply(springSecurity())
                        .build();

        this.orderProductList = new ArrayList<>();
        this.orderProductList.add(new OrderProduct(null, "text 1"));
        this.orderProductList.add(new OrderProduct(null, "text 2"));
        this.orderProductList.add(new OrderProduct(null, "text 3"));

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAllOrderProducts() throws Exception {
        Page<OrderProduct> page = new PageImpl<>(orderProductList);
        PagedResult<OrderProduct> orderProductPagedResult = new PagedResult<>(page);
        given(orderProductService.findAllOrderProducts(0, 10, "id", "asc"))
                .willReturn(orderProductPagedResult);

        this.mockMvc
                .perform(get("/OrderProduct"))
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
        UUID orderProductId = UUID.randomUUID();
        OrderProduct orderProduct = new OrderProduct(orderProductId, "text 1");
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.of(orderProduct));

        this.mockMvc
                .perform(get("/OrderProduct/{id}", orderProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/OrderProduct/{id}", orderProductId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOrderProduct() throws Exception {
        given(orderProductService.saveOrderProduct(any(OrderProduct.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        OrderProduct orderProduct = new OrderProduct(UUID.randomUUID(), "some text");
        this.mockMvc
                .perform(
                        post("/OrderProduct")
                                .with(csrf())
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
                                .with(csrf())
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
        UUID orderProductId = UUID.randomUUID();
        OrderProduct orderProduct = new OrderProduct(orderProductId, "Updated text");
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.of(orderProduct));
        given(orderProductService.saveOrderProduct(any(OrderProduct.class)))
                .willAnswer((invocation) -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/OrderProduct/{id}", orderProduct.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.empty());
        OrderProduct orderProduct = new OrderProduct(orderProductId, "Updated text");

        this.mockMvc
                .perform(
                        put("/OrderProduct/{id}", orderProductId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        OrderProduct orderProduct = new OrderProduct(orderProductId, "Some text");
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.of(orderProduct));
        doNothing().when(orderProductService).deleteOrderProductById(orderProduct.getId());

        this.mockMvc
                .perform(delete("/OrderProduct/{id}", orderProduct.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(orderProduct.getText())));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrderProduct() throws Exception {
        UUID orderProductId = UUID.randomUUID();
        given(orderProductService.findOrderProductById(orderProductId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/OrderProduct/{id}", orderProductId).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
