package org.example.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.Statistics;
import org.example.cache.Cache;

@Getter
@Setter
public class SplayTree<K, V> implements Cache<K, V> {
    private Node root;
    private long maxSize;
    private Statistics stats;

    @AllArgsConstructor
    public class Node<K extends Comparable<K>> {
        private K key;
        private V value;
        private Node parent;
        private Node left;
        private Node right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    @Override
    public V get(K key) {
        Node current = root;
        while (current != null && key != current.key) {
            if (((Comparable) key).compareTo(current.key) < 0)
                current = current.left;
            else
                current = current.right;
        }
        if (current == null)
            return null;

        splay(current);

        return (V) current.value;
    }

    @Override
    public void put(K key, V value) {
        Node inserted = new Node((Comparable) key, value);
        Node current = root;
        Node parent = null;

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
    }

    @Override
    public void invalidateAll() {
        root = null;
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
            else if (node.parent.right == node && node.parent.parent.right == node.right) {
                zag(node.parent.parent);
                zag(node.parent);
            }
            else if (node.parent.right == node && node.parent.parent.left == node.parent) {
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
        if (node.left != null)
            node.left.parent = node;
        updateChildren(node, leftNode);
        leftNode.parent = node.parent;
        leftNode.right = node;
        node.parent = leftNode;
    }
    private void zag(Node node) {
        Node rightNode = node.right;
        node.right = rightNode.left;
        if (node.right != null)
            node.right.parent = node;
        updateChildren(node, rightNode);
        rightNode.parent = node.parent;
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
}
