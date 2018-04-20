package com.github.fatulm.query;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Some utilities needed for lambdas
 */
public class LambdaUtils {
    private LambdaUtils() {
    }

    /**
     * Elvis operator implementation for java
     *
     * @param <T>      input type
     * @param <R>      output type
     * @param function input function
     * @return null if input is null and function if input is not null
     */
    public static <T, R> Function<T, R> elvis(Function<T, R> function) {
        return t -> t == null ? null : function.apply(t);
    }

    /**
     * Function returning null
     *
     * @param <T> input type
     * @param <R> output type
     * @return null
     */
    public static <T, R> Function<T, R> toNull() {
        return t -> null;
    }

    /**
     * Mapping if input matches with some criteria or returning itself
     *
     * @param predicate predicate for checking if you want to map to something else
     * @param function  input function
     * @param <T>       input and output type
     * @return function which returns input or function of input if matches with predicate
     */
    public static <T> Function<T, T> mapIf(Predicate<T> predicate, Function<T, ? extends T> function) {
        return t -> predicate.test(t) ? function.apply(t) : t;
    }
}
