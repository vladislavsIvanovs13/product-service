package org.product.services;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.product.dto.ProductDto;
import org.product.impl.LinkedHashTable;
import org.product.impl.S3FIFO;
import org.product.mappers.ProductMapper;
import org.springframework.beans.factory.annotation.Value;
import org.product.impl.SplayTree;
import org.product.entities.Product;
import org.product.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    @Value("${cache.dataObjects}")
    private int dataObjects;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SplayTree<Integer, Product> cache;

    @Transactional
    public void fillDatabase() {
        Faker faker = new Faker();
        int id = faker.number().hashCode();

        for (int i = 0; i < dataObjects; i++) {
            Product product = Product.builder()
                    .id(id++)
                    .title(faker.commerce().productName())
                    .price(faker.number().randomDouble(2,1,1000))
                    .quantity(faker.number().numberBetween(1, 1000))
                    .weight(faker.number().randomDouble(2,1,100))
                    .build();
            productRepository.save(product);
        }
    }

    @Transactional
    public List<Integer> getAllIds() {
        return productRepository.getAllIds();
    }

    @Transactional
    public ProductDto getProduct(int productId) {
        var product = cache.get(productId);
        if (product == null) {
            product = productRepository.getProductById(productId);
            cache.put(productId, product);
        }

        System.out.println("Size: " + cache.getStats().getCacheSize());
        System.out.printf(Locale.US, "Hit rate: %.4f%n", cache.getStats().getHitRate());
        System.out.printf(Locale.US, "Miss rate: %.4f%n", cache.getStats().getMissRate());

        System.out.println("Memory: " + cache.getStats().getMemoryUsed() + " bytes");
        System.out.println("Operations: " + cache.getStats().getOperations() + " ops");
        System.out.printf("Throughput: %.0f ops/s%n", cache.getStats().getThroughput());

        System.out.printf(Locale.US, "Execution time: %.4f s%n", (System.nanoTime() - cache.getStats().getStartTime()) / 1_000_000_000.0);
        System.out.printf(Locale.US, "Ops time: %.4f s%n", cache.getStats().getOpsTime() / 1_000_000_000.0);

        return productMapper.toDto(product);
    }
}
