package com.unconv.spring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Entity
@Table(name = "fruits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fruit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotEmpty(message = "Fruit image URL cannot be empty")
    @URL(protocol = "https", message = "The fruit image URL should be valid")
    private String fruitImageUrl;

    @Column(nullable = false)
    @NotEmpty(message = "Fruit name cannot be empty")
    private String fruitName;

    @Column(nullable = false)
    @NotEmpty(message = "Fruit vendor name cannot be empty")
    private String fruitVendor;
}
