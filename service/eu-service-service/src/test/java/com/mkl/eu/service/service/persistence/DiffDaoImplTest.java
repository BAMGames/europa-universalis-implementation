package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Test for DiffDao.
 *
 * @author MKL
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        RollbackTransactionalDataSetTestExecutionListener.class
})
@ContextConfiguration(locations = {"classpath:com/mkl/eu/service/service/eu-service-service-applicationContext.xml",
        "classpath:com/mkl/eu/service/service/test-database-applicationContext.xml"})
@DataSet(value = {"diff.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class DiffDaoImplTest {

    @Autowired
    private IDiffDao diffDao;

    @Test
    public void testGetDiffsSince() {
        List<DiffEntity> games = diffDao.getDiffsSince(null, null);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(null, 1L);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(1L, null);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(3L, 1L);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(3L, 1L);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(1L, 0L);

        Assert.assertEquals(5, games.size());

        games = diffDao.getDiffsSince(1L, 1L);

        Assert.assertEquals(4, games.size());

        games = diffDao.getDiffsSince(1L, 2L);

        Assert.assertEquals(3, games.size());

        games = diffDao.getDiffsSince(1L, 3L);

        Assert.assertEquals(2, games.size());

        games = diffDao.getDiffsSince(1L, 4L);

        Assert.assertEquals(1, games.size());

        games = diffDao.getDiffsSince(1L, 5L);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(1L, 6L);

        Assert.assertEquals(0, games.size());

        games = diffDao.getDiffsSince(2L, 0L);

        Assert.assertEquals(3, games.size());

        games = diffDao.getDiffsSince(2L, 1L);

        Assert.assertEquals(2, games.size());

        games = diffDao.getDiffsSince(2L, 2L);

        Assert.assertEquals(1, games.size());

        games = diffDao.getDiffsSince(2L, 3L);

        Assert.assertEquals(0, games.size());
    }
}
