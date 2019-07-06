package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.domain.IStatusWorkflowDomain;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryInWarEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.persistence.oe.military.SiegeEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.AbstractProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.util.DiffUtil;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.mkl.eu.service.service.service.AbstractGameServiceTest.getAttribute;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Test of StatusWorkflowDomainImpl.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusWorkflowDomainTest {
    @InjectMocks
    private StatusWorkflowDomainImpl statusWorkflowDomain;

    @Mock
    private ICounterDomain counterDomain;

    @Mock
    private IOEUtil oeUtil;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IProvinceDao provinceDao;

    @Test
    public void testComputeEndMinorLogisticsNoCountries() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(0).setGameStatus(GameStatusEnum.DIPLOMACY);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(1).setGameStatus(GameStatusEnum.MILITARY_MOVE);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(2).setGameStatus(GameStatusEnum.DIPLOMACY);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(3).setGameStatus(GameStatusEnum.MILITARY_MOVE);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(4).setGameStatus(GameStatusEnum.DIPLOMACY);
        game.getOrders().add(new CountryOrderEntity());
        game.getOrders().get(5).setGameStatus(GameStatusEnum.MILITARY_MOVE);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(3, game.getOrders().size());
    }

    private void checkDiffsForMilitary(GameEntity game) {
        List<DiffEntity> diffs = statusWorkflowDomain.computeEndMinorLogistics(game);

        DiffEntity diff = diffs.stream()
                .filter(d -> d != null && d.getType() == DiffTypeEnum.MODIFY && d.getTypeObject() == DiffTypeObjectEnum.STATUS)
                .findAny()
                .orElse(null);
        Assert.assertNotNull(diff);
        Assert.assertEquals(game.getId(), diff.getIdGame());
        Assert.assertEquals(game.getVersion(), diff.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diff.getType());
        Assert.assertEquals(DiffTypeObjectEnum.STATUS, diff.getTypeObject());
        Assert.assertEquals(null, diff.getIdObject());
        Assert.assertEquals(1, diff.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diff.getAttributes().get(0).getType());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE.name(), diff.getAttributes().get(0).getValue());
    }

    /**
     * No Wars, 4 countries played, one minor. Order expected:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>espagne (init 12)</li>
     * <li>turquie (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsNoWars() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername(null);
        game.getCountries().add(pologne);

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(12);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(14);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(4, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(3).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 14)</li>
     * <li>espagne (init 12)</li>
     * <li>turquie (init 9)</li>
     * </ul>
     * <p>
     * One war. france and turquie against russie, espagne and pologne (but pologne in LIMITED).
     * Expected order:
     * <ul>
     * <li>pologne (init 14)</li>
     * <li>espagne and russie (init 12)</li>
     * <li>france and turquie (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsSimpleWar() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setOffensive(false);
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName(russie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(4).setOffensive(false);
        game.getWars().get(0).getCountries().get(4).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(4).getCountry().setName(pologne.getName());

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(12);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(14);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(5, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(4).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 14)</li>
     * <li>espagne (init 12)</li>
     * <li>turquie (init 9)</li>
     * <li>venise (init 10)</li>
     * </ul>
     * <p>
     * Two wars.
     * First one: france and turquie against russie, espagne and pologne (but pologne in LIMITED).
     * Second one: pologne and venise against hongrie (minor).
     * <p>
     * Expected order:
     * <ul>
     * <li>espagne and russie (init 12)</li>
     * <li>pologne and venise (init 10)</li>
     * <li>france and turquie (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsSimpleWars() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        PlayableCountryEntity venise = new PlayableCountryEntity();
        venise.setName("venise");
        venise.setUsername("venise");
        game.getCountries().add(venise);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setOffensive(false);
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName(russie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(4).setOffensive(false);
        game.getWars().get(0).getCountries().get(4).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(4).getCountry().setName(pologne.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName(pologne.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setOffensive(true);
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName(venise.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(2).setOffensive(false);
        game.getWars().get(1).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(2).getCountry().setName("hungaria");

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(12);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(14);
        when(oeUtil.getInitiative(venise)).thenReturn(10);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(6, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(venise.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(4).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(5).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(5).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(5).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(5).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 14)</li>
     * <li>espagne (init 12)</li>
     * <li>turquie (init 9)</li>
     * <li>venise (init 10)</li>
     * </ul>
     * <p>
     * Two wars.
     * First one: france and turquie against russie, espagne and pologne (but pologne in LIMITED).
     * Second one: france against venise.
     * <p>
     * Expected order:
     * <ul>
     * <li>pologne (init 14)</li>
     * <li>espagne and russie (init 12)</li>
     * <li>venise (init 10)</li>
     * <li>france and turquie (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsMediumWars() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        PlayableCountryEntity venise = new PlayableCountryEntity();
        venise.setName("venise");
        venise.setUsername("venise");
        game.getCountries().add(venise);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setOffensive(false);
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName(russie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(4).setOffensive(false);
        game.getWars().get(0).getCountries().get(4).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(4).getCountry().setName(pologne.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setOffensive(false);
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName(venise.getName());


        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(12);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(14);
        when(oeUtil.getInitiative(venise)).thenReturn(10);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(6, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(venise.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(4).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(5).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(5).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(5).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(5).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 14)</li>
     * <li>espagne (init 12)</li>
     * <li>turquie (init 9)</li>
     * <li>venise (init 10)</li>
     * <li>hollande (init 25)</li>
     * </ul>
     * <p>
     * Two wars.
     * First one: france and turquie against russie, espagne and pologne (but pologne in LIMITED).
     * Second one: france and hollande against venise.
     * <p>
     * Expected order:
     * <ul>
     * <li>pologne (init 14)</li>
     * <li>espagne and russie (init 12)</li>
     * <li>venise (init 10)</li>
     * <li>france, hollande and turquie (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsMediumWars2() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        PlayableCountryEntity venise = new PlayableCountryEntity();
        venise.setName("venise");
        venise.setUsername("venise");
        game.getCountries().add(venise);
        PlayableCountryEntity hollande = new PlayableCountryEntity();
        hollande.setName("hollande");
        hollande.setUsername("hollande");
        game.getCountries().add(hollande);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setOffensive(false);
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName(russie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(4).setOffensive(false);
        game.getWars().get(0).getCountries().get(4).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(4).getCountry().setName(pologne.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setOffensive(false);
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName(venise.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(2).setOffensive(true);
        game.getWars().get(1).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(2).getCountry().setName(hollande.getName());

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(12);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(14);
        when(oeUtil.getInitiative(venise)).thenReturn(10);
        when(oeUtil.getInitiative(hollande)).thenReturn(25);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(7, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(venise.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(4).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(5).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(5).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(5).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(5).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(6).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(6).getGame());
        Assert.assertEquals(hollande.getName(), game.getOrders().get(6).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(6).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 9)</li>
     * <li>espagne (init 9)</li>
     * <li>turquie (init 9)</li>
     * <li>venise (init 10)</li>
     * <li>hollande (init 25)</li>
     * </ul>
     * <p>
     * Two wars.
     * First one: france and turquie against russie, espagne and pologne (but pologne in LIMITED).
     * Second one: france and hollande against venise.
     * <p>
     * Expected order:
     * <ul>
     * <li>venise (init 10)</li>
     * <li>france, hollande and turquie (init 9)</li>
     * <li>espagne and russie (init 9)</li>
     * <li>pologne (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsComplexWars() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        PlayableCountryEntity venise = new PlayableCountryEntity();
        venise.setName("venise");
        venise.setUsername("venise");
        game.getCountries().add(venise);
        PlayableCountryEntity hollande = new PlayableCountryEntity();
        hollande.setName("hollande");
        hollande.setUsername("hollande");
        game.getCountries().add(hollande);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(3).setOffensive(false);
        game.getWars().get(0).getCountries().get(3).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(3).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(3).getCountry().setName(russie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(4).setOffensive(false);
        game.getWars().get(0).getCountries().get(4).setImplication(WarImplicationEnum.LIMITED);
        game.getWars().get(0).getCountries().get(4).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(4).getCountry().setName(pologne.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setOffensive(false);
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName(venise.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(2).setOffensive(true);
        game.getWars().get(1).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(2).getCountry().setName(hollande.getName());

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(9);
        when(oeUtil.getInitiative(turquie)).thenReturn(9);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(9);
        when(oeUtil.getInitiative(venise)).thenReturn(10);
        when(oeUtil.getInitiative(hollande)).thenReturn(25);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(7, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(venise.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(hollande.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(4).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(5).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(5).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(5).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(5).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(6).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(6).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(6).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(6).getPosition());
    }

    /**
     * 5 countries played:
     * <ul>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>pologne (init 9)</li>
     * <li>espagne (init 10)</li>
     * <li>turquie (init 11)</li>
     * <li>venise (init 12)</li>
     * <li>hollande (init 25)</li>
     * </ul>
     * <p>
     * Three wars.
     * First one: venise and turquie against russie.
     * Second one: france against venise and espagne.
     * Third one: espagne against turquie.
     * <p>
     * Expected order:
     * <ul>
     * <li>hollande (init 25)</li>
     * <li>france (init 21)</li>
     * <li>russie (init 15)</li>
     * <li>venise, espagne and turquie (init 10)</li>
     * <li>pologne (init 9)</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test
    public void testComputeEndMinorLogisticsSpartaWars() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        france.setUsername("france");
        game.getCountries().add(france);
        PlayableCountryEntity espagne = new PlayableCountryEntity();
        espagne.setName("espagne");
        espagne.setUsername("espagne");
        game.getCountries().add(espagne);
        PlayableCountryEntity turquie = new PlayableCountryEntity();
        turquie.setName("turquie");
        turquie.setUsername("turquie");
        game.getCountries().add(turquie);
        PlayableCountryEntity russie = new PlayableCountryEntity();
        russie.setName("russie");
        russie.setUsername("russie");
        game.getCountries().add(russie);
        PlayableCountryEntity pologne = new PlayableCountryEntity();
        pologne.setName("pologne");
        pologne.setUsername("pologne");
        game.getCountries().add(pologne);
        PlayableCountryEntity venise = new PlayableCountryEntity();
        venise.setName("venise");
        venise.setUsername("venise");
        game.getCountries().add(venise);
        PlayableCountryEntity hollande = new PlayableCountryEntity();
        hollande.setName("hollande");
        hollande.setUsername("hollande");
        game.getCountries().add(hollande);
        game.getWars().add(new WarEntity());
        game.getWars().get(0).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(0).setOffensive(true);
        game.getWars().get(0).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(0).getCountry().setName(venise.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(1).setOffensive(true);
        game.getWars().get(0).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(1).getCountry().setName(turquie.getName());
        game.getWars().get(0).getCountries().add(new CountryInWarEntity());
        game.getWars().get(0).getCountries().get(2).setOffensive(false);
        game.getWars().get(0).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(0).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(0).getCountries().get(2).getCountry().setName(russie.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(1).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(0).setOffensive(true);
        game.getWars().get(1).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(0).getCountry().setName(france.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(1).setOffensive(false);
        game.getWars().get(1).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(1).getCountry().setName(venise.getName());
        game.getWars().get(1).getCountries().add(new CountryInWarEntity());
        game.getWars().get(1).getCountries().get(2).setOffensive(false);
        game.getWars().get(1).getCountries().get(2).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(1).getCountries().get(2).setCountry(new CountryEntity());
        game.getWars().get(1).getCountries().get(2).getCountry().setName(espagne.getName());
        game.getWars().add(new WarEntity());
        game.getWars().get(2).setType(WarTypeEnum.CLASSIC_WAR);
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(0).setOffensive(true);
        game.getWars().get(2).getCountries().get(0).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(2).getCountries().get(0).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(0).getCountry().setName(espagne.getName());
        game.getWars().get(2).getCountries().add(new CountryInWarEntity());
        game.getWars().get(2).getCountries().get(1).setOffensive(false);
        game.getWars().get(2).getCountries().get(1).setImplication(WarImplicationEnum.FULL);
        game.getWars().get(2).getCountries().get(1).setCountry(new CountryEntity());
        game.getWars().get(2).getCountries().get(1).getCountry().setName(turquie.getName());

        when(oeUtil.getInitiative(france)).thenReturn(21);
        when(oeUtil.getInitiative(espagne)).thenReturn(10);
        when(oeUtil.getInitiative(turquie)).thenReturn(11);
        when(oeUtil.getInitiative(russie)).thenReturn(15);
        when(oeUtil.getInitiative(pologne)).thenReturn(9);
        when(oeUtil.getInitiative(venise)).thenReturn(12);
        when(oeUtil.getInitiative(hollande)).thenReturn(25);

        checkDiffsForMilitary(game);

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
        Assert.assertEquals(7, game.getOrders().size());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(0).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(0).getGame());
        Assert.assertEquals(hollande.getName(), game.getOrders().get(0).getCountry().getName());
        Assert.assertEquals(0, game.getOrders().get(0).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(1).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(1).getGame());
        Assert.assertEquals(france.getName(), game.getOrders().get(1).getCountry().getName());
        Assert.assertEquals(1, game.getOrders().get(1).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(2).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(2).getGame());
        Assert.assertEquals(russie.getName(), game.getOrders().get(2).getCountry().getName());
        Assert.assertEquals(2, game.getOrders().get(2).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(3).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(3).getGame());
        Assert.assertEquals(venise.getName(), game.getOrders().get(3).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(3).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(4).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(4).getGame());
        Assert.assertEquals(turquie.getName(), game.getOrders().get(4).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(4).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(5).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(5).getGame());
        Assert.assertEquals(espagne.getName(), game.getOrders().get(5).getCountry().getName());
        Assert.assertEquals(3, game.getOrders().get(5).getPosition());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getOrders().get(6).getGameStatus());
        Assert.assertEquals(game, game.getOrders().get(6).getGame());
        Assert.assertEquals(pologne.getName(), game.getOrders().get(6).getCountry().getName());
        Assert.assertEquals(4, game.getOrders().get(6).getPosition());
    }

    @Test
    public void testNextRound() {
        GameEntity game = new GameEntity();
        StackEntity roundStack = new StackEntity();
        roundStack.setProvince("B_MR_END");
        CounterEntity roundCounter = new CounterEntity();
        roundCounter.setType(CounterFaceTypeEnum.GOOD_WEATHER);
        roundCounter.setOwner(roundStack);
        roundStack.getCounters().add(roundCounter);
        game.getStacks().add(roundStack);
        CountryOrderEntity order = new CountryOrderEntity();
        order.setActive(true);
        order.setReady(true);
        order.setPosition(6);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(true);
        order.setReady(false);
        order.setPosition(5);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setActive(false);
        order.setReady(true);
        order.setPosition(0);
        game.getOrders().add(order);

        DiffEntity winter0 = new DiffEntity();
        winter0.setId(1L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W0", game)).thenReturn(winter0);
        DiffEntity summer1 = new DiffEntity();
        summer1.setId(2L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_S1", game)).thenReturn(summer1);
        DiffEntity summer2 = new DiffEntity();
        summer2.setId(3L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_S2", game)).thenReturn(summer2);
        DiffEntity winter2 = new DiffEntity();
        winter2.setId(4L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W2", game)).thenReturn(winter2);
        DiffEntity summer3 = new DiffEntity();
        summer3.setId(5L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_S3", game)).thenReturn(summer3);
        DiffEntity winter3 = new DiffEntity();
        winter3.setId(6L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W3", game)).thenReturn(winter3);
        DiffEntity winter4 = new DiffEntity();
        winter4.setId(7L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W4", game)).thenReturn(winter4);
        DiffEntity summer5 = new DiffEntity();
        summer5.setId(8L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_S5", game)).thenReturn(summer5);
        DiffEntity winter5 = new DiffEntity();
        winter5.setId(9L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W5", game)).thenReturn(winter5);
        DiffEntity end = new DiffEntity();
        end.setId(-1L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_End", game)).thenReturn(end);

        checkNextRound(game, "B_MR_END", 1, true, winter0, false);

        checkNextRound(game, "B_MR_END", 3, true, summer1, false);

        checkNextRound(game, "B_MR_END", 5, true, summer2, false);

        checkNextRound(game, "B_MR_END", 6, true, winter2, false);

        checkNextRound(game, "B_MR_W0", 1, false, summer1, false);

        checkNextRound(game, "B_MR_S1", 7, false, summer2, false);

        checkNextRound(game, "B_MR_S2", 4, false, winter2, false);

        checkNextRound(game, "B_MR_W2", 8, false, winter3, false);

        checkNextRound(game, "B_MR_W3", 10, false, summer5, false);

        checkNextRound(game, "B_MR_W4", 2, false, summer5, false);

        checkNextRound(game, "B_MR_W4", 8, false, winter5, false);

        checkNextRound(game, "B_MR_W4", 9, false, end, true);

        checkNextRound(game, "B_MR_S5", 5, false, winter5, false);

        checkNextRound(game, "B_MR_S5", 6, false, end, true);

        checkNextRound(game, "B_MR_W5", 1, false, end, true);

        checkNextRound(game, "B_MR_W5", 8, false, end, true);

        checkNextRound(game, "B_MR_W5", 10, false, end, true);
    }

    private void checkNextRound(GameEntity game, String roundBefore, int die, boolean init, DiffEntity roundMove, boolean end) {
        game.getStacks().get(0).setProvince(roundBefore);
        when(oeUtil.rollDie(game, (PlayableCountryEntity) null)).thenReturn(die);

        List<DiffEntity> diffs = statusWorkflowDomain.nextRound(game, init);

        long alreadyReady = game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.isReady())
                .count();

        long activeNotZero = game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        o.isActive() && o.getPosition() != 0)
                .count();

        long zeroNotActive = game.getOrders().stream()
                .filter(o -> o.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
                        !o.isActive() && o.getPosition() == 0)
                .count();

        Assert.assertEquals(0, alreadyReady);
        Assert.assertEquals(0, activeNotZero);
        Assert.assertEquals(0, zeroNotActive);

        if (end) {
            Assert.assertEquals(1, diffs.size());

            Assert.assertEquals(roundMove, diffs.get(0));
        } else {

            Assert.assertEquals(4, diffs.size());

            Assert.assertEquals(roundMove, diffs.get(0));

            Assert.assertEquals(game.getId(), diffs.get(1).getIdGame());
            Assert.assertEquals(game.getVersion(), diffs.get(1).getVersionGame().longValue());
            Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(1).getType());
            Assert.assertEquals(DiffTypeObjectEnum.STACK, diffs.get(1).getTypeObject());
            Assert.assertEquals(null, diffs.get(1).getIdObject());
            Assert.assertEquals(1, diffs.get(1).getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.MOVE_PHASE, diffs.get(1).getAttributes().get(0).getType());
            Assert.assertEquals(MovePhaseEnum.NOT_MOVED.name(), diffs.get(1).getAttributes().get(0).getValue());

            Assert.assertEquals(game.getId(), diffs.get(2).getIdGame());
            Assert.assertEquals(game.getVersion(), diffs.get(2).getVersionGame().longValue());
            Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(2).getType());
            Assert.assertEquals(DiffTypeObjectEnum.STATUS, diffs.get(2).getTypeObject());
            Assert.assertEquals(null, diffs.get(2).getIdObject());
            Assert.assertEquals(1, diffs.get(2).getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffs.get(2).getAttributes().get(0).getType());
            Assert.assertEquals(GameStatusEnum.MILITARY_MOVE.name(), diffs.get(2).getAttributes().get(0).getValue());

            Assert.assertEquals(game.getId(), diffs.get(3).getIdGame());
            Assert.assertEquals(game.getVersion(), diffs.get(3).getVersionGame().longValue());
            Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(3).getType());
            Assert.assertEquals(DiffTypeObjectEnum.TURN_ORDER, diffs.get(3).getTypeObject());
            Assert.assertEquals(null, diffs.get(3).getIdObject());
            Assert.assertEquals(1, diffs.get(3).getAttributes().size());
            Assert.assertEquals(DiffAttributeTypeEnum.ACTIVE, diffs.get(3).getAttributes().get(0).getType());
            Assert.assertEquals("0", diffs.get(3).getAttributes().get(0).getValue());
        }
    }

    @Test
    public void testEndRound() {
        GameEntity game = new GameEntity();

        DiffEntity end = new DiffEntity();
        end.setId(1L);
        when(counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_End", game)).thenReturn(end);

        List<DiffEntity> diffs = statusWorkflowDomain.endRound(game);

        Assert.assertEquals(1, diffs.size());
        Assert.assertEquals(end, diffs.get(0));
    }

    @Test
    public void testEndMilitaryPhaseStillBattle() {
        int currentTurn = 12;
        int notCurrentTurn = 11;
        GameEntity game = new GameEntity();
        game.setTurn(currentTurn);
        game.setStatus(GameStatusEnum.MILITARY_BATTLES);
        BattleEntity battle = new BattleEntity();
        battle.setStatus(BattleStatusEnum.NEW);
        battle.setTurn(currentTurn);
        game.getBattles().add(battle);
        SiegeEntity siege = new SiegeEntity();
        siege.setStatus(SiegeStatusEnum.NEW);
        siege.setTurn(notCurrentTurn);
        game.getSieges().add(siege);

        List<DiffEntity> diffs = statusWorkflowDomain.endMilitaryPhase(game);

        Assert.assertEquals(0, diffs.size());
        Assert.assertEquals(GameStatusEnum.MILITARY_BATTLES, game.getStatus());
    }

    @Test
    public void testEndMilitaryPhaseStillSiege() {
        int currentTurn = 12;
        int notCurrentTurn = 11;
        GameEntity game = new GameEntity();
        game.setTurn(currentTurn);
        game.setStatus(GameStatusEnum.MILITARY_SIEGES);
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");
        game.getCountries().add(france);
        PlayableCountryEntity spain = new PlayableCountryEntity();
        spain.setName("spain");
        game.getCountries().add(spain);

        CountryOrderEntity order = new CountryOrderEntity();
        order.setCountry(france);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setPosition(0);
        order.setActive(true);
        game.getOrders().add(order);
        order = new CountryOrderEntity();
        order.setCountry(spain);
        order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
        order.setPosition(1);
        order.setActive(false);
        game.getOrders().add(order);

        WarEntity war = new WarEntity();
        CountryInWarEntity countryWar = new CountryInWarEntity();
        countryWar.setOffensive(true);
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName(france.getName());
        war.getCountries().add(countryWar);
        countryWar = new CountryInWarEntity();
        countryWar.setOffensive(false);
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setCountry(new CountryEntity());
        countryWar.getCountry().setName(spain.getName());
        war.getCountries().add(countryWar);
        game.getWars().add(war);

        BattleEntity battle = new BattleEntity();
        battle.setStatus(BattleStatusEnum.NEW);
        battle.setTurn(notCurrentTurn);
        battle.setWar(war);
        game.getBattles().add(battle);

        SiegeEntity siege = new SiegeEntity();
        siege.setStatus(SiegeStatusEnum.NEW);
        siege.setTurn(currentTurn);
        siege.setWar(war);
        siege.setBesiegingOffensive(true);
        game.getSieges().add(siege);

        List<DiffEntity> diffs = statusWorkflowDomain.endMilitaryPhase(game);

        Assert.assertEquals(0, diffs.size());
        Assert.assertEquals(GameStatusEnum.MILITARY_SIEGES, game.getStatus());
    }

    @Test
    public void testEndMilitaryPhase() {
        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_MOVE)
                .futureBattle()
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_BATTLES)
                        .changeStatus()
                        .addBattle());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_MOVE)
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_MOVE)
                        .changeActivePlayer());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_MOVE)
                .lastPlayerInTurnOrder()
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_SIEGES)
                        .changeStatus()
                        .changeActivePlayer()
                        .addSiege());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_MOVE)
                .lastPlayerInTurnOrder()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_MOVE)
                        .changeStatus()
                        .changeActivePlayer()
                        .nextRound());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_BATTLES)
                .pendingBattle()
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_BATTLES));

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_BATTLES)
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_MOVE)
                        .changeStatus()
                        .changeActivePlayer());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_BATTLES)
                .lastPlayerInTurnOrder()
                .futureSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_SIEGES)
                        .changeStatus()
                        .changeActivePlayer()
                        .addSiege());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_BATTLES)
                .lastPlayerInTurnOrder()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_MOVE)
                        .changeStatus()
                        .changeActivePlayer()
                        .nextRound());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_SIEGES)
                .pendingSiege()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_SIEGES));

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_SIEGES)
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_SIEGES)
                        .changeActivePlayer());

        EndMilitaryPhaseBuilder.create()
                .status(GameStatusEnum.MILITARY_SIEGES)
                .lastPlayerInTurnOrder()
                .whenEndMilitaryPhase(statusWorkflowDomain, this)
                .thenExpect(EndMilitaryPhaseResultBuilder.create()
                        .status(GameStatusEnum.MILITARY_MOVE)
                        .changeStatus()
                        .changeActivePlayer()
                        .nextRound());
    }

    static class EndMilitaryPhaseBuilder {
        boolean lastPlayerInTurnOrder;
        boolean pendingBattle;
        boolean pendingSiege;
        boolean futureBattle;
        boolean futureSiege;
        GameStatusEnum status;
        GameEntity game;
        List<DiffEntity> diffs;

        static EndMilitaryPhaseBuilder create() {
            return new EndMilitaryPhaseBuilder();
        }

        EndMilitaryPhaseBuilder lastPlayerInTurnOrder() {
            lastPlayerInTurnOrder = true;
            return this;
        }

        EndMilitaryPhaseBuilder pendingBattle() {
            pendingBattle = true;
            return this;
        }

        EndMilitaryPhaseBuilder pendingSiege() {
            pendingSiege = true;
            return this;
        }

        EndMilitaryPhaseBuilder futureBattle() {
            futureBattle = true;
            return this;
        }

        EndMilitaryPhaseBuilder futureSiege() {
            futureSiege = true;
            return this;
        }

        EndMilitaryPhaseBuilder status(GameStatusEnum status) {
            this.status = status;
            return this;
        }

        EndMilitaryPhaseBuilder whenEndMilitaryPhase(IStatusWorkflowDomain statusWorkflowDomain, StatusWorkflowDomainTest testClass) {
            int currentTurn = 12;
            int notCurrentTurn = 11;
            game = new GameEntity();
            game.setTurn(currentTurn);
            game.setStatus(status);
            PlayableCountryEntity france = new PlayableCountryEntity();
            france.setName("france");
            game.getCountries().add(france);
            PlayableCountryEntity spain = new PlayableCountryEntity();
            spain.setName("spain");
            game.getCountries().add(spain);

            CountryOrderEntity order = new CountryOrderEntity();
            order.setCountry(france);
            order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
            order.setPosition(0);
            order.setActive(!lastPlayerInTurnOrder);
            game.getOrders().add(order);
            order = new CountryOrderEntity();
            order.setCountry(spain);
            order.setGameStatus(GameStatusEnum.MILITARY_MOVE);
            order.setPosition(1);
            order.setActive(lastPlayerInTurnOrder);
            game.getOrders().add(order);

            WarEntity war = new WarEntity();
            war.setId(666l);
            CountryInWarEntity countryWar = new CountryInWarEntity();
            countryWar.setOffensive(true);
            countryWar.setImplication(WarImplicationEnum.FULL);
            countryWar.setCountry(new CountryEntity());
            countryWar.getCountry().setName(france.getName());
            war.getCountries().add(countryWar);
            countryWar = new CountryInWarEntity();
            countryWar.setOffensive(false);
            countryWar.setImplication(WarImplicationEnum.FULL);
            countryWar.setCountry(new CountryEntity());
            countryWar.getCountry().setName(spain.getName());
            war.getCountries().add(countryWar);
            game.getWars().add(war);

            BattleEntity battle = new BattleEntity();
            battle.setStatus(BattleStatusEnum.NEW);
            battle.setTurn(notCurrentTurn);
            battle.setWar(war);
            game.getBattles().add(battle);

            SiegeEntity siege = new SiegeEntity();
            siege.setStatus(SiegeStatusEnum.NEW);
            siege.setTurn(notCurrentTurn);
            siege.setWar(war);
            game.getSieges().add(siege);

            if (pendingBattle) {
                battle = new BattleEntity();
                battle.setStatus(BattleStatusEnum.NEW);
                battle.setTurn(currentTurn);
                battle.setWar(war);
                battle.setPhasingOffensive(!lastPlayerInTurnOrder);
                game.getBattles().add(battle);
            }
            if (pendingSiege) {
                siege = new SiegeEntity();
                siege.setStatus(SiegeStatusEnum.NEW);
                siege.setTurn(currentTurn);
                siege.setWar(war);
                siege.setBesiegingOffensive(!lastPlayerInTurnOrder);
                game.getSieges().add(siege);
            }
            if (futureBattle) {
                StackEntity stack = new StackEntity();
                stack.setProvince("idf");
                stack.setMovePhase(MovePhaseEnum.FIGHTING);
                game.getStacks().add(stack);
            }
            if (futureSiege) {
                StackEntity stack = new StackEntity();
                stack.setProvince("idf");
                stack.setMovePhase(MovePhaseEnum.BESIEGING);
                game.getStacks().add(stack);
            }

            when(testClass.oeUtil.searchWar(anyList(), anyList(), any())).thenReturn(new ImmutablePair<>(war, false));
            AbstractProvinceEntity idf = new EuropeanProvinceEntity();
            when(testClass.provinceDao.getProvinceByName("idf")).thenReturn(idf);
            when(testClass.oeUtil.getController(idf, game)).thenReturn(france.getName());
            when(testClass.counterDomain.moveSpecialCounter(CounterFaceTypeEnum.GOOD_WEATHER, null, "B_MR_W-1", game)).thenReturn(DiffUtil.createDiff(game, DiffTypeEnum.MOVE, DiffTypeObjectEnum.COUNTER));

            diffs = statusWorkflowDomain.endMilitaryPhase(game);

            return this;
        }

        EndMilitaryPhaseBuilder thenExpect(EndMilitaryPhaseResultBuilder result) {
            Assert.assertEquals("The status of the game is not correct.", result.status, game.getStatus());
            DiffEntity changeStatus = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.STATUS)
                    .findAny()
                    .orElse(null);
            if (result.changeStatus) {
                Assert.assertNotNull("A modify status diff event was expected.", changeStatus);
                Assert.assertEquals("The modify status diff event has the wrong new status.", result.status.name(), getAttribute(changeStatus, DiffAttributeTypeEnum.STATUS));
            } else {
                Assert.assertNull("A modify status diff event was not expected.", changeStatus);
            }
            DiffEntity changeActivePlayer = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MODIFY && diff.getTypeObject() == DiffTypeObjectEnum.TURN_ORDER)
                    .findAny()
                    .orElse(null);
            if (result.changeActivePlayer) {
                Assert.assertNotNull("A modify turn order diff event was expected.", changeActivePlayer);
                Assert.assertEquals("The modify turn order diff event has the wrong new active player.", result.nextRound ? "0" : "1", getAttribute(changeActivePlayer, DiffAttributeTypeEnum.ACTIVE));
            } else {
                Assert.assertNull("A modify turn order diff event was not expected.", changeActivePlayer);
            }
            DiffEntity addBattle = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.ADD && diff.getTypeObject() == DiffTypeObjectEnum.BATTLE)
                    .findAny()
                    .orElse(null);
            if (result.addBattle) {
                Assert.assertNotNull("A add battle diff event was expected.", addBattle);
                Assert.assertEquals("The add battle diff event has the wrong province.", "idf", getAttribute(addBattle, DiffAttributeTypeEnum.PROVINCE));
                Assert.assertEquals("The add battle diff event has the wrong turn.", game.getTurn().toString(), getAttribute(addBattle, DiffAttributeTypeEnum.TURN));
                Assert.assertEquals("The add battle diff event has the wrong status.", BattleStatusEnum.NEW.name(), getAttribute(addBattle, DiffAttributeTypeEnum.STATUS));
                Assert.assertEquals("The add battle diff event has the wrong war.", "666", getAttribute(addBattle, DiffAttributeTypeEnum.ID_WAR));
                Assert.assertEquals("The add battle diff event has the wrong phasing offensive.", "false", getAttribute(addBattle, DiffAttributeTypeEnum.PHASING_OFFENSIVE));
            } else {
                Assert.assertNull("A add battle order diff event was not expected.", addBattle);
            }
            int expectedBattles = 1 + (pendingBattle ? 1 : 0) + (result.addBattle ? 1 : 0);
            Assert.assertEquals("The game has not the right number of battles.", expectedBattles, game.getBattles().size());
            DiffEntity addSiege = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.ADD && diff.getTypeObject() == DiffTypeObjectEnum.SIEGE)
                    .findAny()
                    .orElse(null);
            if (result.addSiege) {
                Assert.assertNotNull("A add siege diff event was expected.", addSiege);
                Assert.assertEquals("The add siege diff event has the wrong province.", "idf", getAttribute(addSiege, DiffAttributeTypeEnum.PROVINCE));
                Assert.assertEquals("The add siege diff event has the wrong turn.", game.getTurn().toString(), getAttribute(addSiege, DiffAttributeTypeEnum.TURN));
                Assert.assertEquals("The add siege diff event has the wrong status.", SiegeStatusEnum.NEW.name(), getAttribute(addSiege, DiffAttributeTypeEnum.STATUS));
                Assert.assertEquals("The add siege diff event has the wrong war.", "666", getAttribute(addSiege, DiffAttributeTypeEnum.ID_WAR));
                Assert.assertEquals("The add siege diff event has the wrong phasing offensive.", "false", getAttribute(addSiege, DiffAttributeTypeEnum.PHASING_OFFENSIVE));
            } else {
                Assert.assertNull("A add siege order diff event was not expected.", addSiege);
            }
            int expectedSieges = 1 + (pendingSiege ? 1 : 0) + (result.addSiege ? 1 : 0);
            Assert.assertEquals("The game has not the right number of sieges.", expectedSieges, game.getSieges().size());
            DiffEntity nextRound = diffs.stream()
                    .filter(diff -> diff.getType() == DiffTypeEnum.MOVE && diff.getTypeObject() == DiffTypeObjectEnum.COUNTER)
                    .findAny()
                    .orElse(null);
            if (result.nextRound) {
                Assert.assertNotNull("A next round diff event was expected.", nextRound);
            } else {
                Assert.assertNull("A next round diff event was not expected.", nextRound);
            }

            return this;
        }
    }

    static class EndMilitaryPhaseResultBuilder {
        boolean changeActivePlayer;
        boolean changeStatus;
        boolean addBattle;
        boolean addSiege;
        boolean nextRound;
        GameStatusEnum status;

        static EndMilitaryPhaseResultBuilder create() {
            return new EndMilitaryPhaseResultBuilder();
        }

        EndMilitaryPhaseResultBuilder changeActivePlayer() {
            changeActivePlayer = true;
            return this;
        }

        EndMilitaryPhaseResultBuilder changeStatus() {
            changeStatus = true;
            return this;
        }

        EndMilitaryPhaseResultBuilder addBattle() {
            addBattle = true;
            return this;
        }

        EndMilitaryPhaseResultBuilder addSiege() {
            addSiege = true;
            return this;
        }

        EndMilitaryPhaseResultBuilder nextRound() {
            nextRound = true;
            return this;
        }

        EndMilitaryPhaseResultBuilder status(GameStatusEnum status) {
            this.status = status;
            return this;
        }
    }
}
