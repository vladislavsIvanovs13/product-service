package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.states.State;
import org.openjdk.jol.info.GraphLayout;

import static org.example.states.State.HIT;

@Getter
@Setter
@AllArgsConstructor
public class Statistics {
    private long operations;
    private long requests;
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

    public void incRequests() {
        requests++;
    }

    public void updateRates(State state) {
        if (state.equals(HIT)) {
            hitRate = (double) ++hits / requests;
            missRate = (double) misses / requests;
        }
        else {
            hitRate = (double) hits / requests;
            missRate = (double) ++misses / requests;
        }
    }

    public void updateMemoryUsed(Object... objects) {
        long recalculated = 0;
        for (Object object : objects)
            recalculated += GraphLayout.parseInstance(object).totalSize();
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
