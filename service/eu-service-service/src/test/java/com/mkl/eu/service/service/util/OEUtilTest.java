package com.mkl.eu.service.service.util;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.MonarchEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.util.impl.OEUtilImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

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
        game.getStacks().get(1).setProvince("B_STAB_-2");
        game.getStacks().get(1).getCounters().add(new CounterEntity());
        game.getStacks().get(1).getCounters().get(0).setType(CounterFaceTypeEnum.STABILITY);
        game.getStacks().get(1).getCounters().get(0).setCountry("angleterre");
        game.getStacks().get(1).getCounters().get(0).setOwner(game.getStacks().get(1));
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
}
