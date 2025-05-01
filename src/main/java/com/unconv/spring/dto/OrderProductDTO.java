package com.unconv.spring.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductDTO {

    private UUID id;

    @NotEmpty(message = "Text cannot be empty")
    private String text;
}
