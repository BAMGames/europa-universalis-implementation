package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.IProvinceDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.country.ICountryDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
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
    private IStackDao stackDao;

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
        CounterForCreation counter = new CounterForCreation();
        String province = "IdF";

        try {
            gameAdminService.createCounter(null, null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, null, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, null, null);
            Assert.fail("Should break because counter is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, null);
            Assert.fail("Should break because province is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.type is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.type", e.getParams()[0]);
        }

        counter.setType(CounterTypeEnum.ARMY_MINUS);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.country is null");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        counter.setCountry("FRA");

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because game does not exist");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because versions does not match");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterFailComplex() {
        Long idGame = 12L;
        Long versionGame = 1L;
        CounterForCreation counter = new CounterForCreation();
        counter.setType(CounterTypeEnum.ARMY_MINUS);
        counter.setCountry("FRA");
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
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        when(countryDao.getCountryByName("FRA", 12L)).thenReturn(new PlayableCountryEntity());

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because province does not exist");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterSuccess() {
        Long idGame = 12L;
        Long versionGame = 1L;
        CounterForCreation counter = new CounterForCreation();
        counter.setType(CounterTypeEnum.ARMY_MINUS);
        counter.setCountry("FRA");
        String province = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        when(countryDao.getCountryByName("FRA", 12L)).thenReturn(new PlayableCountryEntity());

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

        when(gameDao.update(game, true)).thenAnswer(invocation -> {
            game.getStacks().get(0).setId(1L);
            game.getStacks().get(0).getCounters().get(0).setId(2L);
            return game;
        });

        DiffResponse response = gameAdminService.createCounter(idGame, versionGame, counter, province);

        InOrder inOrder = inOrder(gameDao, provinceDao, countryDao, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(countryDao).getCountryByName("FRA", 12L);
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(gameDao).update(game, true);
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertNull(diffEntity.getIdObject());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.COUNTER, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(5, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.PROVINCE, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals(idf.getName(), diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.TYPE, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals(counter.getType().name(), diffEntity.getAttributes().get(1).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTRY, diffEntity.getAttributes().get(2).getType());
        Assert.assertEquals(counter.getCountry(), diffEntity.getAttributes().get(2).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.STACK, diffEntity.getAttributes().get(3).getType());
        Assert.assertEquals(game.getStacks().get(0).getId().toString(), diffEntity.getAttributes().get(3).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.COUNTER, diffEntity.getAttributes().get(4).getType());
        Assert.assertEquals(game.getStacks().get(0).getCounters().get(0).getId().toString(), diffEntity.getAttributes().get(4).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());

        Assert.assertEquals(1, game.getStacks().size());
        Assert.assertEquals(idf, game.getStacks().get(0).getProvince());
        Assert.assertEquals(game, game.getStacks().get(0).getGame());
        Assert.assertEquals(1, game.getStacks().get(0).getCounters().size());
        Assert.assertEquals(counter.getCountry(), game.getStacks().get(0).getCounters().get(0).getCountry());
        Assert.assertEquals(counter.getType(), game.getStacks().get(0).getCounters().get(0).getType());
        Assert.assertEquals(game.getStacks().get(0), game.getStacks().get(0).getCounters().get(0).getOwner());
    }
}
