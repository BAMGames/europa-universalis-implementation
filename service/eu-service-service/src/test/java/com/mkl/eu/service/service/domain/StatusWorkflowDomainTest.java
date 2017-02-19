package com.mkl.eu.service.service.domain;

import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.domain.impl.StatusWorkflowDomainImpl;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

/**
 * Test of StatusWorkflowDomainImpl.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusWorkflowDomainTest {
    @InjectMocks
    private StatusWorkflowDomainImpl statusWorkflowDomain;

    @Test
    public void testComputeEndAdministrativeActions() throws Exception {
        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
        game.setStatus(GameStatusEnum.ADMINISTRATIVE_ACTIONS_CHOICE);

        List<DiffEntity> diffs = statusWorkflowDomain.computeEndAdministrativeActions(game);

        Assert.assertEquals(1, diffs.size());
        Assert.assertEquals(game.getId(), diffs.get(0).getIdGame());
        Assert.assertEquals(game.getVersion(), diffs.get(0).getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.MODIFY, diffs.get(0).getType());
        Assert.assertEquals(DiffTypeObjectEnum.STATUS, diffs.get(0).getTypeObject());
        Assert.assertEquals(null, diffs.get(0).getIdObject());
        Assert.assertEquals(1, diffs.get(0).getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.STATUS, diffs.get(0).getAttributes().get(0).getType());
        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE.name(), diffs.get(0).getAttributes().get(0).getValue());

        Assert.assertEquals(GameStatusEnum.MILITARY_MOVE, game.getStatus());
    }
}
