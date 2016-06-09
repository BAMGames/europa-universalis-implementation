package com.mkl.eu.service.service.mapping;

import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.unitils.reflectionassert.ReflectionAssert;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Test of ChatMapping.
 *
 * @author MKL.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/com/mkl/eu/service/service/mapping/test-eu-mapping-applicationContext..xml"})
public class ChatMappingTest {
    private static final PlayableCountry FRA_VO;
    private static final PlayableCountry PRU_VO;
    private static final PlayableCountryEntity FRA_OE;
    private static final PlayableCountryEntity PRU_OE;

    @Autowired
    private ChatMapping chatMapping;

    static {
        FRA_VO = new PlayableCountry();
        FRA_VO.setId(1L);
        FRA_VO.setName("FRA");
        FRA_VO.setUsername("MKL");

        PRU_VO = new PlayableCountry();
        PRU_VO.setId(2L);
        PRU_VO.setName("PRU");
        PRU_VO.setUsername("Fogia");

        FRA_OE = new PlayableCountryEntity();
        FRA_OE.setId(1L);
        FRA_OE.setName("FRA");
        FRA_OE.setUsername("MKL");

        PRU_OE = new PlayableCountryEntity();
        PRU_OE.setId(2L);
        PRU_OE.setName("PRU");
        PRU_OE.setUsername("Fogia");
    }

    @Test
    public void testVoidChatMapping() {
        Chat vo = chatMapping.getChat(null, null, null, null);

        ReflectionAssert.assertReflectionEquals(new Chat(), vo);

        vo = chatMapping.getChat(new ArrayList<>(), null, null, null);

        ReflectionAssert.assertReflectionEquals(new Chat(), vo);

        vo = chatMapping.getChat(new ArrayList<>(), new ArrayList<>(), null, null);

        ReflectionAssert.assertReflectionEquals(new Chat(), vo);

        vo = chatMapping.getChat(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);

        ReflectionAssert.assertReflectionEquals(new Chat(), vo);

        vo = chatMapping.getChat(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 12L);

        ReflectionAssert.assertReflectionEquals(new Chat(), vo);
    }

    @Test
    public void testFullChatMapping() {
        List<MessageGlobalEntity> globalMessages = getGlobalMessagesEntities();

        List<RoomEntity> rooms = getRoomEntities();

        List<ChatEntity> messages = getMessageEntities(rooms.get(0));

        Long idCountry = FRA_VO.getId();

        Chat vo = chatMapping.getChat(globalMessages, rooms, messages, idCountry);

        ReflectionAssert.assertReflectionEquals(createChatVo(true), vo);

        idCountry = PRU_VO.getId();

        vo = chatMapping.getChat(globalMessages, rooms, messages, idCountry);

        ReflectionAssert.assertReflectionEquals(createChatVo(false), vo);
    }

    private List<MessageGlobalEntity> getGlobalMessagesEntities() {
        List<MessageGlobalEntity> globalMessages = new ArrayList<>();
        MessageGlobalEntity message = new MessageGlobalEntity();
        message.setId(1L);
        message.setMessage("Global - 1");
        message.setDateSent(ZonedDateTime.parse("2010-01-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(FRA_OE);
        globalMessages.add(message);
        message = new MessageGlobalEntity();
        message.setId(2L);
        message.setMessage("Global - 2");
        message.setDateSent(ZonedDateTime.parse("2010-01-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(PRU_OE);
        globalMessages.add(message);

        return globalMessages;
    }

    private List<RoomEntity> getRoomEntities() {
        List<RoomEntity> rooms = new ArrayList<>();

        List<PresentEntity> presents = new ArrayList<>();
        PresentEntity present = new PresentEntity();
        present.setCountry(FRA_OE);
        present.setPresent(true);
        present.setVisible(true);
        presents.add(present);

        present = new PresentEntity();
        present.setCountry(PRU_OE);
        present.setPresent(false);
        present.setVisible(false);
        presents.add(present);

        RoomEntity room = new RoomEntity();
        room.setId(1L);
        room.setName("room 1");
        room.setOwner(FRA_OE);
        room.setPresents(presents);
        rooms.add(room);

        room = new RoomEntity();
        room.setId(2L);
        room.setName("room 2");
        room.setOwner(PRU_OE);
        room.setPresents(presents);
        rooms.add(room);

        return rooms;
    }

    private List<ChatEntity> getMessageEntities(RoomEntity room) {
        List<ChatEntity> chats = new ArrayList<>();

        ChatEntity chat = new ChatEntity();
        chat.setId(1L);
        chat.setDateRead(ZonedDateTime.parse("2010-02-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setRoom(room);
        chat.setReceiver(FRA_OE);
        MessageEntity message = new MessageEntity();
        message.setId(11L);
        message.setSender(PRU_OE);
        message.setMessage("Room 1 - Message 1");
        message.setDateSent(ZonedDateTime.parse("2010-02-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setMessage(message);
        chats.add(chat);

        RoomEntity room3 = new RoomEntity();
        room3.setId(3L);
        room3.setName("room 3");
        room3.setOwner(FRA_OE);

        chat = new ChatEntity();
        chat.setId(2L);
        chat.setDateRead(ZonedDateTime.parse("2010-03-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setRoom(room3);
        chat.setReceiver(FRA_OE);
        message = new MessageEntity();
        message.setId(12L);
        message.setSender(PRU_OE);
        message.setMessage("Room 3 - Message 1");
        message.setDateSent(ZonedDateTime.parse("2010-03-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setMessage(message);
        chats.add(chat);

        chat = new ChatEntity();
        chat.setId(3L);
        chat.setDateRead(ZonedDateTime.parse("2010-04-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setRoom(room3);
        chat.setReceiver(PRU_OE);
        message = new MessageEntity();
        message.setId(13L);
        message.setSender(FRA_OE);
        message.setMessage("Room 3 - Message 2");
        message.setDateSent(ZonedDateTime.parse("2010-04-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        chat.setMessage(message);
        chats.add(chat);

        return chats;
    }

    private Chat createChatVo(boolean visible) {
        Chat chat = new Chat();

        chat.setGlobalMessages(getGlobalMessagesVos());

        chat.setRooms(getRoomsVos(visible));

        return chat;
    }

    private List<Message> getGlobalMessagesVos() {
        List<Message> globalMessages = new ArrayList<>();
        Message message = new Message();
        message.setId(1L);
        message.setMessage("Global - 1");
        message.setDateSent(ZonedDateTime.parse("2010-01-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(FRA_VO);
        globalMessages.add(message);
        message = new Message();
        message.setId(2L);
        message.setMessage("Global - 2");
        message.setDateSent(ZonedDateTime.parse("2010-01-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(PRU_VO);
        globalMessages.add(message);

        return globalMessages;
    }

    private List<Room> getRoomsVos(boolean visible) {
        List<Room> rooms = new ArrayList<>();
        List<PlayableCountry> countries = new ArrayList<>();
        countries.add(FRA_VO);

        Room room = new Room();
        room.setId(1L);
        room.setName("room 1");
        room.setOwner(FRA_VO);
        room.setCountries(countries);
        room.setVisible(visible);
        room.setPresent(visible);
        rooms.add(room);
        Message message = new Message();
        message.setId(11L);
        message.setDateRead(ZonedDateTime.parse("2010-02-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(PRU_VO);
        message.setMessage("Room 1 - Message 1");
        message.setDateSent(ZonedDateTime.parse("2010-02-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        room.getMessages().add(message);

        room = new Room();
        room.setId(2L);
        room.setName("room 2");
        room.setOwner(PRU_VO);
        room.setCountries(countries);
        room.setVisible(visible);
        room.setPresent(visible);
        rooms.add(room);

        room = new Room();
        room.setId(3L);
        room.setName("room 3");
        room.setOwner(FRA_VO);
        room.setVisible(false);
        room.setPresent(false);
        rooms.add(room);
        message = new Message();
        message.setId(12L);
        message.setDateRead(ZonedDateTime.parse("2010-03-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(PRU_VO);
        message.setMessage("Room 3 - Message 1");
        message.setDateSent(ZonedDateTime.parse("2010-03-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        room.getMessages().add(message);
        message = new Message();
        message.setId(13L);
        message.setDateRead(ZonedDateTime.parse("2010-04-02T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        message.setSender(FRA_VO);
        message.setMessage("Room 3 - Message 2");
        message.setDateSent(ZonedDateTime.parse("2010-04-01T10:15:30+01:00[Europe/Paris]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        room.getMessages().add(message);

        return rooms;
    }
}
