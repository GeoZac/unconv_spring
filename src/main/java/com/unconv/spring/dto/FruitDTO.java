package com.unconv.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FruitDTO {

    private Long id;

    @NotEmpty(message = "Fruit image URL cannot be empty")
    @URL(protocol = "https", message = "The fruit image URL should be valid")
    private String fruitImageUrl;

    @NotEmpty(message = "Fruit name cannot be empty")
    private String fruitName;

    @NotEmpty(message = "Fruit vendor name cannot be empty")
    private String fruitVendor;
}
