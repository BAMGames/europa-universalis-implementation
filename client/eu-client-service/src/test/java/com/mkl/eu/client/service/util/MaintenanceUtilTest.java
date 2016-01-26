package com.mkl.eu.client.service.util;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.ForceTypeEnum;
import com.mkl.eu.client.service.vo.tables.BasicForce;
import com.mkl.eu.client.service.vo.tables.Unit;
import com.mkl.eu.client.service.vo.util.MaintenanceUtil;
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
    public void testComputeMaintenance() {
        Assert.assertEquals(0, MaintenanceUtil.computeMaintenance(null, null, null));

        Map<CounterFaceTypeEnum, Long> forces = new HashMap<>();

        Assert.assertEquals(0, MaintenanceUtil.computeMaintenance(forces, null, null));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1l);

        Assert.assertEquals(0, MaintenanceUtil.computeMaintenance(forces, null, null));

        List<Unit> units = new ArrayList<>();

        Assert.assertEquals(0, MaintenanceUtil.computeMaintenance(forces, null, units));

        Unit unit = new Unit();
        unit.setPrice(12);
        unit.setType(ForceTypeEnum.ARMY_MINUS);
        units.add(unit);

        Assert.assertEquals(12, MaintenanceUtil.computeMaintenance(forces, null, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 3l);

        Assert.assertEquals(36, MaintenanceUtil.computeMaintenance(forces, null, units));

        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1l);

        Assert.assertEquals(36, MaintenanceUtil.computeMaintenance(forces, null, units));

        unit = new Unit();
        unit.setPrice(25);
        unit.setType(ForceTypeEnum.ARMY_PLUS);
        units.add(unit);

        Assert.assertEquals(61, MaintenanceUtil.computeMaintenance(forces, null, units));

        List<BasicForce> basicForces = new ArrayList<>();

        Assert.assertEquals(61, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        BasicForce basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_PLUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        Assert.assertEquals(36, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        basicForce = new BasicForce();
        basicForce.setType(ForceTypeEnum.ARMY_MINUS);
        basicForce.setNumber(1);
        basicForces.add(basicForce);

        Assert.assertEquals(24, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        basicForces.get(0).setNumber(2);

        Assert.assertEquals(0, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 4l);

        Assert.assertEquals(12, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

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

        Assert.assertEquals(12, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

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

        Assert.assertEquals(12, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION, 3l);

        Assert.assertEquals(25, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        unit.setPrice(20);

        Assert.assertEquals(22, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

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

        Assert.assertEquals(58, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

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

        Assert.assertEquals(8, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        forces = new HashMap<>();
        forces.put(CounterFaceTypeEnum.ARMY_PLUS, 1l);

        Assert.assertEquals(20, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        basicForce.setType(ForceTypeEnum.ARMY_MINUS);

        Assert.assertEquals(12, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        forces.put(CounterFaceTypeEnum.ARMY_MINUS, 1l);

        Assert.assertEquals(24, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.ARMY_PLUS).setPrice(23);

        Assert.assertEquals(23, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        CommonUtil.findFirst(units, iUnit -> iUnit.getType() == ForceTypeEnum.ARMY_PLUS).setPrice(25);
        forces.put(CounterFaceTypeEnum.LAND_DETACHMENT, 1l);

        Assert.assertEquals(32, MaintenanceUtil.computeMaintenance(forces, basicForces, units));

        basicForce.setType(ForceTypeEnum.LD);

        Assert.assertEquals(37, MaintenanceUtil.computeMaintenance(forces, basicForces, units));
    }

    @Test
    public void testPurchasePrice() {
        Assert.assertEquals(0, MaintenanceUtil.getPurchasePrice(null, null, null, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(null, null, 12, null));

        Assert.assertEquals(12, MaintenanceUtil.getPurchasePrice(50, null, 12, null));

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
    }
}
