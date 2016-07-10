package com.mkl.eu.service.service.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XORShift implementation of random numbers with the possibility to resume a sequence.
 *
 * @author MKL.
 */
public class SavableRandom extends Random {

    /**
     * The internal state associated with this pseudorandom number generator.
     * (The specs for the methods in this class describe the ongoing
     * computation of this value.)
     */
    private AtomicLong seed = new AtomicLong();

    /** {@inheritDoc} */
    @Override
    protected int next(int bits) {
        long newSeed = seed.get();

        newSeed ^= (newSeed << 21);
        newSeed ^= (newSeed >>> 35);
        newSeed ^= (newSeed << 4);
        seed.set(newSeed);

        return (int) (newSeed >>> (48 - bits));
    }

    /** @return the seed. */
    public long getSeed() {
        return seed.get();
    }

    /** @param seed the seed to set. */
    @Override
    public void setSeed(long seed) {
        if (this.seed != null) {
            this.seed.set(seed);
        }
    }
}
