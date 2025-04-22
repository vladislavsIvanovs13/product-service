package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.jol.info.ClassLayout;

@Getter
@Setter
@AllArgsConstructor
public class Statistics {
    private long operations;
    private long hits;
    private long misses;
    private double hitRate;
    private double missRate;
    private double throughput;
    private double startTime;
    private long memoryUsed;
    private long cacheSize;

    public void invalidateCacheSize() {
        cacheSize = 0;
    }

    public void decCacheSize() {
        cacheSize--;
    }

    public void incCacheSize() {
        cacheSize++;
    }

    public void incOperations() {
        operations++;
    }

    public void updateHitRate() {
        hitRate = (double) ++hits / operations;
    }

    public void updateMissRate(boolean hasExtraMiss) {
        if (hasExtraMiss)
            missRate = (double) ++misses / operations;
        else
            missRate = (double) misses / operations;
    }

    public void updateMemoryUsed(Object... objects) {
        long recalculated = 0;
        for (Object object : objects)
            recalculated += ClassLayout.parseInstance(object).instanceSize();
        memoryUsed = recalculated;
    }

    public void updateThroughput() {
        double curTime = System.nanoTime();
        double executionTime = (curTime - startTime) / 1_000_000_000;
        throughput = operations / executionTime;
//        System.out.printf("Exec time: %.8f %n", executionTime);
//        System.out.printf("Throughput: %.0f ops/s%n", throughput);
    }
}
