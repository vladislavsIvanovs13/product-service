package org.product.repositories;

import org.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Product getProductById(int id);

    @Query("SELECT p.id FROM Product p")
    List<Integer> getAllIds();
}
