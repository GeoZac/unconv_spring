package com.unconv.spring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FruitProductDTO {

    private Long id;

    @NotNull(message = "Cost price cannot be empty")
    private float costPrice;

    @NotNull(message = "Fruit cannot be empty")
    private FruitDTO fruit;

    private OfferDTO offer;

    @NotNull(message = "Package weight cannot be empty")
    private String packageWeight;

    @NotNull(message = "Selling price cannot be empty")
    private float sellingPrice;
}
