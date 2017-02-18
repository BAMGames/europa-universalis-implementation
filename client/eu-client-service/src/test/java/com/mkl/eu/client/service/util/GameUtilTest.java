package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public void testTechnology() {
        Assert.assertNull(GameUtil.getTechnology(null));
        Assert.assertNull(GameUtil.getTechnology(""));
        Assert.assertNull(GameUtil.getTechnology("eIdf"));
        Assert.assertEquals(new Integer(1), GameUtil.getTechnology("B_TECH_1"));
        Assert.assertEquals(new Integer(3), GameUtil.getTechnology("B_TECH_3"));
        Assert.assertEquals(new Integer(12), GameUtil.getTechnology("B_TECH_12"));
        Assert.assertEquals(25, GameUtil.getTechnology("B_TECH_25").intValue());
        Assert.assertEquals(60, GameUtil.getTechnology("B_TECH_60").intValue());

        Assert.assertEquals("B_TECH_1", GameUtil.getTechnologyBox(1));
        Assert.assertEquals("B_TECH_3", GameUtil.getTechnologyBox(3));
        Assert.assertEquals("B_TECH_12", GameUtil.getTechnologyBox(12));
    }

    @Test
    public void testIsTechnology() {
        Assert.assertFalse(GameUtil.isTechnologyBox(null));
        Assert.assertFalse(GameUtil.isTechnologyBox(""));
        Assert.assertFalse(GameUtil.isTechnologyBox("eIdf"));
        Assert.assertTrue(GameUtil.isTechnologyBox("B_TECH_1"));
        Assert.assertTrue(GameUtil.isTechnologyBox("B_TECH_3"));
        Assert.assertTrue(GameUtil.isTechnologyBox("B_TECH_12"));
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
    public void testRotwProvince() {
        Assert.assertEquals(false, GameUtil.isRotwProvince(null));
        Assert.assertEquals(false, GameUtil.isRotwProvince(""));
        Assert.assertEquals(false, GameUtil.isRotwProvince("eTangers"));
        Assert.assertEquals(true, GameUtil.isRotwProvince("rTangers"));
    }

    @Test
    public void testGetActivePlayers() {
        List<PlayableCountry> countries;
        Game game = new Game();
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        countries = GameUtil.getActivePlayers(game);

        Assert.assertEquals(0, countries.size());

        PlayableCountry france = new PlayableCountry();
        france.setName("france");
        france.setReady(true);
        game.getCountries().add(france);
        PlayableCountry angleterre = new PlayableCountry();
        angleterre.setName("angleterre");
        game.getCountries().add(angleterre);

        countries = GameUtil.getActivePlayers(game);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());

        PlayableCountry turquie = new PlayableCountry();
        turquie.setName("turquie");
        turquie.setReady(true);
        game.getCountries().add(turquie);
        PlayableCountry pologne = new PlayableCountry();
        pologne.setName("pologne");
        pologne.setReady(false);
        game.getCountries().add(pologne);

        countries = GameUtil.getActivePlayers(game);
        Collections.sort(countries, Comparator.comparing(PlayableCountry::getName));

        Assert.assertEquals(2, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());
        Assert.assertEquals("pologne", countries.get(1).getName());

        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        countries = GameUtil.getActivePlayers(game);
        Collections.sort(countries, Comparator.comparing(PlayableCountry::getName));

        Assert.assertEquals(0, countries.size());

        game.setStatus(GameStatusEnum.MILITARY_HIERARCHY);

        countries = GameUtil.getActivePlayers(game);
        Collections.sort(countries, Comparator.comparing(PlayableCountry::getName));

        Assert.assertEquals(2, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());
        Assert.assertEquals("pologne", countries.get(1).getName());

        game.setStatus(GameStatusEnum.MILITARY_MOVE);

        CountryOrder order = new CountryOrder();
        order.setCountry(france);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(france);
        order.setGameStatus(GameStatusEnum.DIPLOMACY);
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(angleterre);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(turquie);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setActive(false);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(pologne);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        game.getOrders().add(order);

        countries = GameUtil.getActivePlayers(game);
        Collections.sort(countries, Comparator.comparing(PlayableCountry::getName));

        Assert.assertEquals(2, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());
        Assert.assertEquals("france", countries.get(1).getName());
    }
}
