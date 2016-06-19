package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.tables.Tech;
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
    public void testSizeFromType() {
        Assert.assertEquals(0, CounterUtil.getSizeFromType(null));
        Assert.assertEquals(0, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));

        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_GALLEY));

        Assert.assertEquals(2, CounterUtil.getSizeFromType(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(2, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARMY_MINUS));

        Assert.assertEquals(4, CounterUtil.getSizeFromType(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(4, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARMY_PLUS));
    }

    @Test
    public void testFortressLevel() {
        Assert.assertEquals(0, CounterUtil.getFortressLevelFromType(null));
        Assert.assertEquals(0, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARMY_PLUS));

        Assert.assertEquals(0, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORT));
        Assert.assertEquals(0, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_0_ST_PETER));

        Assert.assertEquals(1, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORTRESS_1));
        Assert.assertEquals(1, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));

        Assert.assertEquals(2, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORTRESS_2));
        Assert.assertEquals(2, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_2_ST_PETER));
        Assert.assertEquals(2, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_2));
        Assert.assertEquals(2, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR));
        Assert.assertEquals(2, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL));

        Assert.assertEquals(3, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORTRESS_3));
        Assert.assertEquals(3, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_3_ST_PETER));
        Assert.assertEquals(3, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_3));
        Assert.assertEquals(3, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_3_GIBRALTAR));
        Assert.assertEquals(3, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_3_SEBASTOPOL));

        Assert.assertEquals(4, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORTRESS_4));
        Assert.assertEquals(4, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_4_ST_PETER));
        Assert.assertEquals(4, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_4));

        Assert.assertEquals(5, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.FORTRESS_5));
        Assert.assertEquals(5, CounterUtil.getFortressLevelFromType(CounterFaceTypeEnum.ARSENAL_5_ST_PETER));
    }

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

    @Test
    public void testNeutralTech() {
        Assert.assertFalse(CounterUtil.isNeutralTechnology(null));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_RENAISSANCE));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_ARQUEBUS));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_MUSKET));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_BAROQUE));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_MANOEUVRE));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_LACE_WAR));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_NAE_GALEON));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_GALLEON_FLUYT));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_BATTERY));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_VESSEL));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_THREE_DECKER));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_SEVENTY_FOUR));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_TERCIO));
        Assert.assertTrue(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.TECH_GALLEASS));
        Assert.assertFalse(CounterUtil.isNeutralTechnology(CounterFaceTypeEnum.ARMY_MINUS));
    }

    @Test
    public void testTechName() {
        Assert.assertEquals(null, CounterUtil.getTechnologyName(null));
        Assert.assertEquals(null, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_LAND));
        Assert.assertEquals(Tech.RENAISSANCE, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_RENAISSANCE));
        Assert.assertEquals(Tech.ARQUEBUS, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_ARQUEBUS));
        Assert.assertEquals(Tech.MUSKET, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_MUSKET));
        Assert.assertEquals(Tech.BAROQUE, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_BAROQUE));
        Assert.assertEquals(Tech.MANOEUVRE, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_MANOEUVRE));
        Assert.assertEquals(Tech.LACE_WAR, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_LACE_WAR));
        Assert.assertEquals(Tech.NAE_GALEON, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_NAE_GALEON));
        Assert.assertEquals(Tech.GALLEON_FLUYT, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_GALLEON_FLUYT));
        Assert.assertEquals(Tech.BATTERY, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_BATTERY));
        Assert.assertEquals(Tech.VESSEL, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_VESSEL));
        Assert.assertEquals(Tech.THREE_DECKER, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_THREE_DECKER));
        Assert.assertEquals(Tech.SEVENTY_FOUR, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_SEVENTY_FOUR));
        Assert.assertEquals(Tech.TERCIO, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_TERCIO));
        Assert.assertEquals(Tech.GALLEASS, CounterUtil.getTechnologyName(CounterFaceTypeEnum.TECH_GALLEASS));
    }
}
