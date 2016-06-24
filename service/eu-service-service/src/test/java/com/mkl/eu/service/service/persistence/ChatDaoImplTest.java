package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.service.service.persistence.board.ICounterDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test for ChatDao.
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
@DataSet(value = {"chat.xml"}, columnSensing = true, tearDownOperation = DBOperation.DELETE_ALL)
public class ChatDaoImplTest {

    @Autowired
    private IChatDao chatDao;

    @Test
    public void testGetMessages() {
        List<ChatEntity> messages = chatDao.getMessages(null);

        Assert.assertEquals(0, messages.size());

        messages = chatDao.getMessages(1L);

        Assert.assertEquals(12, messages.size());

        messages = chatDao.getMessages(6L);

        Assert.assertEquals(9, messages.size());

        messages = chatDao.getMessages(2L);

        Assert.assertEquals(3, messages.size());

        messages = chatDao.getMessages(11L);

        Assert.assertEquals(3, messages.size());

        messages = chatDao.getMessages(12L);

        Assert.assertEquals(3, messages.size());
    }
}
