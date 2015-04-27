package com.mkl.eu.service.service.service;

import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.impl.GameServiceImpl;
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
 * Test of GameService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class GameServiceTest {
    @InjectMocks
    private GameServiceImpl gameService;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private DiffMapping diffMapping;

    @Test
    public void testUpdateGame() {
        Long idGame = 12L;
        Long versionGame = 1L;

        List<DiffEntity> diffs = new ArrayList<>();
        DiffEntity diff1 = new DiffEntity();
        diff1.setVersionGame(1L);
        diffs.add(diff1);
        DiffEntity diff2 = new DiffEntity();
        diff2.setVersionGame(2L);
        diffs.add(diff2);
        DiffEntity diff3 = new DiffEntity();
        diff3.setVersionGame(3L);
        diffs.add(diff3);
        DiffEntity diff4 = new DiffEntity();
        diff4.setVersionGame(4L);
        diffs.add(diff4);
        DiffEntity diff5 = new DiffEntity();
        diff5.setVersionGame(5L);
        diffs.add(diff5);

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffs);

        List<Diff> diffVos = new ArrayList<>();
        diffVos.add(new Diff());
        diffVos.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffVos);

        DiffResponse response = gameService.updateGame(idGame, versionGame);

        InOrder inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(5L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());

        diff2.setVersionGame(7L);
        diff4.setVersionGame(7L);

        response = gameService.updateGame(idGame, versionGame);

        inOrder = inOrder(diffDao, diffMapping);

        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(7L, response.getVersionGame().longValue());
        Assert.assertEquals(diffVos, response.getDiffs());
    }
}
