package org.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.product.states.State;
import org.openjdk.jol.info.GraphLayout;

import static org.product.states.State.HIT;

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
    private double opsTime;
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

    public void updateOpsTime(double time) {
        opsTime += time;
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
        double executionTime = opsTime / 1_000_000_000;
        throughput = operations / executionTime;
    }
}
