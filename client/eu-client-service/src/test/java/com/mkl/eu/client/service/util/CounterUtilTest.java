package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit tests for CounterUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CounterUtilTest {

    @Test
    public void testManufacture() {
        Assert.assertFalse(CounterUtil.isManufacture(null));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_ART_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_CEREALS_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_CEREALS_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_CLOTHES_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_CLOTHES_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_FISH_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_FISH_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_METAL_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_METAL_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_SALT_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_SALT_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_WINE_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_WINE_PLUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_WOOD_MINUS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterFaceTypeEnum.MNU_WOOD_PLUS));
        Assert.assertFalse(CounterUtil.isManufacture(CounterFaceTypeEnum.ARMY_MINUS));
    }

    @Test
    public void testManufactureLevel() {
        Assert.assertEquals(0, CounterUtil.getManufactureLevel(null));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_ART_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_CEREALS_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_CEREALS_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_CLOTHES_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_CLOTHES_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_FISH_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_FISH_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_METAL_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_METAL_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_SALT_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_SALT_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_WINE_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_WINE_PLUS));
        Assert.assertEquals(1, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_WOOD_MINUS));
        Assert.assertEquals(2, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.MNU_WOOD_PLUS));
        Assert.assertEquals(0, CounterUtil.getManufactureLevel(CounterFaceTypeEnum.ARMY_MINUS));
    }

    @Test
    public void testArsenal() {
        Assert.assertFalse(CounterUtil.isArsenal(null));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_0_ST_PETER));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_2));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_2_ST_PETER));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_3));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_3_ST_PETER));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_3_SEBASTOPOL));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_3_GIBRALTAR));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_4));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_4_ST_PETER));
        Assert.assertTrue(CounterUtil.isArsenal(CounterFaceTypeEnum.ARSENAL_5_ST_PETER));
        Assert.assertFalse(CounterUtil.isArsenal(CounterFaceTypeEnum.ARMY_MINUS));
    }

    @Test
    public void testForce() {
        Assert.assertFalse(CounterUtil.isForce(null));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.FORT));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertTrue(CounterUtil.isForce(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));
        Assert.assertFalse(CounterUtil.isForce(CounterFaceTypeEnum.ARSENAL_2));
    }
}
