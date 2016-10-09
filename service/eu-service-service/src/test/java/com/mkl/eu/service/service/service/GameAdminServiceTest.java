package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.domain.ICounterDomain;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.CounterEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.ref.country.CountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.ICountryDao;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
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
    private ICounterDomain counterDomain;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICountryDao countryDao;

    @Mock
    private IStackDao stackDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private DiffMapping diffMapping;

    @Test
    public void testCreateCounterFailSimple() {
        Long idGame = 12L;
        Long versionGame = 1L;
        CounterForCreation counter = new CounterForCreation();
        String province = "IdF";

        try {
            gameAdminService.createCounter(null, null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, null, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, null, null);
            Assert.fail("Should break because counter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, null);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.type", e.getParams()[0]);
        }

        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because counter.country is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        counter.setCountry("FRA");

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterFailComplex() {
        Long idGame = 12L;
        Long versionGame = 1L;
        CounterForCreation counter = new CounterForCreation();
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
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
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("counter.country", e.getParams()[0]);
        }

        when(countryDao.getCountryByName("FRA")).thenReturn(new CountryEntity());

        try {
            gameAdminService.createCounter(idGame, versionGame, counter, province);
            Assert.fail("Should break because province does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("province", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateCounterSuccess() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;
        CounterForCreation counter = new CounterForCreation();
        counter.setType(CounterFaceTypeEnum.ARMY_MINUS);
        counter.setCountry("FRA");
        String province = "IdF";

        EuropeanProvinceEntity idf = new EuropeanProvinceEntity();
        idf.setId(257L);
        idf.setName("IdF");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        when(gameDao.lock(12L)).thenReturn(game);

        when(countryDao.getCountryByName("FRA")).thenReturn(new CountryEntity());

        when(provinceDao.getProvinceByName("IdF")).thenReturn(idf);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameAdminService.createCounter(idGame, versionGame, counter, province);

        InOrder inOrder = inOrder(gameDao, provinceDao, countryDao, counterDomain, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(countryDao).getCountryByName("FRA");
        inOrder.verify(provinceDao).getProvinceByName("IdF");
        inOrder.verify(counterDomain).createCounter(CounterFaceTypeEnum.ARMY_MINUS, "FRA", province, null, game);
        inOrder.verify(gameDao).update(game, true);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testRemoveCounterFailSimple() {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 25L;

        try {
            gameAdminService.removeCounter(null, null, null);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        try {
            gameAdminService.removeCounter(idGame, null, null);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        try {
            gameAdminService.removeCounter(idGame, versionGame, null);
            Assert.fail("Should break because idCounter is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("idCounter", e.getParams()[0]);
        }

        try {
            gameAdminService.removeCounter(idGame, versionGame, idCounter);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            gameAdminService.removeCounter(idGame, versionGame, idCounter);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

        try {
            gameAdminService.removeCounter(idGame, versionGame, idCounter);
            Assert.fail("Should break because counter does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("idCounter", e.getParams()[0]);
        }
    }

    @Test
    public void testRemoveCounterSuccess() throws Exception {
        Long idGame = 12L;
        Long versionGame = 1L;
        Long idCounter = 25L;

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.getStacks().add(new StackEntity());
        game.getStacks().get(0).setId(51L);
        game.getStacks().get(0).setProvince("IdF");
        game.getStacks().get(0).getCounters().add(new CounterEntity());
        game.getStacks().get(0).getCounters().get(0).setId(225L);
        CounterEntity counter = new CounterEntity();
        counter.setId(idCounter);
        counter.setOwner(game.getStacks().get(0));
        game.getStacks().get(0).getCounters().add(counter);

        when(gameDao.lock(12L)).thenReturn(game);

        when(counterDomain.removeCounter(25L, game)).thenReturn(new DiffEntity());

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = gameAdminService.removeCounter(idGame, versionGame, idCounter);

        InOrder inOrder = inOrder(gameDao, counterDomain, diffDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(counterDomain).removeCounter(25L, game);
        inOrder.verify(gameDao).update(game, true);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
    }
}
