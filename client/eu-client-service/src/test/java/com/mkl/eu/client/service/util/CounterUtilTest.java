package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CultureEnum;
import com.mkl.eu.client.service.vo.enumeration.EstablishmentTypeEnum;
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
    public void testFortress() {
        Assert.assertEquals(false, CounterUtil.isFortress(null));
        Assert.assertEquals(false, CounterUtil.isFortress(CounterFaceTypeEnum.ARMY_PLUS));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORT));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_0_ST_PETER));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORTRESS_1));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORTRESS_2));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_2_ST_PETER));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_2));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORTRESS_3));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_3_ST_PETER));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_3));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_3_GIBRALTAR));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_3_SEBASTOPOL));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORTRESS_4));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_4_ST_PETER));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_4));

        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.FORTRESS_5));
        Assert.assertEquals(true, CounterUtil.isFortress(CounterFaceTypeEnum.ARSENAL_5_ST_PETER));
    }

    @Test
    public void testArmy() {
        Assert.assertEquals(false, CounterUtil.isArmy(null));
        Assert.assertEquals(false, CounterUtil.isArmy(CounterFaceTypeEnum.FORTRESS_3));

        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));

        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(true, CounterUtil.isArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
    }

    @Test
    public void testLandArmy() {
        Assert.assertEquals(false, CounterUtil.isLandArmy(null));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.FORTRESS_3));

        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isLandArmy(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));

        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(false, CounterUtil.isLandArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
    }

    @Test
    public void testNavalArmy() {
        Assert.assertEquals(false, CounterUtil.isNavalArmy(null));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.FORTRESS_3));

        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(false, CounterUtil.isNavalArmy(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));

        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(true, CounterUtil.isNavalArmy(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
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

        Assert.assertEquals(null, CounterUtil.getManufactureLevel1(null));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_ART_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_ART_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_ART_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CEREALS_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_CEREALS_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CEREALS_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_CEREALS_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CLOTHES_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_CLOTHES_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CLOTHES_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_CLOTHES_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_FISH_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_FISH_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_FISH_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_FISH_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_METAL_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_METAL_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_SALT_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_SALT_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_SALT_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_SALT_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WINE_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_WINE_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WINE_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_WINE_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WOOD_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_WOOD_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WOOD_MINUS, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.MNU_WOOD_PLUS));
        Assert.assertEquals(null, CounterUtil.getManufactureLevel1(CounterFaceTypeEnum.ARMY_MINUS));

        Assert.assertEquals(null, CounterUtil.getManufactureLevel2(null));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_ART_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_ART_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_ART_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CEREALS_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_CEREALS_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CEREALS_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_CEREALS_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CLOTHES_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_CLOTHES_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CLOTHES_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_CLOTHES_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_FISH_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_FISH_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_FISH_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_FISH_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_METAL_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_METAL_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_SALT_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_SALT_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_SALT_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_SALT_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WINE_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_WINE_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WINE_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_WINE_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WOOD_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_WOOD_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WOOD_PLUS, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.MNU_WOOD_PLUS));
        Assert.assertEquals(null, CounterUtil.getManufactureLevel2(CounterFaceTypeEnum.ARMY_MINUS));
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

    @Test
    public void testTechType() {
        Assert.assertEquals(null, CounterUtil.getTechnologyType(null));
        Assert.assertEquals(null, CounterUtil.getTechnologyType("toto"));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_RENAISSANCE, CounterUtil.getTechnologyType(Tech.RENAISSANCE));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_ARQUEBUS, CounterUtil.getTechnologyType(Tech.ARQUEBUS));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_MUSKET, CounterUtil.getTechnologyType(Tech.MUSKET));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_BAROQUE, CounterUtil.getTechnologyType(Tech.BAROQUE));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_MANOEUVRE, CounterUtil.getTechnologyType(Tech.MANOEUVRE));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LACE_WAR, CounterUtil.getTechnologyType(Tech.LACE_WAR));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAE_GALEON, CounterUtil.getTechnologyType(Tech.NAE_GALEON));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, CounterUtil.getTechnologyType(Tech.GALLEON_FLUYT));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_BATTERY, CounterUtil.getTechnologyType(Tech.BATTERY));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_VESSEL, CounterUtil.getTechnologyType(Tech.VESSEL));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_THREE_DECKER, CounterUtil.getTechnologyType(Tech.THREE_DECKER));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, CounterUtil.getTechnologyType(Tech.SEVENTY_FOUR));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_TERCIO, CounterUtil.getTechnologyType(Tech.TERCIO));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_GALLEASS, CounterUtil.getTechnologyType(Tech.GALLEASS));
    }

    @Test
    public void testTechGroup() {
        Assert.assertEquals(null, CounterUtil.getTechnologyGroup(null, true));
        Assert.assertEquals(null, CounterUtil.getTechnologyGroup(null, false));
        Assert.assertEquals(null, CounterUtil.getTechnologyGroup(CultureEnum.MEDIEVAL, true));
        Assert.assertEquals(null, CounterUtil.getTechnologyGroup(CultureEnum.MEDIEVAL, false));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND_LATIN, CounterUtil.getTechnologyGroup(CultureEnum.LATIN, true));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAVAL_LATIN, CounterUtil.getTechnologyGroup(CultureEnum.LATIN, false));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND_ISLAM, CounterUtil.getTechnologyGroup(CultureEnum.ISLAM, true));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, CounterUtil.getTechnologyGroup(CultureEnum.ISLAM, false));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, CounterUtil.getTechnologyGroup(CultureEnum.ORTHODOX, true));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, CounterUtil.getTechnologyGroup(CultureEnum.ORTHODOX, false));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_LAND_ASIA, CounterUtil.getTechnologyGroup(CultureEnum.ROTW, true));
        Assert.assertEquals(CounterFaceTypeEnum.TECH_NAVAL_ASIA, CounterUtil.getTechnologyGroup(CultureEnum.ROTW, false));
    }

    @Test
    public void testStackTech() {
        Assert.assertTrue(CounterUtil.canTechnologyStack(null, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, true));

        Assert.assertTrue(CounterUtil.canTechnologyStack(null, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, false));
    }

    @Test
    public void testEstablishment() {
        Assert.assertFalse(CounterUtil.isEstablishment(null));
        Assert.assertFalse(CounterUtil.isEstablishment(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.COLONY_MINUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.COLONY_PLUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_MINUS));
        Assert.assertTrue(CounterUtil.isEstablishment(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_PLUS));

        Assert.assertNull(CounterUtil.getEstablishmentType((CounterFaceTypeEnum) null));
        Assert.assertNull(CounterUtil.getEstablishmentType(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(EstablishmentTypeEnum.TRADING_POST, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertEquals(EstablishmentTypeEnum.TRADING_POST, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertEquals(EstablishmentTypeEnum.COLONY, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.COLONY_MINUS));
        Assert.assertEquals(EstablishmentTypeEnum.COLONY, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.COLONY_PLUS));
        Assert.assertEquals(EstablishmentTypeEnum.MINOR_ESTABLISHMENT, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_MINUS));
        Assert.assertEquals(EstablishmentTypeEnum.MINOR_ESTABLISHMENT, CounterUtil.getEstablishmentType(CounterFaceTypeEnum.MINOR_ESTABLISHMENT_PLUS));

        Assert.assertNull(CounterUtil.getEstablishmentType((AdminActionTypeEnum) null));
        Assert.assertNull(CounterUtil.getEstablishmentType(AdminActionTypeEnum.ELT));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_MINUS, CounterUtil.getEstablishmentType(AdminActionTypeEnum.TP));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_MINUS, CounterUtil.getEstablishmentType(AdminActionTypeEnum.COL));

        Assert.assertNull(CounterUtil.getFacePlus(null));
        Assert.assertNull(CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.COLONY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.COLONY_PLUS));
    }
}
