package com.mkl.eu.client.service.util;

import java.util.stream.Stream;

/**
 * Classes from Mario fusco on his speak about trampolines.
 *
 * @author MKL.
 */
public interface TailCall<T> {
    TailCall<T> apply();

    default boolean isComplete() {
        return false;
    }

    default T result() {
        throw new UnsupportedOperationException();
    }

    default T invoke() {
        return Stream.iterate(this, TailCall::apply)
                .filter(TailCall::isComplete)
                .findFirst()
                .get()
                .result();
    }

    static <T> TailCall<T> done(final T value) {
        return new TailCall<T>() {
            @Override
            public boolean isComplete() {
                return true;
            }

            @Override
            public T result() {
                return value;
            }

            @Override
            public TailCall<T> apply() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
