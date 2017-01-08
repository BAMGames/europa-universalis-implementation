package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.service.service.persistence.board.IStackDao;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;
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
 * Test for CounterDao.
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
@DataSet(value = {"/com/mkl/eu/service/service/persistence/provinces.xml", "stack.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class StackDaoImplTest {

    @Autowired
    private IStackDao stackDao;

    @Test
    public void testGetMovingStacks() {
        List<StackEntity> stacks;

        stacks = stackDao.getMovingStacks(null);

        Assert.assertEquals(0, stacks.size());

        stacks = stackDao.getMovingStacks(1L);

        Assert.assertEquals(2, stacks.size());
        Assert.assertEquals(1L, stacks.get(0).getId().longValue());
        Assert.assertEquals(4L, stacks.get(1).getId().longValue());

        stacks = stackDao.getMovingStacks(2L);

        Assert.assertEquals(0, stacks.size());

        stacks = stackDao.getMovingStacks(3L);

        Assert.assertEquals(0, stacks.size());
    }
}
