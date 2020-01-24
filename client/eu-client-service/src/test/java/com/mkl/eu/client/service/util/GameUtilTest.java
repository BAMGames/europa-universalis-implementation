package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diplo.CountryInWar;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;
import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import com.mkl.eu.client.service.vo.ref.country.CountryLight;
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

        Assert.assertEquals("B_PB_0G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_0G")));
        Assert.assertEquals("B_PB_0D", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_0D")));
        Assert.assertEquals("B_PB_1G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_1G")));
        Assert.assertEquals("B_PB_1D", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_1D")));
        Assert.assertEquals("B_PB_2G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_2G")));
        Assert.assertEquals("B_PB_2D", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_2D")));
        Assert.assertEquals("B_PB_3G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_3G")));
        Assert.assertEquals("B_PB_3D", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_3D")));
        Assert.assertEquals("B_PB_4G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_4G")));
        Assert.assertEquals("B_PB_4D", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_4D")));
        Assert.assertEquals("B_PB_5G", GameUtil.inflationBoxFromNumber(GameUtil.inflationBoxToNumber("B_PB_5G")));

        Assert.assertFalse(GameUtil.isInflationMax("B_PB_0G"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_0D"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_1G"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_1D"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_2G"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_2D"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_3G"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_3D"));
        Assert.assertFalse(GameUtil.isInflationMax("B_PB_4G"));
        Assert.assertTrue(GameUtil.isInflationMax("B_PB_4D"));

        Assert.assertTrue(GameUtil.isInflationMin("B_PB_0G"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_0D"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_1G"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_1D"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_2G"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_2D"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_3G"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_3D"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_4G"));
        Assert.assertFalse(GameUtil.isInflationMin("B_PB_4D"));
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
    public void testRounds() {
        Assert.assertFalse(GameUtil.isRoundBox(null));
        Assert.assertFalse(GameUtil.isRoundBox("eIdf"));
        Assert.assertTrue(GameUtil.isRoundBox("B_MR_W0"));
        Assert.assertTrue(GameUtil.isRoundBox("B_MR_S1"));
        Assert.assertTrue(GameUtil.isRoundBox("B_MR_W5"));
        Assert.assertTrue(GameUtil.isRoundBox("B_MR_S5"));
        Assert.assertTrue(GameUtil.isRoundBox("B_MR_End"));

        Assert.assertFalse(GameUtil.isWinterRoundBox(null));
        Assert.assertFalse(GameUtil.isWinterRoundBox("eIdf"));
        Assert.assertTrue(GameUtil.isWinterRoundBox("B_MR_W0"));
        Assert.assertFalse(GameUtil.isWinterRoundBox("B_MR_S1"));
        Assert.assertTrue(GameUtil.isWinterRoundBox("B_MR_W5"));
        Assert.assertFalse(GameUtil.isWinterRoundBox("B_MR_S5"));
        Assert.assertFalse(GameUtil.isWinterRoundBox("B_MR_End"));

        Assert.assertEquals(-1, GameUtil.getRoundBox(null));
        Assert.assertEquals(-1, GameUtil.getRoundBox("eIdf"));
        Assert.assertEquals(0, GameUtil.getRoundBox("B_MR_W0"));
        Assert.assertEquals(1, GameUtil.getRoundBox("B_MR_S1"));
        Assert.assertEquals(5, GameUtil.getRoundBox("B_MR_W5"));
        Assert.assertEquals(5, GameUtil.getRoundBox("B_MR_S5"));
        Assert.assertEquals(-1, GameUtil.getRoundBox("B_MR_End"));

        Assert.assertEquals("B_MR_W0", GameUtil.getRoundBoxAdd("B_MR_W0", 0));
        Assert.assertEquals("B_MR_S1", GameUtil.getRoundBoxAdd("B_MR_W0", 1));
        Assert.assertEquals("B_MR_W1", GameUtil.getRoundBoxAdd("B_MR_W0", 2));
        Assert.assertEquals("B_MR_S2", GameUtil.getRoundBoxAdd("B_MR_W0", 3));
        Assert.assertEquals("B_MR_W2", GameUtil.getRoundBoxAdd("B_MR_W0", 4));
        Assert.assertEquals("B_MR_S3", GameUtil.getRoundBoxAdd("B_MR_W0", 5));

        Assert.assertEquals("B_MR_S1", GameUtil.getRoundBoxAdd("B_MR_S1", 0));
        Assert.assertEquals("B_MR_W1", GameUtil.getRoundBoxAdd("B_MR_S1", 1));
        Assert.assertEquals("B_MR_S2", GameUtil.getRoundBoxAdd("B_MR_S1", 2));
        Assert.assertEquals("B_MR_W2", GameUtil.getRoundBoxAdd("B_MR_S1", 3));
        Assert.assertEquals("B_MR_S3", GameUtil.getRoundBoxAdd("B_MR_S1", 4));
        Assert.assertEquals("B_MR_W3", GameUtil.getRoundBoxAdd("B_MR_S1", 5));

        Assert.assertEquals("B_MR_W5", GameUtil.getRoundBoxAdd("B_MR_W3", 4));
        Assert.assertEquals(GameUtil.ROUND_END, GameUtil.getRoundBoxAdd("B_MR_W3", 5));
        Assert.assertFalse(GameUtil.isLastRound(GameUtil.getRoundBoxAdd("B_MR_W3", 4)));
        Assert.assertTrue(GameUtil.isLastRound(GameUtil.getRoundBoxAdd("B_MR_W3", 5)));

        Assert.assertEquals("B_MR_W5", GameUtil.getRoundBoxAdd("B_MR_S4", 3));
        Assert.assertEquals(GameUtil.ROUND_END, GameUtil.getRoundBoxAdd("B_MR_S4", 4));
        Assert.assertFalse(GameUtil.isLastRound(GameUtil.getRoundBoxAdd("B_MR_S4", 3)));
        Assert.assertTrue(GameUtil.isLastRound(GameUtil.getRoundBoxAdd("B_MR_S4", 4)));

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_W0") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_S1") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_W1") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_S2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_W2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_S3") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W0", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_S1") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_W1") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_S2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_W2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_S3") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S1", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_S1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_W1") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_S2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_W2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_S3") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W1", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_S1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_W1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_S2") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_W2") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_S3") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S2", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_S1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_W1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_S2") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_W2") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_S3") < 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_W2", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_S1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_W1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_S2") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_W2") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_S3") == 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_S3", "B_MR_END") < 0);

        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_W0") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_S1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_W1") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_S2") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_W2") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_S3") > 0);
        Assert.assertTrue(GameUtil.compareRoundBoxes("B_MR_END", "B_MR_END") == 0);

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
        france.setUsername("france");
        france.setReady(true);
        game.getCountries().add(france);
        PlayableCountry angleterre = new PlayableCountry();
        angleterre.setName("angleterre");
        angleterre.setUsername("angleterre");
        game.getCountries().add(angleterre);

        countries = GameUtil.getActivePlayers(game);

        Assert.assertEquals(1, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());

        PlayableCountry turquie = new PlayableCountry();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        turquie.setReady(true);
        game.getCountries().add(turquie);
        PlayableCountry pologne = new PlayableCountry();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        pologne.setReady(false);
        game.getCountries().add(pologne);
        PlayableCountry suede = new PlayableCountry();
        suede.setName("suede");
        suede.setReady(false);
        game.getCountries().add(suede);

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
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(angleterre);
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(turquie);
        order.setActive(false);
        game.getOrders().add(order);
        order = new CountryOrder();
        order.setCountry(pologne);
        game.getOrders().add(order);

        countries = GameUtil.getActivePlayers(game);
        Collections.sort(countries, Comparator.comparing(PlayableCountry::getName));

        Assert.assertEquals(2, countries.size());
        Assert.assertEquals("angleterre", countries.get(0).getName());
        Assert.assertEquals("france", countries.get(1).getName());
    }

    @Test
    public void testAtWar() {
        Game game = new Game();
        War war = new War();
        war.getCountries().add(createCountryInWar("france", WarImplicationEnum.FULL, true));
        war.getCountries().add(createCountryInWar("turquie", WarImplicationEnum.LIMITED, true));
        war.getCountries().add(createCountryInWar("espagne", WarImplicationEnum.FULL, false));
        game.getWars().add(war);
        war = new War();
        war.getCountries().add(createCountryInWar("turquie", WarImplicationEnum.FULL, true));
        war.getCountries().add(createCountryInWar("france", WarImplicationEnum.LIMITED, true));
        war.getCountries().add(createCountryInWar("pologne", WarImplicationEnum.FULL, false));
        war.getCountries().add(createCountryInWar("russie", WarImplicationEnum.LIMITED, false));
        game.getWars().add(war);

        Assert.assertFalse(GameUtil.isAtWar(null, game));
        Assert.assertTrue(GameUtil.isAtWar("france", game));
        Assert.assertTrue(GameUtil.isAtWar("espagne", game));
        Assert.assertTrue(GameUtil.isAtWar("turquie", game));
        Assert.assertTrue(GameUtil.isAtWar("pologne", game));
        Assert.assertFalse(GameUtil.isAtWar("russie", game));
        Assert.assertFalse(GameUtil.isAtWar("england", game));
    }

    private CountryInWar createCountryInWar(String countryName, WarImplicationEnum implication, boolean offensive) {
        CountryInWar country = new CountryInWar();
        country.setCountry(new CountryLight());
        country.getCountry().setName(countryName);
        country.setImplication(implication);
        country.setOffensive(offensive);
        return country;
    }

    @Test
    public void testTurn() {
        Assert.assertFalse(GameUtil.isTurnBox(null));
        Assert.assertFalse(GameUtil.isTurnBox("eIdf"));
        Assert.assertTrue(GameUtil.isTurnBox("B_Turn_1"));
        Assert.assertTrue(GameUtil.isTurnBox("B_Turn_12"));
        Assert.assertTrue(GameUtil.isTurnBox("B_Turn_62"));

        Assert.assertEquals("B_Turn_1", GameUtil.getTurnBox(-1));
        Assert.assertEquals("B_Turn_1", GameUtil.getTurnBox(0));
        Assert.assertEquals("B_Turn_1", GameUtil.getTurnBox(1));
        Assert.assertEquals("B_Turn_5", GameUtil.getTurnBox(5));
        Assert.assertEquals("B_Turn_12", GameUtil.getTurnBox(12));
        Assert.assertEquals("B_Turn_62", GameUtil.getTurnBox(62));
        Assert.assertEquals("B_Turn_62", GameUtil.getTurnBox(63));
        Assert.assertEquals("B_Turn_62", GameUtil.getTurnBox(127));
    }

    @Test
    public void testImproveStability() {
        Assert.assertEquals(-1, GameUtil.improveStability(-5));
        Assert.assertEquals(-1, GameUtil.improveStability(-4));
        Assert.assertEquals(-1, GameUtil.improveStability(-3));
        Assert.assertEquals(-1, GameUtil.improveStability(-2));
        Assert.assertEquals(-1, GameUtil.improveStability(-1));
        Assert.assertEquals(-1, GameUtil.improveStability(0));
        Assert.assertEquals(-1, GameUtil.improveStability(1));
        Assert.assertEquals(-1, GameUtil.improveStability(2));
        Assert.assertEquals(-1, GameUtil.improveStability(3));
        Assert.assertEquals(-1, GameUtil.improveStability(4));
        Assert.assertEquals(-1, GameUtil.improveStability(5));
        Assert.assertEquals(0, GameUtil.improveStability(6));
        Assert.assertEquals(0, GameUtil.improveStability(7));
        Assert.assertEquals(0, GameUtil.improveStability(8));
        Assert.assertEquals(0, GameUtil.improveStability(9));
        Assert.assertEquals(0, GameUtil.improveStability(10));
        Assert.assertEquals(1, GameUtil.improveStability(11));
        Assert.assertEquals(1, GameUtil.improveStability(12));
        Assert.assertEquals(1, GameUtil.improveStability(13));
        Assert.assertEquals(1, GameUtil.improveStability(14));
        Assert.assertEquals(2, GameUtil.improveStability(15));
        Assert.assertEquals(2, GameUtil.improveStability(16));
        Assert.assertEquals(2, GameUtil.improveStability(17));
        Assert.assertEquals(3, GameUtil.improveStability(18));
        Assert.assertEquals(3, GameUtil.improveStability(19));
        Assert.assertEquals(3, GameUtil.improveStability(20));
        Assert.assertEquals(3, GameUtil.improveStability(21));
        Assert.assertEquals(3, GameUtil.improveStability(22));
        Assert.assertEquals(3, GameUtil.improveStability(23));
        Assert.assertEquals(3, GameUtil.improveStability(24));
    }

    @Test
    public void testReverseInvestment() {
        Assert.assertEquals(null, GameUtil.reverseInvestment(null));
        Assert.assertEquals(InvestmentEnum.S, GameUtil.reverseInvestment(30));
        Assert.assertEquals(InvestmentEnum.M, GameUtil.reverseInvestment(50));
        Assert.assertEquals(InvestmentEnum.L, GameUtil.reverseInvestment(100));
        Assert.assertEquals(null, GameUtil.reverseInvestment(99));
    }
}
