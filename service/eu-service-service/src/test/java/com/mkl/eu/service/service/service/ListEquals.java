package com.mkl.eu.service.service.service;

import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Matcher for mockito that sort List before matching them.
 *
 * @author MKL
 */
public class ListEquals<T extends Comparable<T>> extends ArgumentMatcher<List<T>> {
    /** Object wanted. */
    private final List<T> wanted;

    /**
     * Constructor.
     * @param wanted the wanted object.
     */
    public ListEquals(List<T> wanted) {
        this.wanted = wanted;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(Object o) {
        if (o instanceof List) {
            //noinspection unchecked
            List<T> actual = (List<T>)o;

            List<T> wantedOrd = new ArrayList<>(wanted);
            Collections.sort(wantedOrd);
            List<T> actualOrd = new ArrayList<>(actual);
            Collections.sort(actualOrd);

            return wantedOrd.equals(actualOrd);
        }

        return false;
    }
}
