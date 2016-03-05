package com.mkl.eu.client.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Various utility method.
 *
 * @author MKL.
 */
public final class CommonUtil {
    /**
     * No instance.
     */
    private CommonUtil() {

    }

    /**
     * Find the first element of the collection that matches the predicate, or <code>null</code> if none matches.
     *
     * @param list      collection to parse.
     * @param predicate to use for matching purpose.
     * @param <T>       Type of the Collection.
     * @return the first element of the collection matching the predicate.
     */
    public static <T> T findFirst(Collection<T> list, Predicate<T> predicate) {
        T returnValue = null;
        Optional<T> opt = list.stream().filter(predicate).findFirst();
        if (opt.isPresent()) {
            returnValue = opt.get();
        }

        return returnValue;
    }

    /**
     * Find the first element of the stream that matches the predicate, or <code>null</code> if none matches.
     *
     * @param stream    stream to parse.
     * @param predicate to use for matching purpose.
     * @param <T>       Type of the Collection.
     * @return the first element of the collection matching the predicate.
     */
    public static <T> T findFirst(Stream<T> stream, Predicate<T> predicate) {
        T returnValue = null;
        Optional<T> opt = stream.filter(predicate).findFirst();
        if (opt.isPresent()) {
            returnValue = opt.get();
        }

        return returnValue;
    }

    /**
     * Add several Integer that can be <code>null</code>.
     *
     * @param numbers to add.
     * @return the sum of the numbers.
     */
    public static Integer add(Integer... numbers) {
        Integer sum = null;

        for (Integer number : numbers) {
            if (sum == null) {
                sum = number;
            } else if (number != null) {
                sum = sum + number;
            }
        }

        return sum;
    }

    /**
     * Increment a Map of K->Integer for a given key.
     *
     * @param map the map.
     * @param key the key.
     * @param <K> the class of the key.
     */
    public static <K> void addOne(Map<K, Integer> map, K key) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1);
            }
        }
    }

    /**
     * Decrement a Map of K->Integer for a given key.
     * If the value is 0, removes the key.
     * If the key doesn't exist, does nothing.
     *
     * @param map the map.
     * @param key the key.
     * @param <K> the class of the key.
     */
    public static <K> void subtractOne(Map<K, Integer> map, K key) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) - 1);
                if (map.get(key) == 0) {
                    map.remove(key);
                }
            }
        }
    }
}
