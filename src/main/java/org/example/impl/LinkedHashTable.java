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
public class LinkedHashTable<K, V> implements Cache<K, V> {
    private Map<K, Node> hashTable;
    private List<Node> list;
    private long maxSize;
    private Statistics stats;

    public LinkedHashTable() {
        hashTable = new HashMap<>();
        list = new LinkedList<>();
        stats = new Statistics(0,0,0,0,0,
                0,0, System.nanoTime(),0,0);
    }

    @Getter
    @AllArgsConstructor
    public class Node {
        private K key;
        private V value;
    }

    @Override
    public V get(K key) {
        stats.incOperations();
        stats.incRequests();
        Optional<Node> node = Optional.ofNullable(hashTable.get(key));
        if (node.isPresent()) {
            list.remove(node.get());
            list.add(node.get());
            stats.updateRates(HIT);
            stats.updateThroughput();
            return node.get().value;
        }
        stats.updateRates(MISS);
        stats.updateThroughput();
        return null;
    }

    @Override
    public void put(K key, V value) {
        stats.incOperations();
        if (hashTable.size() >= maxSize) {
            Node removed = list.removeFirst();
            hashTable.remove(removed.key);
            stats.decCacheSize();
        }
        Node added = new Node(key, value);
        list.add(added);
        hashTable.put(key, added);
        stats.incCacheSize();
//        stats.updateMemoryUsed(hashTable, list);
        stats.updateThroughput();
    }

    @Override
    public void invalidateAll() {
        hashTable.clear();
        list.clear();
        stats.invalidateCacheSize();
//        stats.updateMemoryUsed(hashTable, list);
        stats.updateThroughput();
    }
}
