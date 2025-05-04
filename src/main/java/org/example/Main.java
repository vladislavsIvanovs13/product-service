package org.example;

import com.github.javafaker.Faker;
import org.example.cache.Cache;
import org.example.impl.LinkedHashTable;
import org.example.impl.S3FIFO;
import org.example.impl.SplayTree;
import org.example.objects.Product;
import org.openjdk.jol.info.GraphLayout;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        int dataObjects = 1000;
        int maxCacheSize = 200;
        int operations = 10000;
        double probability = 0.9;

        Faker faker = new Faker();
        List<Product> data = new ArrayList<>();
        List<Product> flow = new ArrayList<>();
        Random random = new Random();

        List<Integer> ids = new ArrayList<>();
        int id = faker.number().hashCode();
        for (int i = 0; i < dataObjects; i++)
            ids.add(id++);
        Collections.shuffle(ids);

        for (int i = 0; i < dataObjects; i++) {
            Product product = Product.builder()
                    .id(ids.get(i))
                    .title(faker.commerce().productName())
                    .price(faker.number().randomDouble(2, 1, 1000))
                    .quantity(faker.number().numberBetween(1, 1000))
                    .weight(faker.number().randomDouble(2,1,100))
                    .build();
            data.add(product);
        }

        // need to check modelling course technic about probabilities and ifs
        for (int i = 0; i < operations; i++) {
            int bound = (int) Math.round(0.1 * dataObjects);
            if (random.nextDouble() < probability)
                flow.add(data.get(random.nextInt(0, bound)));
            else
                flow.add(data.get(random.nextInt(bound, data.size())));
        }

//        LinkedHashTable<Integer, Product> cache = new LinkedHashTable<>();
        SplayTree<Integer, Product> cache = new SplayTree<>();
//        S3FIFO<Integer, Product> cache = new S3FIFO<>();
        cache.setMaxSize(maxCacheSize);

        for (var product : flow) {
            int productId = product.getId();
            var prod = cache.get(productId);
            if (prod == null)
                cache.put(productId, product);
            // else return record
        }

//        System.out.println(GraphLayout.parseInstance(cache.getStats()).toPrintable());

        System.out.println("Size: " + cache.getStats().getCacheSize());
        System.out.printf(Locale.US, "Hit rate: %.4f%n", cache.getStats().getHitRate());
        System.out.printf(Locale.US, "Miss rate: %.4f%n", cache.getStats().getMissRate());

        System.out.println("Memory: " + cache.getStats().getMemoryUsed() + " bytes");
        System.out.println("Operations: " + cache.getStats().getOperations() + " ops");
        System.out.printf("Throughput: %.0f ops/s%n", cache.getStats().getThroughput());

        System.out.printf(Locale.US, "Execution time: %.4f s%n", (System.nanoTime() - cache.getStats().getStartTime()) / 1_000_000_000.0);
    }
}