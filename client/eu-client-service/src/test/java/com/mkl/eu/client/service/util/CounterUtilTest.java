package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.client.service.vo.tables.Tech;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static com.mkl.eu.client.common.util.CommonUtil.EPSILON;
import static com.mkl.eu.client.common.util.CommonUtil.THIRD;

/**
 * Unit tests for CounterUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CounterUtilTest {

    @Test
    public void testSizeFromType() {
        Assert.assertEquals(0, CounterUtil.getSizeFromType(null), EPSILON);
        Assert.assertEquals(0, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER), EPSILON);

        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_DETACHMENT), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_TRANSPORT), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_GALLEY), EPSILON);
        Assert.assertEquals(1, CounterUtil.getSizeFromType(CounterFaceTypeEnum.PACHA_1), EPSILON);

        Assert.assertEquals(2, CounterUtil.getSizeFromType(CounterFaceTypeEnum.FLEET_MINUS), EPSILON);
        Assert.assertEquals(2, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARMY_MINUS), EPSILON);
        Assert.assertEquals(2, CounterUtil.getSizeFromType(CounterFaceTypeEnum.PACHA_2), EPSILON);

        Assert.assertEquals(3, CounterUtil.getSizeFromType(CounterFaceTypeEnum.PACHA_3), EPSILON);

        Assert.assertEquals(4, CounterUtil.getSizeFromType(CounterFaceTypeEnum.FLEET_PLUS), EPSILON);
        Assert.assertEquals(4, CounterUtil.getSizeFromType(CounterFaceTypeEnum.ARMY_PLUS), EPSILON);

        Assert.assertEquals(THIRD, CounterUtil.getSizeFromType(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION), EPSILON);
        Assert.assertEquals(THIRD, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION), EPSILON);
        Assert.assertEquals(THIRD, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK), EPSILON);
        Assert.assertEquals(THIRD, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION), EPSILON);
        Assert.assertEquals(THIRD, CounterUtil.getSizeFromType(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION), EPSILON);

        Assert.assertEquals(null, CounterUtil.getSize2FromType(null));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, CounterUtil.getSize2FromType(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(null, CounterUtil.getSize2FromType(CounterFaceTypeEnum.NAVAL_GALLEY));

        Assert.assertEquals(null, CounterUtil.getSize1FromType(null));
        Assert.assertEquals(null, CounterUtil.getSize1FromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, CounterUtil.getSize1FromType(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, CounterUtil.getSize1FromType(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_INDIAN, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_INDIAN, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_SEPOY, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_SEPOY, CounterUtil.getSize1FromType(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_TRANSPORT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_TRANSPORT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_TRANSPORT, CounterUtil.getSize1FromType(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_GALLEY, CounterUtil.getSize1FromType(CounterFaceTypeEnum.NAVAL_GALLEY));

        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(null));
        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.ARSENAL_1_ST_PETER));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(null, CounterUtil.getSizeThirdFromType(CounterFaceTypeEnum.NAVAL_GALLEY));
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

        Assert.assertEquals(0, CounterUtil.getFortressesFromLevel(-1).size());
        Assert.assertEquals(0, CounterUtil.getFortressesFromLevel(6).size());

        Assert.assertEquals(1, CounterUtil.getFortressesFromLevel(0).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORT, CounterUtil.getFortressesFromLevel(0).get(0));

        Assert.assertEquals(1, CounterUtil.getFortressesFromLevel(1).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_1, CounterUtil.getFortressesFromLevel(1).get(0));

        Assert.assertEquals(2, CounterUtil.getFortressesFromLevel(2).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_2, CounterUtil.getFortressesFromLevel(2).get(0));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_2, CounterUtil.getFortressesFromLevel(2).get(1));

        Assert.assertEquals(2, CounterUtil.getFortressesFromLevel(3).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_3, CounterUtil.getFortressesFromLevel(3).get(0));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_3, CounterUtil.getFortressesFromLevel(3).get(1));

        Assert.assertEquals(2, CounterUtil.getFortressesFromLevel(4).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_4, CounterUtil.getFortressesFromLevel(4).get(0));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_4, CounterUtil.getFortressesFromLevel(4).get(1));

        Assert.assertEquals(1, CounterUtil.getFortressesFromLevel(5).size());
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_5, CounterUtil.getFortressesFromLevel(5).get(0));

        Assert.assertEquals(CounterFaceTypeEnum.FORT, CounterUtil.getFortressesFromLevel(0, false));
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_1, CounterUtil.getFortressesFromLevel(1, false));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_2, CounterUtil.getFortressesFromLevel(2, true));
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_2, CounterUtil.getFortressesFromLevel(2, false));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_3, CounterUtil.getFortressesFromLevel(3, true));
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_3, CounterUtil.getFortressesFromLevel(3, false));
        Assert.assertEquals(CounterFaceTypeEnum.ARSENAL_4, CounterUtil.getFortressesFromLevel(4, true));
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_4, CounterUtil.getFortressesFromLevel(4, false));
        Assert.assertEquals(CounterFaceTypeEnum.FORTRESS_5, CounterUtil.getFortressesFromLevel(5, false));
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
        Assert.assertEquals(false, CounterUtil.isLandArmy((CounterFaceTypeEnum) null));
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

        Assert.assertEquals(false, CounterUtil.isLandArmy((Stack) null));
        Stack stack = new Stack();
        Assert.assertEquals(false, CounterUtil.isLandArmy(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(false, CounterUtil.isLandArmy(stack));
        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isLandArmy(stack));
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
    public void testArmyCounter() {
        Assert.assertEquals(false, CounterUtil.isArmyCounter(null));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.FORTRESS_3));

        Assert.assertEquals(true, CounterUtil.isArmyCounter(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmyCounter(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(true, CounterUtil.isArmyCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isArmyCounter(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));

        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(false, CounterUtil.isArmyCounter(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
    }

    @Test
    public void testManufacture() {
        Assert.assertFalse(CounterUtil.isManufacture((CounterFaceTypeEnum) null));
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

        Assert.assertFalse(CounterUtil.isManufacture((CounterTypeEnum) null));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_ART));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_CEREALS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_CLOTHES));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_FISH));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_INSTRUMENTS));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_METAL));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_METAL_SCHLESIEN));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_SALT));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_WINE));
        Assert.assertTrue(CounterUtil.isManufacture(CounterTypeEnum.MNU_WOOD));
        Assert.assertFalse(CounterUtil.isManufacture(CounterTypeEnum.ARMY));

        Assert.assertEquals(null, CounterUtil.getManufactureFace(null));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_ART_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_ART));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CEREALS_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_CEREALS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_CLOTHES_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_CLOTHES));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_FISH_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_FISH));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_INSTRUMENTS));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_METAL));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_METAL_SCHLESIEN));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_SALT_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_SALT));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WINE_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_WINE));
        Assert.assertEquals(CounterFaceTypeEnum.MNU_WOOD_MINUS, CounterUtil.getManufactureFace(CounterTypeEnum.MNU_WOOD));
        Assert.assertEquals(null, CounterUtil.getManufactureFace(CounterTypeEnum.ARMY));

        Assert.assertEquals(null, CounterUtil.getManufactureCounter(null));
        Assert.assertEquals(CounterTypeEnum.MNU_ART, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_ART, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_ART_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_CEREALS, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_CEREALS_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_CEREALS, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_CEREALS_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_CLOTHES, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_CLOTHES_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_CLOTHES, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_CLOTHES_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_FISH, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_FISH_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_FISH, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_FISH_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_INSTRUMENTS, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_INSTRUMENTS_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_INSTRUMENTS, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_INSTRUMENTS_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_METAL, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_METAL_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_METAL, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_METAL_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_METAL_SCHLESIEN, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_METAL_SCHLESIEN, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_METAL_SCHLESIEN_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_SALT, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_SALT_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_SALT, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_SALT_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_WINE, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_WINE_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_WINE, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_WINE_PLUS));
        Assert.assertEquals(CounterTypeEnum.MNU_WOOD, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_WOOD_MINUS));
        Assert.assertEquals(CounterTypeEnum.MNU_WOOD, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.MNU_WOOD_PLUS));
        Assert.assertEquals(null, CounterUtil.getManufactureCounter(CounterFaceTypeEnum.ARMY_MINUS));
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
        Assert.assertEquals(null, CounterUtil.getTechnologyType(Tech.MEDIEVAL));
        Assert.assertEquals(null, CounterUtil.getTechnologyType(Tech.CARRACK));
        Assert.assertEquals(null, CounterUtil.getTechnologyType("NotATech"));
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
        Assert.assertTrue(CounterUtil.canTechnologyStack(null, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, true, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_LATIN, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ISLAM, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ASIA, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_LATIN, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ASIA, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, true, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, true, false));

        Assert.assertTrue(CounterUtil.canTechnologyStack(null, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_LATIN, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ISLAM, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ASIA, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, false, false));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_LATIN, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ASIA, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, false, false));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, false, false));


        Assert.assertTrue(CounterUtil.canTechnologyStack(null, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_LATIN, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ISLAM, true, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ASIA, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_LATIN, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ASIA, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, true, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, true, true));

        Assert.assertTrue(CounterUtil.canTechnologyStack(null, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_RENAISSANCE, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_ARQUEBUS, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MUSKET, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BAROQUE, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_MANOEUVRE, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LACE_WAR, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ORTHODOX, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_LATIN, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ISLAM, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_LAND_ASIA, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAE_GALEON, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEON_FLUYT, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_BATTERY, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_VESSEL, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_THREE_DECKER, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_SEVENTY_FOUR, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ORTHODOX, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_LATIN, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ISLAM, false, true));
        Assert.assertFalse(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_NAVAL_ASIA, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_TERCIO, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.TECH_GALLEASS, false, true));
        Assert.assertTrue(CounterUtil.canTechnologyStack(CounterFaceTypeEnum.ARMY_MINUS, false, true));
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
    }

    @Test
    public void testTradingFleet() {
        Assert.assertFalse(CounterUtil.isTradingFleet(null));
        Assert.assertFalse(CounterUtil.isTradingFleet(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertTrue(CounterUtil.isTradingFleet(CounterFaceTypeEnum.TRADING_FLEET_MINUS));
        Assert.assertTrue(CounterUtil.isTradingFleet(CounterFaceTypeEnum.TRADING_FLEET_PLUS));
    }

    @Test
    public void testFace() {
        Assert.assertNull(CounterUtil.getFacePlus(null));
        Assert.assertNull(CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.COLONY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_PLUS, CounterUtil.getFacePlus(CounterFaceTypeEnum.COLONY_PLUS));

        Assert.assertNull(CounterUtil.getFaceMinus(null));
        Assert.assertNull(CounterUtil.getFaceMinus(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_MINUS, CounterUtil.getFaceMinus(CounterFaceTypeEnum.TRADING_POST_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.TRADING_POST_MINUS, CounterUtil.getFaceMinus(CounterFaceTypeEnum.TRADING_POST_PLUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_MINUS, CounterUtil.getFaceMinus(CounterFaceTypeEnum.COLONY_MINUS));
        Assert.assertEquals(CounterFaceTypeEnum.COLONY_MINUS, CounterUtil.getFaceMinus(CounterFaceTypeEnum.COLONY_PLUS));
    }

    @Test
    public void testUpgradeCost() {
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(null, null));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.TRADE_CENTER_MEDITERRANEAN, null));

        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_PLUS, null));
        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, null));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_MINUS, null));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, null));

        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_PLUS, null));
        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS, null));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_MINUS, null));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, null));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_TRANSPORT, null));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_GALLEY, null));

        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_PLUS, true));
        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, true));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_MINUS, true));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, true));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, true));

        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_PLUS, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_MINUS, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_TRANSPORT, true));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_GALLEY, true));

        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_PLUS, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_MINUS, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION, false));

        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_PLUS, false));
        Assert.assertEquals(10, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS, false));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_MINUS, false));
        Assert.assertEquals(5, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS, false));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT, false));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, false));
        Assert.assertEquals(1, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_TRANSPORT, false));
        Assert.assertEquals(0, CounterUtil.getUpgradeCost(CounterFaceTypeEnum.NAVAL_GALLEY, false));
    }

    @Test
    public void testMobile() {
        Assert.assertEquals(false, CounterUtil.isMobile((CounterFaceTypeEnum) null));
        Assert.assertEquals(false, CounterUtil.isMobile(CounterFaceTypeEnum.FORTRESS_3));

        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_INDIAN));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_SEPOY));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));

        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.FLEET_TRANSPORT_PLUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.FLEET_TRANSPORT_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.NAVAL_TRANSPORT));
        Assert.assertEquals(true, CounterUtil.isMobile(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));

        Assert.assertEquals(false, CounterUtil.isMobile((Stack) null));
        Stack stack = new Stack();
        Assert.assertEquals(false, CounterUtil.isMobile(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(false, CounterUtil.isMobile(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(false, CounterUtil.isMobile(stack));
        stack.getCounters().clear();
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(stack));
        stack.getCounters().add(createCounter(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(true, CounterUtil.isMobile(stack));
        stack.setBesieged(true);
        Assert.assertEquals(false, CounterUtil.isMobile(stack));
    }

    private Counter createCounter(CounterFaceTypeEnum type) {
        Counter counter = new Counter();
        counter.setType(type);
        return counter;
    }

    @Test
    public void testExploration() {
        Assert.assertEquals(false, CounterUtil.isExploration(null));
        Assert.assertEquals(false, CounterUtil.isExploration(CounterFaceTypeEnum.FORTRESS_1));

        Assert.assertEquals(true, CounterUtil.isExploration(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isExploration(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK));
        Assert.assertEquals(true, CounterUtil.isExploration(CounterFaceTypeEnum.LAND_INDIAN_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isExploration(CounterFaceTypeEnum.LAND_SEPOY_EXPLORATION));
        Assert.assertEquals(true, CounterUtil.isExploration(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION));
    }

    @Test
    public void testPacha() {
        Assert.assertFalse(CounterUtil.isPacha((CounterFaceTypeEnum) null));
        Assert.assertFalse(CounterUtil.isPacha((String) null));
        Assert.assertFalse(CounterUtil.isPacha(CounterFaceTypeEnum.LEADER));
        Assert.assertFalse(CounterUtil.isPacha(CounterFaceTypeEnum.LEADER.name()));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_1));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_1.name()));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_2));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_2.name()));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_3));
        Assert.assertTrue(CounterUtil.isPacha(CounterFaceTypeEnum.PACHA_3.name()));
    }

    @Test
    public void testGetLeaderType() {
        Assert.assertEquals(CounterFaceTypeEnum.LEADER, CounterUtil.getLeaderType((Leader) null));
        Leader leader = new Leader();
        Assert.assertEquals(CounterFaceTypeEnum.LEADER, CounterUtil.getLeaderType(leader));
        leader.setType(LeaderTypeEnum.GENERAL);
        Assert.assertEquals(CounterFaceTypeEnum.LEADER, CounterUtil.getLeaderType(leader));
        leader.setType(LeaderTypeEnum.PACHA);
        leader.setSize(1);
        Assert.assertEquals(CounterFaceTypeEnum.PACHA_1, CounterUtil.getLeaderType(leader));
        leader.setSize(2);
        Assert.assertEquals(CounterFaceTypeEnum.PACHA_2, CounterUtil.getLeaderType(leader));
        leader.setSize(3);
        Assert.assertEquals(CounterFaceTypeEnum.PACHA_3, CounterUtil.getLeaderType(leader));

        Assert.assertFalse(CounterUtil.isLeaderType(null));
        Assert.assertFalse(CounterUtil.isLeaderType(LimitTypeEnum.ACTION_COL));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_GENERAL));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_GENERAL_AMERICA));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_ADMIRAL));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_CONQUISTADOR));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_CONQUISTADOR_INDIA));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_EXPLORER));
        Assert.assertTrue(CounterUtil.isLeaderType(LimitTypeEnum.LEADER_GOVERNOR));

        Assert.assertEquals(null, CounterUtil.getLeaderType((LimitTypeEnum) null));
        Assert.assertEquals(null, CounterUtil.getLeaderType(LimitTypeEnum.ACTION_COL));
        Assert.assertEquals(LeaderTypeEnum.GENERAL, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_GENERAL));
        Assert.assertEquals(LeaderTypeEnum.GENERAL, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_GENERAL_AMERICA));
        Assert.assertEquals(LeaderTypeEnum.ADMIRAL, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_ADMIRAL));
        Assert.assertEquals(LeaderTypeEnum.CONQUISTADOR, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_CONQUISTADOR));
        Assert.assertEquals(LeaderTypeEnum.CONQUISTADOR, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_CONQUISTADOR_INDIA));
        Assert.assertEquals(LeaderTypeEnum.EXPLORER, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_EXPLORER));
        Assert.assertEquals(LeaderTypeEnum.GOVERNOR, CounterUtil.getLeaderType(LimitTypeEnum.LEADER_GOVERNOR));
    }
}
