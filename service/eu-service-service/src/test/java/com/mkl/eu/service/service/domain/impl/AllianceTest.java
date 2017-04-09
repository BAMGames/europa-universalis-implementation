package com.mkl.eu.service.service.domain.impl;

import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for alliances.
 *
 * @author MKL.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class AllianceTest {

    @Test
    public void testFusionAlliances() {
        List<Alliance> alliances = new ArrayList<>();

        Alliance.fusion(alliances);

        Assert.assertEquals(0, alliances.size());

        PlayableCountryEntity autriche = new PlayableCountryEntity();
        autriche.setName("autriche");
        PlayableCountryEntity baviere = new PlayableCountryEntity();
        baviere.setName("baviere");
        PlayableCountryEntity crimee = new PlayableCountryEntity();
        crimee.setName("crimee");
        PlayableCountryEntity danemark = new PlayableCountryEntity();
        danemark.setName("danemark");
        PlayableCountryEntity egypte = new PlayableCountryEntity();
        egypte.setName("egypte");
        PlayableCountryEntity france = new PlayableCountryEntity();
        france.setName("france");

        List<PlayableCountryEntity> countries = new ArrayList<>();
        countries.add(autriche);
        countries.add(baviere);

        Alliance alliance = new Alliance(countries, 5);
        alliances.add(alliance);

        countries = new ArrayList<>();
        countries.add(crimee);
        countries.add(danemark);

        alliance = new Alliance(countries, 8);
        alliances.add(alliance);

        Alliance.fusion(alliances);

        Assert.assertEquals(2, alliances.size());
        Assert.assertEquals(5, alliances.get(0).getInitiative());
        Assert.assertEquals(2, alliances.get(0).getCountries().size());
        Assert.assertEquals(8, alliances.get(1).getInitiative());
        Assert.assertEquals(2, alliances.get(1).getCountries().size());

        countries = new ArrayList<>();
        countries.add(danemark);
        countries.add(autriche);

        alliance = new Alliance(countries, 8);
        alliances.add(alliance);

        Alliance.fusion(alliances);

        Assert.assertEquals(1, alliances.size());
        Assert.assertEquals(5, alliances.get(0).getInitiative());
        Assert.assertEquals(4, alliances.get(0).getCountries().size());

        alliances.clear();

        countries = new ArrayList<>();
        countries.add(autriche);
        countries.add(baviere);

        alliance = new Alliance(countries, 5);
        alliances.add(alliance);

        countries = new ArrayList<>();
        countries.add(crimee);
        countries.add(danemark);

        alliance = new Alliance(countries, 8);
        alliances.add(alliance);

        countries = new ArrayList<>();
        countries.add(egypte);
        countries.add(france);

        alliance = new Alliance(countries, 11);
        alliances.add(alliance);

        countries = new ArrayList<>();
        countries.add(autriche);
        countries.add(france);

        alliance = new Alliance(countries, 14);
        alliances.add(alliance);

        countries = new ArrayList<>();
        countries.add(egypte);
        countries.add(danemark);

        alliance = new Alliance(countries, 17);
        alliances.add(alliance);

        Alliance.fusion(alliances);

        Assert.assertEquals(1, alliances.size());
        Assert.assertEquals(5, alliances.get(0).getInitiative());
        Assert.assertEquals(6, alliances.get(0).getCountries().size());
    }
}
