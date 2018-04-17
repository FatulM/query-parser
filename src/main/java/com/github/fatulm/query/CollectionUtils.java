package com.github.fatulm.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Some collection utilities
 */
public class CollectionUtils {
    private CollectionUtils() {
    }

    /**
     * Implementation for {@code List}
     *
     * @return new {@code List}
     */
    public static List<String> listImpl() {
        return new ArrayList<>();
    }

    /**
     * Implementation for {@code Map}
     *
     * @return new {@code Map}
     */
    public static Map<String, List<String>> mapImpl() {
        return new HashMap<>();
    }
}
