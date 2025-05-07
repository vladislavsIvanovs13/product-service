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

    @Value("${cache.operations}")
    private int operations;

    @Value("${cache.probability}")
    private double probability;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final SplayTree<Integer, Product> cache;

    @Transactional
    public void fillDatabase() {
        Faker faker = new Faker();
        List<Integer> ids = new ArrayList<>();

        int id = faker.number().hashCode();
        for (int i = 0; i < dataObjects; i++)
            ids.add(id++);
        Collections.shuffle(ids);

        for (int i = 0; i < dataObjects; i++) {
            Product product = Product.builder()
                    .id(ids.get(i))
                    .title(faker.commerce().productName())
                    .price(faker.number().randomDouble(2,1,1000))
                    .quantity(faker.number().numberBetween(1, 1000))
                    .weight(faker.number().randomDouble(2,1,100))
                    .build();
            productRepository.save(product);
        }
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

        return productMapper.toDto(product);
    }

    @Transactional
    public List<ProductDto> serve() {
        List<Integer> flow = new ArrayList<>();
        List<Integer> data = productRepository.getAllIds();
        Random random = new Random();
        List<ProductDto> products = new ArrayList<>();

        // need to check modelling course technic about probabilities and ifs
        for (int i = 0; i < operations; i++) {
            int bound = (int) Math.round(0.1 * dataObjects);
            if (random.nextDouble() < probability)
                flow.add(data.get(random.nextInt(0, bound)));
            else
                flow.add(data.get(random.nextInt(bound, dataObjects)));
        }

        var end = System.nanoTime();
        for (int productId : flow)
            products.add(getProduct(productId));

        System.out.println("Serving " + dataObjects + " products");
        System.out.printf(Locale.US, "Execution time: %.4f s%n", (System.nanoTime() - end) / 1_000_000_000.0);
        return products;
    }
}
