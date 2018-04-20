package com.github.fatulm.query;

import java.util.Map;

/**
 * Simple immutable Key-Value holder class
 * (implementation of Map.Entry)
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class Pair<K, V> implements Map.Entry<K, V> {
    private final K key;
    private final V value;

    /**
     * @param key   key
     * @param value value
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }


    /**
     * @param entry key and value
     */
    public Pair(Map.Entry<K, V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    /**
     * @return key
     */
    public K getKey() {
        return key;
    }

    /**
     * @return value
     */
    public V getValue() {
        return value;
    }

    /**
     * Operation not supported
     */
    @Override
    public V setValue(V value) {
        throw new RuntimeException("Operation not supported");
    }
}
