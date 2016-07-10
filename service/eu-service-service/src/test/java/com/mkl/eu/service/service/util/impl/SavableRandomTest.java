package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.service.service.util.SavableRandom;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit test for SavableRandom.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class SavableRandomTest {

    @Test
    public void testRandom() {
        SavableRandom rand = new SavableRandom();
        rand.setSeed(System.nanoTime());

        int loop = rand.nextInt(1000);

        for (int i = 0; i < loop; i++) {
            rand.nextInt(10);
        }

        loop = rand.nextInt(15);
        Integer[] firtSequence = new Integer[loop];
        Integer[] resumeSequence = new Integer[loop];

        long seed = rand.getSeed();

        for (int i = 0; i < loop; i++) {
            firtSequence[i] = rand.nextInt(10);
        }

        SavableRandom otherRand = new SavableRandom();
        otherRand.setSeed(seed);

        for (int i = 0; i < loop; i++) {
            resumeSequence[i] = otherRand.nextInt(10);
        }

        Assert.assertArrayEquals(firtSequence, resumeSequence);
    }
}
