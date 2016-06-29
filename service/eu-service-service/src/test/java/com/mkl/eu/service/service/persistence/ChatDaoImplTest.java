package com.mkl.eu.service.service.persistence;

import com.excilys.ebi.spring.dbunit.config.DBOperation;
import com.excilys.ebi.spring.dbunit.test.DataSet;
import com.excilys.ebi.spring.dbunit.test.RollbackTransactionalDataSetTestExecutionListener;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.oe.chat.*;
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

    @Test
    public void testGetGlobalMessages() {
        List<MessageGlobalEntity> messages = chatDao.getGlobalMessages(null);

        Assert.assertEquals(0, messages.size());

        messages = chatDao.getGlobalMessages(1L);

        Assert.assertEquals(17, messages.size());

        messages = chatDao.getGlobalMessages(2L);

        Assert.assertEquals(0, messages.size());
    }

    @Test
    public void testGetRooms() {
        List<RoomEntity> rooms = chatDao.getRooms(null, null);

        Assert.assertEquals(0, rooms.size());

        rooms = chatDao.getRooms(1L, null);

        Assert.assertEquals(0, rooms.size());

        rooms = chatDao.getRooms(null, 1L);

        Assert.assertEquals(0, rooms.size());

        rooms = chatDao.getRooms(1L, 1L);

        Assert.assertEquals(2, rooms.size());
        Assert.assertEquals("room1-1", rooms.get(0).getName());
        Assert.assertEquals("room1-2", rooms.get(1).getName());

        rooms = chatDao.getRooms(1L, 2L);

        Assert.assertEquals(2, rooms.size());
        Assert.assertEquals("room1-1", rooms.get(0).getName());
        Assert.assertEquals("room1-2", rooms.get(1).getName());

        rooms = chatDao.getRooms(1L, 6L);

        Assert.assertEquals(2, rooms.size());
        Assert.assertEquals("room1-1", rooms.get(0).getName());
        Assert.assertEquals("room1-2", rooms.get(1).getName());

        rooms = chatDao.getRooms(2L, 1L);

        Assert.assertEquals(1, rooms.size());
        Assert.assertEquals("room2-1", rooms.get(0).getName());

        rooms = chatDao.getRooms(2L, 6L);

        Assert.assertEquals(0, rooms.size());
    }

    @Test
    public void testUnreadMsg() {
        Assert.assertEquals(0, chatDao.getUnreadMessagesNumber(null));
        Assert.assertEquals(0, chatDao.getUnreadMessagesNumber(1L));
        Assert.assertEquals(1, chatDao.getUnreadMessagesNumber(2L));
        Assert.assertEquals(9, chatDao.getUnreadMessagesNumber(6L));
        Assert.assertEquals(0, chatDao.getUnreadMessagesNumber(11L));
        Assert.assertEquals(1, chatDao.getUnreadMessagesNumber(12L));
    }

    @Test
    public void testRoomGlobal() {
        RoomGlobalEntity room = chatDao.getRoomGlobal(null);

        Assert.assertEquals(null, room);

        room = chatDao.getRoomGlobal(1L);

        Assert.assertEquals(1L, room.getId().longValue());

        room = chatDao.getRoomGlobal(2L);

        Assert.assertEquals(null, room);

        room = chatDao.getRoomGlobal(3L);

        Assert.assertEquals(null, room);
    }

    @Test
    public void testRoom() {
        RoomEntity room = chatDao.getRoom(null, (Long)null);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(1L, (Long)null);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(null, 1L);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(1L, 1L);

        Assert.assertEquals(1, room.getId().longValue());

        room = chatDao.getRoom(1L, 2L);

        Assert.assertEquals(2, room.getId().longValue());

        room = chatDao.getRoom(1L, 21L);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(2L, 21L);

        Assert.assertEquals(21, room.getId().longValue());

        room = chatDao.getRoom(2L, 1L);

        Assert.assertEquals(null, room);
    }

    @Test
    public void testRoomByName() {
        RoomEntity room = chatDao.getRoom(null, (String)null);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(1L, (String)null);

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(null, "room1-1");

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(1L, "room1-1");

        Assert.assertEquals(1, room.getId().longValue());

        room = chatDao.getRoom(1L, "room1-2");

        Assert.assertEquals(2, room.getId().longValue());

        room = chatDao.getRoom(1L, "room2-1");

        Assert.assertEquals(null, room);

        room = chatDao.getRoom(2L, "room2-1");

        Assert.assertEquals(21, room.getId().longValue());

        room = chatDao.getRoom(2L, "room1-1");

        Assert.assertEquals(null, room);
    }

    @Test
    public void testCreateMessageFail() {
        MessageGlobalEntity msg = new MessageGlobalEntity();

        msg.setMessage("toto");

        try {
            chatDao.createMessage(msg);
            Assert.fail("Should break because no link to room.");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.ERROR_CREATION, e.getCode());
        }
    }

    @Test
    public void testCreateMessageSuccess() {
        MessageGlobalEntity msg = new MessageGlobalEntity();

        msg.setMessage("toto");
        RoomGlobalEntity room = chatDao.getRoomGlobal(666L);
        msg.setRoom(room);
        msg.setSender(room.getGame().getCountries().get(0));

        chatDao.createMessage(msg);
    }

    @Test
    public void testCreateMessagesFail() {
        List<ChatEntity> msgs = new ArrayList<>();

        ChatEntity msg = new ChatEntity();
        msgs.add(msg);

        try {
            chatDao.createMessage(msgs);
            Assert.fail("Should break because no link to room.");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.ERROR_CREATION, e.getCode());
        }
    }

    @Test
    public void testCreateMessagesSuccess() {
        List<ChatEntity> msgs = new ArrayList<>();

        ChatEntity msg = new ChatEntity();
        RoomEntity room = chatDao.getRoom(666L, 666L);
        msg.setRoom(room);
        msg.setReceiver(room.getOwner());
        msg.setMessage(room.getMessages().get(0).getMessage());
        msgs.add(msg);

        chatDao.createMessage(msgs);
    }

    @Test
    public void testGetMessagesSince() {
        List<ChatEntity> msg = chatDao.getMessagesSince(null, null, null);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(1L, null, null);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(null, 1L, null);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(1L, 1L, null);

        Assert.assertEquals(12, msg.size());

        msg = chatDao.getMessagesSince(1L, 1L, 0L);

        Assert.assertEquals(12, msg.size());

        msg = chatDao.getMessagesSince(1L, 1L, 1L);

        Assert.assertEquals(11, msg.size());

        msg = chatDao.getMessagesSince(1L, 1L, 10L);

        Assert.assertEquals(3, msg.size());

        msg = chatDao.getMessagesSince(1L, 1L, 100000L);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(1L, 5L, null);

        Assert.assertEquals(1, msg.size());

        msg = chatDao.getMessagesSince(1L, 6L, null);

        Assert.assertEquals(9, msg.size());

        msg = chatDao.getMessagesSince(1L, 2L, null);

        Assert.assertEquals(3, msg.size());

        msg = chatDao.getMessagesSince(2L, 1L, 0L);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(2L, 11L, 0L);

        Assert.assertEquals(3, msg.size());

        msg = chatDao.getMessagesSince(2L, 11L, 20L);

        Assert.assertEquals(3, msg.size());

        msg = chatDao.getMessagesSince(2L, 11L, 2000000L);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesSince(124521L, 1248894L, null);

        Assert.assertEquals(0, msg.size());
    }

    @Test
    public void testGetMessagesGlobalSince() {
        List<MessageGlobalEntity> msg = chatDao.getMessagesGlobalSince(null, null);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesGlobalSince(null, 1L);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesGlobalSince(1L, null);

        Assert.assertEquals(17, msg.size());

        msg = chatDao.getMessagesGlobalSince(1L, 10L);

        Assert.assertEquals(7, msg.size());

        msg = chatDao.getMessagesGlobalSince(1L, 100L);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesGlobalSince(2L, null);

        Assert.assertEquals(0, msg.size());

        msg = chatDao.getMessagesGlobalSince(99999L, null);

        Assert.assertEquals(0, msg.size());
    }

    @Test
    public void testCreatePresentFail() {
        PresentEntity pres = new PresentEntity();

        try {
            chatDao.createPresent(pres);
            Assert.fail("Should break because no link to room.");
        } catch (TechnicalException e) {
            Assert.assertEquals(IConstantsCommonException.ERROR_CREATION, e.getCode());
        }
    }

    @Test
    public void testCreatePresentSuccess() {
        PresentEntity pres = new PresentEntity();

        RoomEntity room = chatDao.getRoom(666L, 666L);
        pres.setRoom(room);
        pres.setCountry(room.getOwner());

        chatDao.createPresent(pres);
    }

    @Test
    public void testReadMessages() {
        Assert.assertEquals(0, chatDao.readMessagesInRoom(null, null, null));

        Assert.assertEquals(0, chatDao.readMessagesInRoom(1L, null, null));

        Assert.assertEquals(0, chatDao.readMessagesInRoom(1L, 6L, null));

        Assert.assertEquals(0, chatDao.readMessagesInRoom(1L, 12L, 7L));

        Assert.assertEquals(0, chatDao.readMessagesInRoom(1215123L, 6L, 7L));

        Assert.assertEquals(3, chatDao.readMessagesInRoom(1L, 6L, 7L));

        // I'd like to test that the dateRead of the right ChatEntity has been
        // created, but it seems impossible
        // http://stackoverflow.com/questions/26105773/difficulty-verifying-hibernate-update-queries-inside-a-transaction-with-test-cod
    }
}
