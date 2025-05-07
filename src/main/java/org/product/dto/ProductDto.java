package org.product.dto;

import lombok.Data;

@Data
public class ProductDto {
    private int id;
    private String title;
    private double price;
    private int quantity;
    private double weight;
}
