package org.product.impl;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.product.Statistics;
import org.product.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.product.states.State.HIT;
import static org.product.states.State.MISS;

@Getter
@Setter
@Component
public class SplayTree<K, V> implements Cache<K, V> {
    @Value("${cache.maxCacheSize}")
    private long maxSize;

    private Node root;
    private Statistics stats;
    private long minFreq;
    private Node minNode;

    public SplayTree() {
        stats = new Statistics(0,0,0,0,0,
                0,0, System.nanoTime(),0,0);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public class Node<K extends Comparable<K>> {
        private K key;
        private V value;
        private Node parent;
        private Node left;
        private Node right;
        private long freq;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    @Override
    public V get(K key) {
        stats.incOperations();
        stats.incRequests();
        Node current = root;
        while (current != null && ((Comparable) key).compareTo(current.key) != 0) {
            if (((Comparable) key).compareTo(current.key) < 0)
                current = current.left;
            else
                current = current.right;
        }
        if (current == null) {
            stats.updateRates(MISS);
            stats.updateThroughput();
            return null;
        }
        current.freq++;
        splay(current);
        stats.updateRates(HIT);
        stats.updateThroughput();

        return (V) current.value;
    }

    @Override
    public void put(K key, V value) {
        stats.incOperations();
        Node inserted = new Node((Comparable) key, value);
        Node current = root;
        Node parent = null;

        if (stats.getCacheSize() >= maxSize) {
            current = evict(current);
            stats.decCacheSize();
        }

        while (current != null) {
            parent = current;
            if (inserted.key.compareTo(current.key) < 0)
                current = current.left;
            else
                current = current.right;
        }

        inserted.parent = parent;
        if (parent == null)
            root = inserted;
        else if (inserted.key.compareTo(parent.key) < 0)
            parent.left = inserted;
        else
            parent.right = inserted;

        splay(inserted);

        stats.incCacheSize();
//        stats.updateMemoryUsed(root);
        stats.updateThroughput();
    }

    @Override
    public void invalidateAll() {
        root = null;
        stats.invalidateCacheSize();
//        stats.updateMemoryUsed(root);
        stats.updateThroughput();
    }

    private void splay(Node node) {
        while (node.parent != null) {
            if (node.parent.parent == null) {
                if (node.parent.left == node)
                    zig(node.parent);
                else
                    zag(node.parent);
            }
            else if (node.parent.left == node && node.parent.parent.left == node.parent) {
                zig(node.parent.parent);
                zig(node.parent);
            }
            else if (node.parent.right == node && node.parent.parent.right == node.parent) {
                zag(node.parent.parent);
                zag(node.parent);
            }
            else if (node.parent.left == node && node.parent.parent.right == node.parent) {
                zig(node.parent);
                zag(node.parent);
            }
            else {
                zag(node.parent);
                zig(node.parent);
            }
        }
    }

    private void zig(Node node) {
        Node leftNode = node.left;
        node.left = leftNode.right;
        if (leftNode.right != null)
            leftNode.right.parent = node;
        leftNode.parent = node.parent;
        updateChildren(node, leftNode);
        leftNode.right = node;
        node.parent = leftNode;
    }

    private void zag(Node node) {
        Node rightNode = node.right;
        node.right = rightNode.left;
        if (rightNode.left != null)
            rightNode.left.parent = node;
        rightNode.parent = node.parent;
        updateChildren(node, rightNode);
        rightNode.left = node;
        node.parent = rightNode;
    }

    private void updateChildren(Node node, Node child) {
        if (node.parent == null)
            root = child;
        else if (node.parent.left == node)
            node.parent.left = child;
        else
            node.parent.right = child;
    }

    private Node evict(Node current) {
        minFreq = maxSize;
        searchNodeToEvict(current);
        current = minNode;

        splay(current);

        if (current.left == null)
            current = current.right;
        else if (current.right == null)
            current = current.left;
        else {
            Node leftTree = current.left;
            leftTree.parent = null;
            Node rightTree = current.right;

            while (leftTree.right != null)
                leftTree = leftTree.right;

            splay(leftTree);

            leftTree.right = rightTree;
            rightTree.parent = leftTree;
            current = leftTree;
        }

        current.parent = null;
        return current;
    }

    public void searchNodeToEvict(Node current) {
        if (current == null) return;
        if (current.freq < minFreq) {
            minFreq = current.freq;
            minNode = current;
        }
        searchNodeToEvict(current.left);
        searchNodeToEvict(current.right);
    }
}
