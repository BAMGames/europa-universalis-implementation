package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.military.ChooseBattleRequest;
import com.mkl.eu.client.service.vo.enumeration.BattleStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.military.BattleEntity;
import com.mkl.eu.service.service.service.impl.MilitaryServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test of MilitaryService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class MilitaryServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private MilitaryServiceImpl militaryService;

    @Test
    public void testChooseBattleFail() {
        Pair<Request<ChooseBattleRequest>, GameEntity> pair = testCheckGame(militaryService::chooseBattle, "chooseBattle");
        Request<ChooseBattleRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(0).setStatus(BattleStatusEnum.SELECT_FORCES);
        game.getBattles().get(0).setProvince("pecs");
        game.getBattles().add(new BattleEntity());
        game.getBattles().get(1).setStatus(BattleStatusEnum.NEW);
        game.getBattles().get(1).setProvince("lyonnais");
        request.setIdCountry(26L);
        testCheckStatus(pair.getRight(), request, militaryService::chooseBattle, "chooseBattle", GameStatusEnum.MILITARY_BATTLES);

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because chooseBattle.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request", e.getParams()[0]);
        }

        request.setRequest(new ChooseBattleRequest());

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because province is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("");

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because province is empty");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }

        request.getRequest().setProvince("idf");

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because another battle is in process");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsServiceException.BATTLE_IN_PROCESS, e.getCode());
            Assert.assertEquals("chooseBattle", e.getParams()[0]);
        }

        game.getBattles().get(0).setStatus(BattleStatusEnum.NEW);

        try {
            militaryService.chooseBattle(request);
            Assert.fail("Should break because no battle is in this province");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("chooseBattle.request.province", e.getParams()[0]);
        }
    }
}
