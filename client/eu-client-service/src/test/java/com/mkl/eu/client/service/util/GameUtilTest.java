package com.mkl.eu.client.service.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit tests for GameUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class GameUtilTest {
    @Test
    public void testStability() {
        Assert.assertNull(GameUtil.getStability(null));
        Assert.assertNull(GameUtil.getStability(""));
        Assert.assertNull(GameUtil.getStability("eIdf"));
        Assert.assertEquals(new Integer(0), GameUtil.getStability("B_STAB_0"));
        Assert.assertEquals(new Integer(3), GameUtil.getStability("B_STAB_3"));
        Assert.assertEquals(new Integer(-3), GameUtil.getStability("B_STAB_-3"));

        Assert.assertEquals("B_STAB_0", GameUtil.getStabilityBox(0));
        Assert.assertEquals("B_STAB_3", GameUtil.getStabilityBox(3));
        Assert.assertEquals("B_STAB_-3", GameUtil.getStabilityBox(-3));
    }

    @Test
    public void testIsStability() {
        Assert.assertFalse(GameUtil.isStabilityBox(null));
        Assert.assertFalse(GameUtil.isStabilityBox(""));
        Assert.assertFalse(GameUtil.isStabilityBox("eIdf"));
        Assert.assertTrue(GameUtil.isStabilityBox("B_STAB_0"));
        Assert.assertTrue(GameUtil.isStabilityBox("B_STAB_3"));
        Assert.assertTrue(GameUtil.isStabilityBox("B_STAB_-3"));
    }

    @Test
    public void testInflation() {
        Assert.assertEquals(new Integer(0), GameUtil.getInflation(null, false));
        Assert.assertEquals(new Integer(0), GameUtil.getInflation("", false));
        Assert.assertEquals(new Integer(0), GameUtil.getInflation("eIdf", false));

        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_0G", false));
        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_0D", false));
        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_1G", false));
        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_1D", false));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_2G", false));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_2D", false));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_3G", false));
        Assert.assertEquals(new Integer(20), GameUtil.getInflation("B_PB_3D", false));
        Assert.assertEquals(new Integer(20), GameUtil.getInflation("B_PB_4G", false));
        Assert.assertEquals(new Integer(25), GameUtil.getInflation("B_PB_4D", false));
        Assert.assertEquals(new Integer(0), GameUtil.getInflation("B_PB_5D", false));

        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_0G", true));
        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_0D", true));
        Assert.assertEquals(new Integer(5), GameUtil.getInflation("B_PB_1G", true));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_1D", true));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_2G", true));
        Assert.assertEquals(new Integer(10), GameUtil.getInflation("B_PB_2D", true));
        Assert.assertEquals(new Integer(20), GameUtil.getInflation("B_PB_3G", true));
        Assert.assertEquals(new Integer(20), GameUtil.getInflation("B_PB_3D", true));
        Assert.assertEquals(new Integer(25), GameUtil.getInflation("B_PB_4G", true));
        Assert.assertEquals(new Integer(33), GameUtil.getInflation("B_PB_4D", true));
        Assert.assertEquals(new Integer(0), GameUtil.getInflation("B_PB_5G", true));
    }

    @Test
    public void testIsInflation() {
        Assert.assertFalse(GameUtil.isInflationBox(null));
        Assert.assertFalse(GameUtil.isInflationBox(""));
        Assert.assertFalse(GameUtil.isInflationBox("eIdf"));
        Assert.assertTrue(GameUtil.isInflationBox("B_PB_0D"));
        Assert.assertTrue(GameUtil.isInflationBox("B_PB_1G"));
        Assert.assertTrue(GameUtil.isInflationBox("B_PB_3D"));
    }

    @Test
    public void testTechBox() {
        Assert.assertEquals(null, GameUtil.getTechnologyBox(null));
        Assert.assertEquals(null, GameUtil.getTechnologyBox(""));
        Assert.assertEquals(null, GameUtil.getTechnologyBox("21"));
        Assert.assertEquals(0, GameUtil.getTechnologyBox("B_TECH_0").intValue());
        Assert.assertEquals(5, GameUtil.getTechnologyBox("B_TECH_5").intValue());
        Assert.assertEquals(25, GameUtil.getTechnologyBox("B_TECH_25").intValue());
        Assert.assertEquals(60, GameUtil.getTechnologyBox("B_TECH_60").intValue());
    }

    @Test
    public void testRotwProvince() {
        Assert.assertEquals(false, GameUtil.isRotwProvince(null));
        Assert.assertEquals(false, GameUtil.isRotwProvince(""));
        Assert.assertEquals(false, GameUtil.isRotwProvince("eTangers"));
        Assert.assertEquals(true, GameUtil.isRotwProvince("rTangers"));
    }
}
