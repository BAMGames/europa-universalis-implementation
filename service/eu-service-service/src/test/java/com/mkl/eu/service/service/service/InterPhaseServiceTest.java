package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.LandLootTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;
import com.mkl.eu.service.service.domain.impl.CounterDomainImpl;
import com.mkl.eu.service.service.domain.impl.StatusWorkflowDomainImpl;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.ref.province.EuropeanProvinceEntity;
import com.mkl.eu.service.service.persistence.ref.IProvinceDao;
import com.mkl.eu.service.service.service.impl.InterPhaseServiceImpl;
import com.mkl.eu.service.service.util.IOEUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;

/**
 * Test of BoardService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class InterPhaseServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private InterPhaseServiceImpl interPhaseService;

    @Mock
    private CounterDomainImpl counterDomain;

    @Mock
    private StatusWorkflowDomainImpl statusWorkflowDomain;

    @Mock
    private IProvinceDao provinceDao;

    @Mock
    private ICounterDao counterDao;

    @Mock
    private IOEUtil oeUtil;

    @Test
    public void testLandLootingFail() {
        Pair<Request<LandLootingRequest>, GameEntity> pair = testCheckGame(interPhaseService::landLooting, "landLooting");
        Request<LandLootingRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        PlayableCountryEntity country = new PlayableCountryEntity();
        country.setName("france");
        country.setId(26L);
        game.getCountries().add(country);
        request.getGame().setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, interPhaseService::landLooting, "landLooting", GameStatusEnum.REDEPLOYMENT);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because landLooting.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request", e.getParams()[0]);
        }

        request.setRequest(new LandLootingRequest());

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because type is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.type", e.getParams()[0]);
        }

        request.getRequest().setType(LandLootTypeEnum.PILLAGE);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because idStack is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        request.getRequest().setIdStack(4L);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        StackEntity stack = new StackEntity();
        stack.setId(4L);
        stack.setCountry("france");
        stack.setProvince("pecs");
        stack.setMovePhase(MovePhaseEnum.LOOTING);
        game.getStacks().add(stack);
        EuropeanProvinceEntity pecs = new EuropeanProvinceEntity();
        pecs.setName("pecs");
        when(provinceDao.getProvinceByName("pecs")).thenReturn(pecs);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_INVALID_STACK, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.isMobile(stack)).thenReturn(true);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack has already looted");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_TWICE, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        stack.setMovePhase(null);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because stack does not belong to country");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.ACCESS_RIGHT, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(counterDao.getPatrons(stack.getCountry(), game.getId())).thenReturn(Collections.singletonList(country.getName()));

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because province does not belong to an enemy");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_NOT_ENEMY, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }

        when(oeUtil.getEnemies(country, game)).thenReturn(Collections.singletonList("spain"));
        when(oeUtil.getOwner(pecs, game)).thenReturn("spain");
        when(oeUtil.getFortressLevel(pecs, game)).thenReturn(5);

        try {
            interPhaseService.landLooting(request);
            Assert.fail("Should break because province cannot be looted because of forces");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.LAND_LOOTING_INSUFFICIENT_FORCES, e.getCode());
            Assert.assertEquals("landLooting.request.idStack", e.getParams()[0]);
        }
    }
}
