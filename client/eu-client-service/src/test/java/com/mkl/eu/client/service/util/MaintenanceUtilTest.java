package com.mkl.eu.client.service.util;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.tables.BasicForce;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.client.service.vo.tables.Unit;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for MaintenanceUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class MaintenanceUtilTest {

    @Test
    public void testComputeUnitMaintenance() {
        Assert.assertEquals(0, MaintenanceUtil.computeUnitMaintenance(null, null, null));

        Map<CounterFaceTypeEnum, Long> forces = new HashMap<>();

        Assert.assertEquals(0, MaintenanceUtil.computeUnitMaintenance(forces, null, null));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1l);

        Assert.assertEquals(0, MaintenanceUtil.computeUnitMaintenance(forces, null, null));

        List<Unit> units = new ArrayList<>();

        Assert.assertEquals(0, MaintenanceUtil.computeUnitMaintenance(forces, null, units));

        Unit unit = new Unit();
        unit.setPrice(12);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        units.add(unit);

        Assert.assertEquals(12, MaintenanceUtil.computeUnitMaintenance(forces, null, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 3l);

        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, null, units));

        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1l);

        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, null, units));

        unit = new Unit();
        unit.setPrice(25);
        unit.setType(ForceTypeEnum.ARMY_PLUS);
        units.add(unit);

        Assert.assertEquals(61, MaintenanceUtil.computeUnitMaintenance(forces, null, units));

        List<BasicForce> basicForces = new ArrayList<>();

        Assert.assertEquals(61, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        BasicForce basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_PLUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        Assert.assertEquals(24, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        basicForces.get(0).setNumber(2);

        Assert.assertEquals(0, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 4l);

        Assert.assertEquals(12, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.NAVAL_TRANSPORT, 1l);
        forces.put(CounterFaceTypeEnum.NAVAL_DETACHMENT, 1l);
        forces.put(CounterFaceTypeEnum.NAVAL_GALLEY, 1l);
        forces.put(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, 2l);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.FLEET_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ND);
        basicForce.setNumber(2);
        basicForces.add(basicForce);

        Assert.assertEquals(12, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        unit = new Unit();
        unit.setPrice(80);
        unit.setType(ForceTypeEnum.FLEET_PLUS);
        units.add(unit);
        unit = new Unit();
        unit.setPrice(50);
        unit.setType(ForceTypeEnum.FLEET_MINUS);
        units.add(unit);
        unit = new Unit();
        unit.setPrice(25);
        unit.setType(ForceTypeEnum.ND);
        units.add(unit);

        Assert.assertEquals(12, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, 3l);

        Assert.assertEquals(25, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        unit.setPrice(20);

        Assert.assertEquals(22, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, 2l);
        forces.put(CounterFaceTypeEnum.ARMY_TIMAR_MINUS, 3l);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_TIMAR_PLUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_TIMAR_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.LDT);
        basicForce.setNumber(2);
        basicForces.add(basicForce);

        Assert.assertEquals(58, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1l);
        unit = new Unit();
        unit.setPrice(8);
        unit.setType(ForceTypeEnum.LD);
        units.add(unit);
        basicForces = new ArrayList<>();
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.LD);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        Assert.assertEquals(8, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1l);

        Assert.assertEquals(20, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        basicForce.setType(ForceTypeEnum.ARMY_MINUS);

        Assert.assertEquals(12, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1l);

        Assert.assertEquals(24, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.ARMY_PLUS).setPrice(23);

        Assert.assertEquals(23, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.ARMY_PLUS).setPrice(25);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1l);

        Assert.assertEquals(32, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        basicForce.setType(ForceTypeEnum.LD);

        Assert.assertEquals(37, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 1L);

        Assert.assertEquals(41, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        // Forces: A+ A- 3LD 15 LDe
        // Basic forces: 16 LD
        // Remain: 1LDe -> 4

        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 3L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 15L);

        basicForces = new ArrayList<>();
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.LD);
        basicForce.setNumber(16);
        basicForces.add(basicForce);

        units = new ArrayList<>();
        Unit armyPlus = new Unit();
        armyPlus.setType(ForceTypeEnum.ARMY_PLUS);
        units.add(armyPlus);
        Unit armyMinus = new Unit();
        armyMinus.setType(ForceTypeEnum.ARMY_MINUS);
        units.add(armyMinus);
        Unit detachment = new Unit();
        detachment.setType(ForceTypeEnum.LD);
        units.add(detachment);
        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        detachment.setPrice(8);

        Assert.assertEquals(4, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        // Forces: A+ A- LD
        // Basic forces: 3 LD

        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1L);
        basicForce.setNumber(3);

        // A+ = 45
        // 2A- = 40
        // A- 2LD = 36
        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(35);

        // A+ = 35
        // 2A- = 40
        // A- 2LD = 36
        Assert.assertEquals(35, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(15);
        // A+ = 45
        // 2A- = 30
        // A- 2LD = 31
        Assert.assertEquals(30, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));


        armyMinus.setPrice(20);
        // Forces: A+ A- LD
        // Basic forces: 1LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1L);
        basicForce.setNumber(1);

        // A+ A- = 65
        // A+ 2LD = 61
        // 2A- 2LD = 56
        Assert.assertEquals(56, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(35);
        // A+ A- = 55
        // A+ 2LD = 51
        // 2A- 2LD = 56
        Assert.assertEquals(51, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(25);
        armyMinus.setPrice(15);
        // A+ A- = 40
        // A+ 2LD = 41
        // 2A- 2LD = 46
        Assert.assertEquals(40, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        // Forces: A+ 2LD
        // Basic forces: 2LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 2L);
        basicForce.setNumber(2);

        // A+ = 45
        // A- 2LD = 36
        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(25);
        // A+ = 45
        // A- 2LD = 41
        Assert.assertEquals(41, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(30);
        // A+ = 45
        // A- 2LD = 46
        Assert.assertEquals(45, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(20);
        // Forces: A- LD
        // Basic forces: LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1L);
        basicForce.setNumber(1);

        // A- = 20
        // 2LD = 16
        Assert.assertEquals(16, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(15);
        // A- = 15
        // 2LD = 16
        Assert.assertEquals(15, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(20);
        // Forces: A+ LD
        // Basic forces: 3LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1L);
        basicForce.setNumber(3);

        // A- = 20
        // 2LD = 16
        Assert.assertEquals(16, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(15);
        // A- = 15
        // 2LD = 16
        Assert.assertEquals(15, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(25);
        // A- = 15
        // 2LD = 16
        Assert.assertEquals(15, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        // Forces: A+ 4LDe
        // Basic forces: 4LDe
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 4L);
        basicForce.setNumber(2);

        // A+ = 45
        // A- 4LDe = 36
        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(30);
        // A+ = 45
        // A- 4LDe = 46
        Assert.assertEquals(45, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(20);
        // Forces: A+ 6LDe
        // Basic forces: 3LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 6L);
        basicForce.setNumber(3);

        // A+ = 45
        // A- 4LDe = 36
        // LD 6LDe = 32
        Assert.assertEquals(32, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(15);
        // A+ = 45
        // A- 4LDe = 31
        // LD 6 LDe = 32
        Assert.assertEquals(31, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(30);
        // A+ = 30
        // A- 4LDe = 31
        // LD 6 LDe = 32
        Assert.assertEquals(30, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        // Forces: A+ 2LDe
        // Basic forces: 1LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 2L);
        basicForce.setNumber(1);

        // A+ = 45
        // A- LD 2LDe = 36
        Assert.assertEquals(36, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(35);
        // A+ = 35
        // A- LD 2LDe = 36
        Assert.assertEquals(35, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        // Forces: A- 2LDe
        // Basic forces: 1LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 2L);
        basicForce.setNumber(1);

        // A- = 20
        // LD + 2 LDe = 16
        Assert.assertEquals(16, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyMinus.setPrice(15);
        // A- = 15
        // LD + 2 LDe = 16
        Assert.assertEquals(15, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(29);
        // Forces: A+ A-
        // Basic forces: LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1L);
        basicForce.setNumber(1);

        // A+ + LD = 37
        // 2A- + LD = 38
        Assert.assertEquals(37, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(31);
        // A+ + LD = 39
        // 2A- + LD = 38
        Assert.assertEquals(38, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        // Forces: A+ LDe
        // Basic forces: LD
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION, 1L);
        basicForce.setNumber(1);

        // A- LD LDe = 32
        Assert.assertEquals(32, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(29);
        armyMinus.setPrice(15);
        // A- LD LDe = 27
        Assert.assertEquals(27, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(31);
        // A- LD LDe = 27
        Assert.assertEquals(27, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(20);
        // A- LD LDe = 27
        Assert.assertEquals(27, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        armyPlus.setPrice(45);
        armyMinus.setPrice(20);
        unit = new Unit();
        unit.setPrice(80);
        unit.setType(ForceTypeEnum.FLEET_PLUS);
        units.add(unit);
        unit = new Unit();
        unit.setPrice(50);
        unit.setType(ForceTypeEnum.FLEET_MINUS);
        units.add(unit);
        unit = new Unit();
        unit.setPrice(25);
        unit.setType(ForceTypeEnum.ND);
        units.add(unit);

        // Forces: F+ A+ AT+
        // Basic forces: F- A- AT- LDND
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.FLEET_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, 1L);
        basicForces = new ArrayList<>();
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.FLEET_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_TIMAR_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);
        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.LDND);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        // ND + A- + AT- = 65
        Assert.assertEquals(65, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        // Forces: F+ A+ AT+
        // Basic forces: F- A- AT- 2LDND
        basicForce.setNumber(2);
        // A- + AT- = 40
        Assert.assertEquals(40, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        // Forces: F+ A+ AT+
        // Basic forces: F- A- AT- 3LDND
        basicForce.setNumber(3);
        // LD + AT- = 40
        Assert.assertEquals(28, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));

        // Forces: F+ AT+
        // Basic forces: F- A- AT- 3LDND
        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.FLEET_PLUS, 1L);
        forces.put(CounterFaceTypeEnum.ARMY_TIMAR_PLUS, 1L);
        basicForce.setNumber(3);
        // LDT- = 8
        Assert.assertEquals(8, MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units));
    }

    @Test
    public void testPurchasePrice() {
        Assert.assertEquals(0, MaintenanceUtil.getPurchasePrice(null, null, null, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(null, null, 12, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(50, null, 12, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(50, null, 12, 2));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(0, 5, 12, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(0, 5, 12, 1));

        Assert.assertEquals(24, MaintenanceUtil.getPurchasePrice(5, 5, 12, 1));

        Assert.assertEquals(24, MaintenanceUtil.getPurchasePrice(9, 5, 12, 1));

        Assert.assertEquals(36, MaintenanceUtil.getPurchasePrice(10, 5, 12, 1));

        Assert.assertEquals(18, MaintenanceUtil.getPurchasePrice(null, 1, 12, 2));

        Assert.assertEquals(20, MaintenanceUtil.getPurchasePrice(null, 1, 13, 2));

        Assert.assertEquals(40, MaintenanceUtil.getPurchasePrice(1, 2, 20, 4));

        Assert.assertEquals(47, MaintenanceUtil.getPurchasePrice(1, 2, 23, 4));

        Assert.assertEquals(45, MaintenanceUtil.getPurchasePrice(1, 2, 22, 4));

        // Will never happen functionally but wanted to test the trampoline pattern
        Assert.assertEquals(5020, MaintenanceUtil.getPurchasePrice(20, 1, 1, 5000));
    }

    @Test
    public void testComputeFortressesMaintenance() {
        Assert.assertEquals(0, MaintenanceUtil.computeFortressesMaintenance(null, null, null, null));

        Map<Pair<Integer, Boolean>, Integer> fortresses = new HashMap<>();
        fortresses.put(new ImmutablePair<>(-1, null), -1);

        Assert.assertEquals(0, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, null));

        fortresses.put(new ImmutablePair<>(0, null), 2);

        Assert.assertEquals(0, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, null));

        fortresses.put(new ImmutablePair<>(1, null), 3);

        Assert.assertEquals(3, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, null));

        fortresses.put(new ImmutablePair<>(2, true), 2);

        Assert.assertEquals(11, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, null));

        fortresses.put(new ImmutablePair<>(4, false), 1);

        Assert.assertEquals(19, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, null));

        Assert.assertEquals(15, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, 40));

        fortresses.put(new ImmutablePair<>(3, true), 2);

        Assert.assertEquals(39, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, 40));

        fortresses.put(new ImmutablePair<>(0, true), 1);

        Assert.assertEquals(40, MaintenanceUtil.computeFortressesMaintenance(fortresses, null, null, 40));

        List<Tech> techs = new ArrayList<>();
        Tech arquebus = new Tech();
        arquebus.setBeginTurn(17);
        arquebus.setName(Tech.ARQUEBUS);
        techs.add(arquebus);
        Tech renaissance = new Tech();
        renaissance.setBeginTurn(11);
        renaissance.setName(Tech.RENAISSANCE);
        techs.add(renaissance);
        Tech baroque = new Tech();
        baroque.setBeginTurn(24);
        baroque.setName(Tech.BAROQUE);
        techs.add(baroque);

        Assert.assertEquals(40, MaintenanceUtil.computeFortressesMaintenance(fortresses, techs, null, 40));

        Assert.assertEquals(40, MaintenanceUtil.computeFortressesMaintenance(fortresses, techs, renaissance, 40));

        Assert.assertEquals(28, MaintenanceUtil.computeFortressesMaintenance(fortresses, techs, baroque, 40));
    }

    @Test
    public void testGetPurchaseForceFromFace() {
        Assert.assertEquals(null, MaintenanceUtil.getPurchaseForceFromFace(null));
        Assert.assertEquals(null, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.MNU_ART_MINUS));
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.ARMY_PLUS));
        Assert.assertEquals(ForceTypeEnum.ARMY_PLUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.ARMY_TIMAR_PLUS));
        Assert.assertEquals(ForceTypeEnum.ARMY_MINUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.ARMY_MINUS));
        Assert.assertEquals(ForceTypeEnum.ARMY_MINUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.ARMY_TIMAR_MINUS));
        Assert.assertEquals(ForceTypeEnum.LD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.LAND_DETACHMENT));
        Assert.assertEquals(ForceTypeEnum.LD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK));
        Assert.assertEquals(ForceTypeEnum.LD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR));
        Assert.assertEquals(ForceTypeEnum.FLEET_PLUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.FLEET_PLUS));
        Assert.assertEquals(ForceTypeEnum.FLEET_MINUS, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.FLEET_MINUS));
        Assert.assertEquals(ForceTypeEnum.NWD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.NAVAL_DETACHMENT));
        Assert.assertEquals(ForceTypeEnum.NGD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.NAVAL_GALLEY));
        Assert.assertEquals(ForceTypeEnum.NTD, MaintenanceUtil.getPurchaseForceFromFace(CounterFaceTypeEnum.NAVAL_TRANSPORT));
    }
}
