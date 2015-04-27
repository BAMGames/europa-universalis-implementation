package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechniqueException;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.IProvinceDao;
import com.mkl.eu.service.service.persistence.country.ICountryDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.impl.GameAdminServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test of GameAdminService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameAdminServiceTest {
    @InjectMocks
    private GameAdminServiceImpl gameAdminService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICountryDao countryDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private DiffMapping diffMapping;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    @Test
    public void testCreateCounterFailSimple() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Counter counter = new Counter();
        String province = "IdF";

        try {
            gameAdminService.createCounter(null, null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, null, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, null, null);
            Assert.fail("Should break because counter is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, null);
            Assert.fail("Should break because province is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.type is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.type", e.getParams()[0]);
        }

        counter.setType(CounterTypeEnum.ARMY_MINUS);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.country is null");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        counter.setCountry(new Country());

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because game does not exist");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because versions does not match");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterFailComplex() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Counter counter = new Counter();
        counter.setType(CounterTypeEnum.ARMY_MINUS);
        counter.setCountry(new Country());
        String province = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.country does not exist");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        counter.getCountry().setId(25L);

        when(countryDao.load(25L)).thenReturn(new CountryEntity());

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because province does not exist");
        } catch (TechniqueException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterSuccess() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Counter counter = new Counter();
        counter.setType(CounterTypeEnum.ARMY_MINUS);
        counter.setCountry(new Country());
        counter.getCountry().setName("FRA");
        String province = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        CountryEntity country = new CountryEntity();
        country.setName("FRA");
        country.setId(25L);

        when(gameDao.lock(12L)).thenReturn(game);

        when(countryDao.getCountryByName("FRA", 12L)).thenReturn(country);

        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameAdminService.createCounter(idGame, versionGame, counter, province);

        InOrder inOrder = inOrder(gameDao, provinceDao, countryDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(countryDao).getCountryByName("FRA", 12L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertNull(diffEntity.getIdObject());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(1, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(0).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals(idf, game.getStacks().get(0).getProvince());
        Assert.assertEquals(game, game.getStacks().get(0).getGame());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals(country, game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(counter.getType(), game.getStacks().get(0).getCounters().get(0).getType());
        Assert.assertEquals(game.getStacks().get(0), game.getStacks().get(0).getCounters().get(0).getOwner());
    }
}
