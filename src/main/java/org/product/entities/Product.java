package org.product.entities;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    private int id;

    @Column
    private String title;

    @Column
    private double price;

    @Column
    private int quantity;

    @Column
    private double weight;
}