package org.example.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.Statistics;
import org.example.cache.Cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Getter
@Setter
public class S3FIFO<K, V> implements Cache<K, V> {
    private long mainMaxSize;
    private long smallMaxSize;
    private long maxSize;
    private Queue<Node> mainQueue;
    private Queue<Node> smallQueue;
    private Map<K, Node> ghostMap;
    private Statistics stats;

    public S3FIFO(long mainMaxSize, long smallMaxSize) {
        this.mainMaxSize = mainMaxSize;
        this.smallMaxSize = smallMaxSize;
        this.mainQueue = new LinkedList<>();
        this.smallQueue = new LinkedList<>();
        this.ghostMap = new HashMap<>();
        this.maxSize = mainMaxSize + smallMaxSize;
        this.stats = new Statistics(0,0,0,0,
                0,0, System.nanoTime(),0,0);
    }

    @AllArgsConstructor
    public class Node {
        private K key;
        private V value;
        private long freq;
    }

    @Override
    public V get(K key) {
        V smallQueueValue = searchForNode(smallQueue, key);
        if (smallQueueValue != null)
            return smallQueueValue;
        return searchForNode(mainQueue, key);
    }

    @Override
    public void put(K key, V value) {
        Node inserted = new Node(key, value, 0);

        if (mainQueue.size() + smallQueue.size() >= maxSize)
            evict();

        if (ghostMap.containsKey(key)) {
            mainQueue.add(inserted);
            ghostMap.remove(key);
        }
        else
            smallQueue.add(inserted);
    }

    @Override
    public void invalidateAll() {
        mainQueue.clear();
        smallQueue.clear();
        ghostMap.clear();
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
        else
            ghostMap.put(smallFirst.key, smallFirst);
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
                return node.value;
            }
        return null;
    }
}
