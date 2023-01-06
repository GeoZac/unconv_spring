package com.unconv.spring.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferDTO {

    private Long id;

    @Pattern(regexp = "^0x(?:[0-9a-fA-F]{3,4}){1,2}$")
    @NotEmpty(message = "Badge color cannot be empty")
    private String badgeColor;

    @NotEmpty(message = "Description cannot be empty")
    private String description;
}
