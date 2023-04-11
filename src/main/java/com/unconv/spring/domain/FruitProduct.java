package com.unconv.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "fruit_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FruitProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Cost price cannot be empty")
    private float costPrice;

    @ManyToOne(
            optional = false,
            fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "fruit_id")
    @NotNull(message = "Fruit cannot be empty")
    private Fruit fruit;

    @ManyToOne(
            fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "offer_id")
    private Offer offer;

    @NotNull(message = "Package weight cannot be empty")
    private String packageWeight;

    @Column(nullable = false)
    @NotNull(message = "Selling price cannot be empty")
    private float sellingPrice;
}
