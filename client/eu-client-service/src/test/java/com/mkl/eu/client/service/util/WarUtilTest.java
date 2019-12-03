package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diplo.CountryInWar;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import com.mkl.eu.client.service.vo.enumeration.WarTypeEnum;
import com.mkl.eu.client.service.vo.ref.country.CountryLight;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;

/**
 * Unit tests for GameUtil.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class WarUtilTest {

    @Test
    public void getAlliesEnemies() {
        Game game = new Game();

        List<String> allies;
        List<String> enemies;

        allies = WarUtil.getAllies("france", game);
        enemies = WarUtil.getEnemies("france", game);

        Assert.assertEquals(1, allies.size());
        Assert.assertEquals(true, allies.contains("france"));
        Assert.assertEquals(0, enemies.size());

        War war1 = new War();
        war1.setType(WarTypeEnum.CLASSIC_WAR);
        CountryInWar countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        war1.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("espagne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        war1.getCountries().add(countryWar);
        game.getWars().add(war1);

        allies = WarUtil.getAllies("france", game);
        enemies = WarUtil.getEnemies("france", game);

        Assert.assertEquals(1, allies.size());
        Assert.assertEquals(true, allies.contains("france"));
        Assert.assertEquals(1, enemies.size());
        Assert.assertEquals(true, enemies.contains("espagne"));

        War war2 = new War();
        war2.setType(WarTypeEnum.RELIGIOUS_WAR);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("espagne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("portugal");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("angleterre");
        countryWar.setImplication(WarImplicationEnum.LIMITED);
        countryWar.setOffensive(true);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("turquie");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        war2.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("hollande");
        countryWar.setImplication(WarImplicationEnum.FOREIGN);
        countryWar.setOffensive(false);
        war2.getCountries().add(countryWar);
        game.getWars().add(war2);

        allies = WarUtil.getAllies("france", game);
        enemies = WarUtil.getEnemies("france", game);

        Assert.assertEquals(2, allies.size());
        Assert.assertEquals(true, allies.contains("france"));
        Assert.assertEquals(true, allies.contains("turquie"));
        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals(true, enemies.contains("espagne"));
        Assert.assertEquals(true, enemies.contains("portugal"));

        War war3 = new War();
        war3.setType(WarTypeEnum.CIVIL_WAR);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("habsbourg");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(true);
        war3.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("france");
        countryWar.setImplication(WarImplicationEnum.LIMITED);
        countryWar.setOffensive(true);
        war3.getCountries().add(countryWar);
        countryWar = new CountryInWar();
        countryWar.setCountry(new CountryLight());
        countryWar.getCountry().setName("pologne");
        countryWar.setImplication(WarImplicationEnum.FULL);
        countryWar.setOffensive(false);
        war3.getCountries().add(countryWar);
        game.getWars().add(war3);

        allies = WarUtil.getAllies("france", game);
        enemies = WarUtil.getEnemies("france", game);

        Assert.assertEquals(2, allies.size());
        Assert.assertEquals(true, allies.contains("france"));
        Assert.assertEquals(true, allies.contains("turquie"));
        Assert.assertEquals(2, enemies.size());
        Assert.assertEquals(true, enemies.contains("espagne"));
        Assert.assertEquals(true, enemies.contains("portugal"));
    }
}
