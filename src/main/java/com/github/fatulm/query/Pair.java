package com.github.fatulm.query;

import java.util.Map;
import java.util.function.Function;

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

    public static <T, R, K extends T, V> Function<Pair<K, V>, Pair<R, V>> keyMap(Function<T, R> func) {
        return pair -> new Pair<>(func.apply(pair.getKey()), pair.getValue());
    }

    public static <T, R, K, V extends T> Function<Pair<K, V>, Pair<K, R>> valueMap(Function<T, R> func) {
        return pair -> new Pair<>(pair.getKey(), func.apply(pair.getValue()));
    }

    /**
     * Operation not supported
     */
    @Override
    public V setValue(V value) {
        throw new RuntimeException("Operation not supported");
    }

    public static <T, R, K extends T, V extends T> Function<Pair<K, V>, Pair<R, R>> keyValueMap(Function<T, R> func) {
        return pair -> new Pair<>(func.apply(pair.getKey()), func.apply(pair.getValue()));
    }

    /**
     * @return key
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * @return value
     */
    @Override
    public V getValue() {
        return value;
    }
}
