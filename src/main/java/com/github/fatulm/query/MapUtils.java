package com.github.fatulm.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for {@code Map}
 */
class MapUtils {
    private MapUtils() {
    }

    /**
     * @return unmodifiable map with non null keys
     */
    public static <K, V> Map<K, V> unmodifiableNonNullKeyMap(Map<K, V> map) {
        return Collections.unmodifiableMap(new HashMap<K, V>(map) {
            @Override
            public V get(Object key) {
                return super.get(requireKeyNonNull(key));
            }

            @Override
            public boolean containsKey(Object key) {
                return super.containsKey(requireKeyNonNull(key));
            }
        });
    }

    /**
     * @return itself
     * @throws NullPointerException if key is null
     */
    private static Object requireKeyNonNull(Object key) {
        if (key == null)
            throw new NullPointerException("key can not be null");
        return key;
    }
}
