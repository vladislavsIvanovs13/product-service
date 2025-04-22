package org.example.objects;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Product {
    private int id;
    private String title;
    private double price;
    private int quantity;
    private double weight;
}
