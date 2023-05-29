package com.unconv.spring.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {

    private Long id;

    @NotEmpty(message = "Text cannot be empty")
    private String text;
}
