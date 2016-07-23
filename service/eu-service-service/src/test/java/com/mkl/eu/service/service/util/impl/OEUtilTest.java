package com.mkl.eu.service.service.util.impl;

import com.mkl.eu.client.service.vo.enumeration.BorderEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.BorderEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.RotwProvinceEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for OEUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class OEUtilTest {
    private OEUtilImpl oeUtil = new OEUtilImpl();

    @Test
    public void testAdminValue() {
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(3, oeUtil.getAdministrativeValue(country));
        country.getMonarch().setAdministrative(5);
        Assert.assertEquals(5, oeUtil.getAdministrativeValue(country));
        country.getMonarch().setAdministrative(9);
        Assert.assertEquals(9, oeUtil.getAdministrativeValue(country));
    }

    @Test
    public void testMilValue() {
        Assert.assertEquals(3, oeUtil.getMilitaryValue(null));
        PlayableCountryEntity country = new PlayableCountryEntity();
        Assert.assertEquals(3, oeUtil.getMilitaryValue(country));
        country.setMonarch(new MonarchEntity());
        Assert.assertEquals(3, oeUtil.getMilitaryValue(country));
        country.getMonarch().setMilitary(5);
        Assert.assertEquals(5, oeUtil.getMilitaryValue(country));
        country.getMonarch().setMilitary(9);
        Assert.assertEquals(9, oeUtil.getMilitaryValue(country));
    }

    @Test
    public void testStability() {
        Assert.assertEquals(0, oeUtil.getStability(null, null));
        GameEntity game = new GameEntity();
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().add(new StackEntity());
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).setProvince("B_STAB_1");
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getStability(game, null));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(0, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_2");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(0, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().get(1).setProvince("B_STAB_-2");
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(-2, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(0, oeUtil.getStability(game, "russie"));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(0).getCounters().get(1).setCountry("russie");
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        Assert.assertEquals(1, oeUtil.getStability(game, "france"));
        Assert.assertEquals(-2, oeUtil.getStability(game, "angleterre"));
        Assert.assertEquals(1, oeUtil.getStability(game, "russie"));
    }

    @Test
    public void testTechnology() {
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(null, null, true));
        GameEntity game = new GameEntity();
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().add(new StackEntity());
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).setProvince("B_TECH_1");
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        game.getStacks().get(0).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(0).getCounters().get(0).setCountry("france");
        game.getStacks().get(0).getCounters().get(0).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, null, true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(1).setProvince("B_TECH_25");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(1).setType(CounterFaceTypeEnum.TECH_NAVAL);
        game.getStacks().get(0).getCounters().get(1).setCountry("angleterre");
        game.getStacks().get(0).getCounters().get(1).setOwner(game.getStacks().get(0));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "angleterre", false));
        game.getStacks().add(new StackEntity());
        game.getStacks().get(2).setProvince("B_STAB_3");
        game.getStacks().get(2).getCounters().add(new CounterEntity());
        game.getStacks().get(2).getCounters().get(0).setType(CounterFaceTypeEnum.TECH_LAND);
        game.getStacks().get(2).getCounters().get(0).setCountry("france");
        game.getStacks().get(2).getCounters().get(0).setOwner(game.getStacks().get(2));
        Assert.assertEquals(0, oeUtil.getTechnologyAdvance(game, "france", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "france", false));
        Assert.assertEquals(25, oeUtil.getTechnologyAdvance(game, "angleterre", true));
        Assert.assertEquals(1, oeUtil.getTechnologyAdvance(game, "angleterre", false));
    }

    @Test
    public void testSettleDistance() {
        Assert.assertEquals(-1, oeUtil.settleDistance(null, null, null, null, 0));
        RotwProvinceEntity panama = new RotwProvinceEntity();
        panama.setTerrain(TerrainEnum.SPARSE_FOREST);
        panama.setName("rPanama");
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, null, null, null, 0));
        List<String> discoveries = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.add(panama.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        List<String> sources = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.add(panama.getName());
        Assert.assertEquals(0, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        RotwProvinceEntity mexique = new RotwProvinceEntity();
        mexique.setTerrain(TerrainEnum.SEA);
        mexique.setName("mexique");
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(0).setProvinceFrom(panama);
        panama.getBorders().get(0).setProvinceTo(mexique);
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.add(mexique.getName());
        Assert.assertEquals(0, oeUtil.settleDistance(panama, discoveries, null, null, 0));
        discoveries.remove(mexique.getName());
        RotwProvinceEntity ecuador = new RotwProvinceEntity();
        ecuador.setTerrain(TerrainEnum.MOUNTAIN);
        ecuador.setName("rEcuador");
        sources.remove(panama.getName());
        sources.add(ecuador.getName());
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(1).setProvinceFrom(panama);
        panama.getBorders().get(1).setProvinceTo(ecuador);
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        discoveries.add(ecuador.getName());
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        RotwProvinceEntity venezuelua = new RotwProvinceEntity();
        venezuelua.setTerrain(TerrainEnum.PLAIN);
        venezuelua.setName("rVenezuela");
        sources.add(venezuelua.getName());
        panama.getBorders().add(new BorderEntity());
        panama.getBorders().get(2).setProvinceFrom(panama);
        panama.getBorders().get(2).setProvinceTo(venezuelua);
        panama.getBorders().get(2).setType(BorderEnum.RIVER);
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.remove(ecuador.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        discoveries.add(venezuelua.getName());
        Assert.assertEquals(8, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.add(ecuador.getName());
        Assert.assertEquals(6, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        sources.clear();
        RotwProvinceEntity guyane = new RotwProvinceEntity();
        guyane.setTerrain(TerrainEnum.PLAIN);
        guyane.setName("rGuyane");
        venezuelua.getBorders().add(new BorderEntity());
        venezuelua.getBorders().get(0).setProvinceFrom(venezuelua);
        venezuelua.getBorders().get(0).setProvinceTo(guyane);
        venezuelua.getBorders().get(0).setType(BorderEnum.PASS);
        sources.add(guyane.getName());
        discoveries.add(guyane.getName());
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, null, 0));
        List<String> friendlies = new ArrayList<>();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        friendlies.add(venezuelua.getName());
        Assert.assertEquals(12, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        RotwProvinceEntity amazonia = new RotwProvinceEntity();
        amazonia.setTerrain(TerrainEnum.PLAIN);
        amazonia.setName("rAmazonia");
        ecuador.getBorders().add(new BorderEntity());
        ecuador.getBorders().get(0).setProvinceFrom(ecuador);
        ecuador.getBorders().get(0).setProvinceTo(amazonia);
        ecuador.getBorders().get(0).setType(BorderEnum.BERING_STRAIT);
        discoveries.add(ecuador.getName());
        Assert.assertEquals(12, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        friendlies.clear();
        Assert.assertEquals(-1, oeUtil.settleDistance(panama, discoveries, sources, friendlies, 0));
        Assert.assertEquals(12, oeUtil.settleDistance(ecuador, discoveries, sources, friendlies, 0));
    }

    @Test
    public void testRollDie() {
        GameEntity game = new GameEntity();
        long seed = System.nanoTime();

        game.setSeed(seed);

        int die = oeUtil.rollDie(game, (String) null);

        game.setSeed(seed);

        Assert.assertEquals(die, oeUtil.rollDie(game, (PlayableCountryEntity) null));
    }
}
