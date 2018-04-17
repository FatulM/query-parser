package com.github.fatulm.query;

/**
 * Simple immutable Key-Value holder class
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class Pair<K, V> {
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
}
