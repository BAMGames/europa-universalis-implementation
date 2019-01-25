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
    /** A very little number, for floating number. */
    public static final double EPSILON = 0.0001d;
    /** One third. */
    public static final double THIRD = 1d / 3;
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
        return findFirst(list.stream(), predicate);
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
     * Add several Double that can be <code>null</code>.
     *
     * @param numbers to add.
     * @return the sum of the numbers.
     */
    public static Double add(Double... numbers) {
        Double sum = null;

        for (Double number : numbers) {
            if (sum == null) {
                sum = number;
            } else if (number != null) {
                sum = sum + number;
            }
        }

        return sum;
    }

    /**
     * @param first  first double.
     * @param second second double.
     * @return the minimum of the two doubles. A <code>null</code> double is considered to be 0.
     */
    public static Double min(Double first, Double second) {
        Double firstNotNull = first;
        Double secondNotNull = second;

        if (firstNotNull == null) {
            firstNotNull = 0d;
        }
        if (secondNotNull == null) {
            secondNotNull = 0d;
        }

        return Math.min(firstNotNull, secondNotNull);
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
     * Add a value to a specific key in a Map of K->Integer.
     *
     * @param map the map.
     * @param key the key.
     * @param add the value to add.
     * @param <K> the class of the key.
     */
    public static <K> void add(Map<K, Integer> map, K key, Integer add) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + add);
            } else {
                map.put(key, add);
            }
        }
    }

    /**
     * Add a value to a specific key in a Map of K->Double.
     *
     * @param map the map.
     * @param key the key.
     * @param add the value to add.
     * @param <K> the class of the key.
     */
    public static <K> void add(Map<K, Double> map, K key, Double add) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + add);
            } else {
                map.put(key, add);
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

    /**
     * Increment a Map of K->Long for a given key.
     *
     * @param map the map.
     * @param key the key.
     * @param <K> the class of the key.
     */
    public static <K> void addOneLong(Map<K, Long> map, K key) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + 1);
            } else {
                map.put(key, 1l);
            }
        }
    }

    /**
     * Add a value to a specific key in a Map of K->Long.
     *
     * @param map the map.
     * @param key the key.
     * @param add the value to add.
     * @param <K> the class of the key.
     */
    public static <K> void addLong(Map<K, Long> map, K key, Long add) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) + add);
            } else {
                map.put(key, add);
            }
        }
    }

    /**
     * Decrement a Map of K->Long for a given key.
     * If the value is 0, removes the key.
     * If the key doesn't exist, does nothing.
     *
     * @param map the map.
     * @param key the key.
     * @param <K> the class of the key.
     */
    public static <K> void subtractOneLong(Map<K, Long> map, K key) {
        if (map != null) {
            if (map.get(key) != null) {
                map.put(key, map.get(key) - 1);
                if (map.get(key) == 0) {
                    map.remove(key);
                }
            }
        }
    }

    /**
     * Subtract the values to the origin. Result cannot be negative (will return <code>0</code>).
     *
     * @param origin beginning value.
     * @param values to subtract.
     * @return the subtraction.
     */
    public static int subtract(Integer origin, Integer... values) {
        int returnValue = toInt(origin);

        if (values != null) {
            for (Integer value : values) {
                returnValue -= toInt(value);
            }
        }

        if (returnValue < 0) {
            returnValue = 0;
        }

        return returnValue;
    }

    /**
     * Transform an Integer to an int (<code>null</code> will be transformed to <code>0</code>).
     *
     * @param i the Integer.
     * @return the int.
     */
    public static int toInt(Integer i) {
        int returnValue = 0;

        if (i != null) {
            returnValue = i;
        }

        return returnValue;
    }
}
