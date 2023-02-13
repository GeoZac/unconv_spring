package com.unconv.spring.web.rest;

import com.unconv.spring.domain.OrderProduct;
import com.unconv.spring.dto.OrderProductDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.OrderProductService;
import com.unconv.spring.utils.AppConstants;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/OrderProduct")
@Slf4j
public class OrderProductController {

    @Autowired private OrderProductService orderProductService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<OrderProduct> getAllOrderProducts(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return orderProductService.findAllOrderProducts(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderProduct> getOrderProductById(@PathVariable Long id) {
        return orderProductService
                .findOrderProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderProduct createOrderProduct(
            @RequestBody @Validated OrderProductDTO orderProductDTO) {
        return orderProductService.saveOrderProduct(
                modelMapper.map(orderProductDTO, OrderProduct.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderProduct> updateOrderProduct(
            @PathVariable Long id, @RequestBody @Valid OrderProductDTO orderProductDTO) {
        return orderProductService
                .findOrderProductById(id)
                .map(
                        orderProductObj -> {
                            orderProductDTO.setId(id);
                            return ResponseEntity.ok(
                                    orderProductService.saveOrderProduct(
                                            modelMapper.map(orderProductDTO, OrderProduct.class)));
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderProduct> deleteOrderProduct(@PathVariable Long id) {
        return orderProductService
                .findOrderProductById(id)
                .map(
                        orderProduct -> {
                            orderProductService.deleteOrderProductById(id);
                            return ResponseEntity.ok(orderProduct);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
