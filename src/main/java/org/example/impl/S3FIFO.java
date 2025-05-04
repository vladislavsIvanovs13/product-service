package org.example.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.Statistics;
import org.example.cache.Cache;

import java.util.*;

import static org.example.states.State.HIT;
import static org.example.states.State.MISS;

@Getter
@Setter
public class S3FIFO<K, V> implements Cache<K, V> {
    private long mainMaxSize;
    private long smallMaxSize;
    private long maxSize;
    private Queue<Node> mainQueue;
    private Queue<Node> smallQueue;
    private Set<K> ghostSet;
    private Statistics stats;

    public S3FIFO() {
        mainQueue = new LinkedList<>();
        smallQueue = new LinkedList<>();
        ghostSet = new HashSet<>();
        stats = new Statistics(0,0,0,0,0,
                0,0, System.nanoTime(),0,0);
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
        this.mainMaxSize = (long) (0.9 * maxSize);
        this.smallMaxSize = (long) (0.1 * maxSize);
    }

    @AllArgsConstructor
    public class Node {
        private K key;
        private V value;
        private long freq;
    }

    @Override
    public V get(K key) {
        stats.incOperations();
        stats.incRequests();
        V smallQueueValue = searchForNode(smallQueue, key);
        if (smallQueueValue != null) {
            stats.updateThroughput();
            return smallQueueValue;
        }
        V mainQueueValue = searchForNode(mainQueue, key);
        if (mainQueueValue != null) {
            stats.updateThroughput();
            return mainQueueValue;
        }
        stats.updateRates(MISS);
        stats.updateThroughput();
        return null;
    }

    @Override
    public void put(K key, V value) {
        stats.incOperations();
        Node inserted = new Node(key, value, 0);

        if (mainQueue.size() + smallQueue.size() >= maxSize) {
            evict();
            stats.decCacheSize();
        }

        if (ghostSet.contains(key)) {
            mainQueue.add(inserted);
            ghostSet.remove(key);
        }
        else
            smallQueue.add(inserted);

        stats.incCacheSize();
//        stats.updateMemoryUsed(mainQueue, smallQueue, ghostSet);
        stats.updateThroughput();
    }

    @Override
    public void invalidateAll() {
        mainQueue.clear();
        smallQueue.clear();
        ghostSet.clear();
        stats.invalidateCacheSize();
//        stats.updateMemoryUsed(mainQueue, smallQueue, ghostSet);
        stats.updateThroughput();
    }

    private void evict() {
        if (smallQueue.size() >= smallMaxSize)
            evictSmall();
        else
            evictMain();
    }

    private void evictSmall() {
        Node smallFirst = smallQueue.remove();
        if (smallFirst.freq > 0) {
            if (mainQueue.size() >= mainMaxSize)
                evictMain();
            smallFirst.freq = 0;
            mainQueue.add(smallFirst);
        }
        else {
            if (ghostSet.size() >= mainMaxSize)
                ghostSet.clear();
            ghostSet.add(smallFirst.key);
        }
    }

    private void evictMain() {
        while (mainQueue.size() >= mainMaxSize) {
            Node mainFirst = mainQueue.remove();
            if (mainFirst.freq > 0) {
                mainFirst.freq--;
                mainQueue.add(mainFirst);
            }
        }
    }

    private V searchForNode(Queue<Node> queue, K key) {
        for (Node node : queue)
            if (node.key.equals(key)) {
                node.freq++;
                node.freq = Math.min(node.freq, 3);
                stats.updateRates(HIT);
                return node.value;
            }
        return null;
    }
}
