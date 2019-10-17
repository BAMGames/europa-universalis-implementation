package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.*;
import com.mkl.eu.client.service.service.chat.*;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.impl.ChatServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * Test of ChatService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatServiceTest extends AbstractGameServiceTest {
    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private IChatDao chatDao;

    @Mock
    private IPlayableCountryDao playableCountryDao;

    @Mock
    private ChatMapping chatMapping;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    /** Variable used to store something coming from a mock. */
    private List<ChatEntity> chatEntities;

    @Test
    public void testCreateRoomFail() {
        Pair<Request<CreateRoomRequest>, GameEntity> pair = testCheckGame(chatService::createRoom, "createRoom");
        Request<CreateRoomRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because createRoom.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.request", e.getParams()[0]);
        }

        request.setRequest(new CreateRoomRequest());

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(4L);

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because name is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.request.name", e.getParams()[0]);
        }

        request.getRequest().setName("Title");

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because country does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.idCountry", e.getParams()[0]);
        }

        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(4L);
        when(chatDao.getRoom(12L, "Title")).thenReturn(new RoomEntity());

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because room already exists");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.request.name", e.getParams()[0]);
        }
    }

    @Test
    public void testCreateRoomSuccess() throws Exception {
        Request<CreateRoomRequest> request = new Request<>();
        request.setRequest(new CreateRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setIdCountry(4L);
        request.getRequest().setName("Title");

        GameEntity game = createGameUsingMocks();

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);
        game.getCountries().add(sender);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        when(chatDao.create(anyObject())).thenAnswer(invocation -> {
            RoomEntity roomEntity = (RoomEntity) invocation.getArguments()[0];
            roomEntity.setId(13L);
            return diffEntity;
        });

        simulateDiff();

        DiffResponse response = chatService.createRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoom(12L, "Title");
        inOrder.verify(chatDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());

        Assert.assertEquals(13L, diffEntity.getIdObject().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(DiffTypeEnum.ADD, diffEntity.getType());
        Assert.assertEquals(DiffTypeObjectEnum.ROOM, diffEntity.getTypeObject());
        Assert.assertEquals(12L, diffEntity.getIdGame().longValue());
        Assert.assertEquals(game.getVersion(), diffEntity.getVersionGame().longValue());
        Assert.assertEquals(2, diffEntity.getAttributes().size());
        Assert.assertEquals(DiffAttributeTypeEnum.NAME, diffEntity.getAttributes().get(0).getType());
        Assert.assertEquals("Title", diffEntity.getAttributes().get(0).getValue());
        Assert.assertEquals(DiffAttributeTypeEnum.ID_COUNTRY, diffEntity.getAttributes().get(1).getType());
        Assert.assertEquals("4", diffEntity.getAttributes().get(1).getValue());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
    }

    @Test
    public void testSpeakInRoomFail() {
        Pair<Request<SpeakInRoomRequest>, GameEntity> pair = testCheckGame(chatService::speakInRoom, "speakInRoom");
        Request<SpeakInRoomRequest> request = pair.getLeft();

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because speakInRoom.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.request", e.getParams()[0]);
        }

        request.setRequest(new SpeakInRoomRequest());

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(4L);

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because message is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.request.message", e.getParams()[0]);
        }

        request.getRequest().setMessage("Message");
    }

    @Test
    public void testSpeakInGlobalRoomSuccess() throws Exception {
        Request<SpeakInRoomRequest> request = new Request<>();
        request.setRequest(new SpeakInRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setIdCountry(4L);
        request.getRequest().setMessage("Message");
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.setIdCountry(4L);

        GameEntity game = createGameUsingMocks();

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);
        game.getCountries().add(sender);

        RoomGlobalEntity room = new RoomGlobalEntity();
        room.setId(13L);

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.add(new ChatEntity());

        List<MessageGlobalEntity> messagesGlobal = new ArrayList<>();
        messagesGlobal.add(new MessageGlobalEntity());

        List<MessageDiff> messagesVos = new ArrayList<>();
        messagesVos.add(new MessageDiff());
        messagesVos.add(new MessageDiff());

        List<MessageDiff> messagesGlobalVos = new ArrayList<>();
        messagesGlobalVos.add(new MessageDiff());

        when(chatDao.getRoomGlobal(12L)).thenReturn(room);

        simulateDiff();

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.speakInRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoomGlobal(12L);
        inOrder.verify(chatDao).createMessage((MessageGlobalEntity) anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
    }

    @Test
    public void testSpeakInRoomSuccess() throws Exception {
        Request<SpeakInRoomRequest> request = new Request<>();
        request.setRequest(new SpeakInRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setIdCountry(4L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setMessage("Message");
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.setIdCountry(4L);

        GameEntity game = createGameUsingMocks();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(4L);
        game.getCountries().get(0).setName("france");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(1).setId(5L);
        game.getCountries().get(1).setName("angleterre");
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(2).setId(6L);
        game.getCountries().get(2).setName("espagne");

        RoomEntity room = new RoomEntity();
        room.setId(9L);
        room.getPresents().add(new PresentEntity());
        room.getPresents().get(0).setRoom(room);
        room.getPresents().get(0).setPresent(true);
        room.getPresents().get(0).setCountry(game.getCountries().get(0));
        room.getPresents().add(new PresentEntity());
        room.getPresents().get(1).setRoom(room);
        room.getPresents().get(1).setPresent(false);
        room.getPresents().get(1).setCountry(game.getCountries().get(1));
        room.getPresents().add(new PresentEntity());
        room.getPresents().get(2).setRoom(room);
        room.getPresents().get(2).setPresent(true);
        room.getPresents().get(2).setCountry(game.getCountries().get(2));

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.add(new ChatEntity());

        List<MessageGlobalEntity> messagesGlobal = new ArrayList<>();
        messagesGlobal.add(new MessageGlobalEntity());

        List<MessageDiff> messagesVos = new ArrayList<>();
        messagesVos.add(new MessageDiff());
        messagesVos.add(new MessageDiff());

        List<MessageDiff> messagesGlobalVos = new ArrayList<>();
        messagesGlobalVos.add(new MessageDiff());

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        doAnswer(invocation -> {
            chatEntities = (List<ChatEntity>) invocation.getArguments()[0];
            return null;
        }).when(chatDao).createMessage((List<ChatEntity>) anyObject());

        simulateDiff();

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.speakInRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        //noinspection unchecked
        inOrder.verify(chatDao).createMessage((List<ChatEntity>) anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());

        Assert.assertEquals(2, chatEntities.size());
        Assert.assertEquals(room, chatEntities.get(0).getRoom());
        Assert.assertEquals(game.getCountries().get(0), chatEntities.get(0).getReceiver());
        Assert.assertEquals("Message", chatEntities.get(0).getMessage().getMessage());
        Assert.assertNotNull(chatEntities.get(0).getDateRead());
        Assert.assertEquals(room, chatEntities.get(1).getRoom());
        Assert.assertEquals(game.getCountries().get(2), chatEntities.get(1).getReceiver());
        Assert.assertEquals("Message", chatEntities.get(1).getMessage().getMessage());
        Assert.assertNull(chatEntities.get(1).getDateRead());
    }

    @Test
    public void testToggleRoomFail() {
        Pair<Request<ToggleRoomRequest>, GameEntity> pair = testCheckGame(chatService::toggleRoom, "toggleRoom");
        Request<ToggleRoomRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because toggleRoom.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.request", e.getParams()[0]);
        }

        request.setRequest(new ToggleRoomRequest());

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(4L);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because idRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.request.idRoom", e.getParams()[0]);
        }

        request.getRequest().setIdRoom(9L);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because visible is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.request.visible", e.getParams()[0]);
        }

        request.getRequest().setVisible(true);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because idCountry does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.idCountry", e.getParams()[0]);
        }

        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(4L);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because idRoom does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.request.idRoom", e.getParams()[0]);
        }
    }

    @Test
    public void testToggleRoomSuccess() throws Exception {
        Request<ToggleRoomRequest> request = new Request<>();
        request.setRequest(new ToggleRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.setIdCountry(4L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setVisible(true);
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.setIdCountry(4L);

        GameEntity game = createGameUsingMocks();

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);
        game.getCountries().add(sender);

        RoomEntity room = new RoomEntity();
        room.setId(9L);
        PresentEntity present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(1L);
        room.getPresents().add(present);
        PresentEntity presentToChange = new PresentEntity();
        presentToChange.setCountry(new PlayableCountryEntity());
        presentToChange.getCountry().setId(4L);
        room.getPresents().add(presentToChange);
        present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(2L);
        room.getPresents().add(present);

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.add(new ChatEntity());

        List<MessageGlobalEntity> messagesGlobal = new ArrayList<>();
        messagesGlobal.add(new MessageGlobalEntity());

        List<MessageDiff> messagesVos = new ArrayList<>();
        messagesVos.add(new MessageDiff());
        messagesVos.add(new MessageDiff());

        List<MessageDiff> messagesGlobalVos = new ArrayList<>();
        messagesGlobalVos.add(new MessageDiff());

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        simulateDiff();

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);

        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.toggleRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
        Assert.assertTrue(presentToChange.isVisible());
    }

    @Test
    public void testInviteKickRoomFail() {
        Pair<Request<InviteKickRoomRequest>, GameEntity> pair = testCheckGame(chatService::inviteKickRoom, "inviteKickRoom");
        Request<InviteKickRoomRequest> request = pair.getLeft();
        GameEntity game = pair.getRight();

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because inviteKickRoom.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request", e.getParams()[0]);
        }

        request.setRequest(new InviteKickRoomRequest());

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because chat is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.chat", e.getParams()[0]);
        }

        request.setChat(new ChatInfo());

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because chat.idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(5L);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(4L);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because idRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.idRoom", e.getParams()[0]);
        }

        request.getRequest().setIdRoom(9L);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because invite is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.invite", e.getParams()[0]);
        }

        request.getRequest().setInvite(true);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because chat.idCountry does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.idCountry", e.getParams()[0]);
        }



        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(5L);
        game.getCountries().add(sender);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because inviteKickRoom.idCountry does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.idCountry", e.getParams()[0]);
        }

        PlayableCountryEntity receiver = new PlayableCountryEntity();
        receiver.setId(4L);
        game.getCountries().add(receiver);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because idRoom does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.idRoom", e.getParams()[0]);
        }
    }

    @Test
    public void testInviteKickRoomSuccess1() throws Exception {
        inviteKickRoomSuccess(true, true);
    }

    @Test
    public void testInviteKickRoomSuccess2() throws Exception {
        inviteKickRoomSuccess(true, false);
    }

    @Test
    public void testInviteKickRoomSuccess3() throws Exception {
        inviteKickRoomSuccess(false, false);
    }

    @Test
    public void testInviteKickRoomSuccess4() throws Exception {
        inviteKickRoomSuccess(false, true);
    }

    private void inviteKickRoomSuccess(boolean presentBefore, boolean presentAfter) throws Exception {
        Request<InviteKickRoomRequest> request = new Request<>();
        request.setRequest(new InviteKickRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(4L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setInvite(presentAfter);
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.setIdCountry(5L);

        GameEntity game = createGameUsingMocks();

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(5L);
        game.getCountries().add(sender);

        PlayableCountryEntity receiver = new PlayableCountryEntity();
        receiver.setId(4L);
        game.getCountries().add(receiver);

        RoomEntity room = new RoomEntity();
        room.setId(9L);
        PresentEntity present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(1L);
        room.getPresents().add(present);
        PresentEntity presentToChange = new PresentEntity();
        presentToChange.setCountry(new PlayableCountryEntity());
        presentToChange.getCountry().setId(4L);
        presentToChange.setPresent(presentBefore);
        room.getPresents().add(presentToChange);
        present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(2L);
        room.getPresents().add(present);

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.add(new ChatEntity());

        List<MessageGlobalEntity> messagesGlobal = new ArrayList<>();
        messagesGlobal.add(new MessageGlobalEntity());

        List<MessageDiff> messagesVos = new ArrayList<>();
        messagesVos.add(new MessageDiff());
        messagesVos.add(new MessageDiff());

        List<MessageDiff> messagesGlobalVos = new ArrayList<>();
        messagesGlobalVos.add(new MessageDiff());

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        simulateDiff();

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.inviteKickRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        if (presentBefore == presentAfter) {
            inOrder.verify(diffDao, never()).create(anyObject());
            inOrder.verify(diffMapping, never()).oeToVo((DiffEntity) anyObject());
        } else {
            inOrder.verify(diffDao).create(anyObject());
            inOrder.verify(diffMapping).oeToVo((DiffEntity) anyObject());
        }
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 5L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
        Assert.assertEquals(presentAfter, presentToChange.isPresent());
    }

    @Test
    public void testInviteKickRoomSuccess() throws Exception {
        Request<InviteKickRoomRequest> request = new Request<>();
        request.setRequest(new InviteKickRoomRequest());
        request.setAuthent(new AuthentInfo());
        request.getAuthent().setUsername("toto");
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);
        request.getRequest().setIdCountry(3L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setInvite(true);
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.setIdCountry(5L);

        GameEntity game = createGameUsingMocks();

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(5L);
        game.getCountries().add(sender);

        PlayableCountryEntity receiver = new PlayableCountryEntity();
        receiver.setId(3L);
        game.getCountries().add(receiver);

        RoomEntity room = new RoomEntity();
        room.setId(9L);
        PresentEntity present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(1L);
        room.getPresents().add(present);
        PresentEntity presentToChange = new PresentEntity();
        presentToChange.setCountry(new PlayableCountryEntity());
        presentToChange.getCountry().setId(4L);
        presentToChange.setPresent(false);
        room.getPresents().add(presentToChange);
        present = new PresentEntity();
        present.setCountry(new PlayableCountryEntity());
        present.getCountry().setId(2L);
        room.getPresents().add(present);

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.add(new ChatEntity());

        List<MessageGlobalEntity> messagesGlobal = new ArrayList<>();
        messagesGlobal.add(new MessageGlobalEntity());

        List<MessageDiff> messagesVos = new ArrayList<>();
        messagesVos.add(new MessageDiff());
        messagesVos.add(new MessageDiff());

        List<MessageDiff> messagesGlobalVos = new ArrayList<>();
        messagesGlobalVos.add(new MessageDiff());

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        simulateDiff();

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.inviteKickRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).load(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        inOrder.verify(chatDao).createPresent(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oeToVo((DiffEntity) anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 5L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(getDiffAfter(), response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
    }

    @Test
    public void testLoadRoom() throws FunctionalException {
        try {
            chatService.loadRoom(null);
            Assert.fail("Should break because request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("loadRoom", e.getParams()[0]);
        }

        SimpleRequest<LoadRoomRequest> request = new SimpleRequest<>();
        request.setRequest(new LoadRoomRequest());
        request.getRequest().setIdCountry(12L);
        request.getRequest().setIdGame(1L);
        request.getRequest().setIdRoom(8L);

        RoomEntity room = new RoomEntity();
        room.setId(1L);
        room.setName("Room 1");
        when(chatDao.getRoom(1L, 8L)).thenReturn(room);

        List<ChatEntity> messages = new ArrayList<>();
        messages.add(new ChatEntity());
        messages.get(0).setId(666L);
        when(chatDao.getMessages(12L)).thenReturn(messages);

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();

        Room roomVo = new Room();
        roomVo.setId(1L);
        roomVo.setName("RoomVO 1");
        when(chatMapping.oeToVo(room, objectsCreated, 12L)).thenReturn(roomVo);

        List<Message> messagesVo = new ArrayList<>();
        messagesVo.add(new Message());
        messagesVo.get(0).setId(667L);
        when(chatMapping.oesToVosChat(messages, objectsCreated)).thenReturn(messagesVo);

        Room roomReturn = chatService.loadRoom(request);

        Assert.assertEquals(roomVo, roomReturn);
        Assert.assertEquals("RoomVO 1", roomReturn.getName());
        Assert.assertEquals(messagesVo, roomReturn.getMessages());
        Assert.assertEquals(667L, roomReturn.getMessages().get(0).getId().longValue());
    }

    @Test
    public void testReadRoom() throws FunctionalException {
        GameEntity game = createGameUsingMocks();
        game.getCountries().add(new PlayableCountryEntity());
        game.getCountries().get(0).setId(5L);

        try {
            chatService.readRoom(null);
            Assert.fail("Should break because request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom", e.getParams()[0]);
        }

        Request<ReadRoomRequest> request = new Request<>();
        request.setGame(new GameInfo());
        request.getGame().setIdGame(12L);
        request.getGame().setVersionGame(1L);

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.request is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.request", e.getParams()[0]);
        }

        request.setRequest(new ReadRoomRequest());

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.request.idRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.request.idRoom", e.getParams()[0]);
        }

        request.getRequest().setIdRoom(8L);

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.chat is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.chat", e.getParams()[0]);
        }

        request.setChat(new ChatInfo());

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.chat.idCountry is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(3L);

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.chat.idCountry is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.idCountry", e.getParams()[0]);
        }

        request.setIdCountry(5L);
        request.getRequest().setMaxId(19L);

        try {
            chatService.readRoom(request);
            Assert.fail("Should break because request.request.idRoom is invalid");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("readRoom.request.idRoom", e.getParams()[0]);
        }

        when(chatDao.getRoom(12L, 8L)).thenReturn(new RoomEntity());

        chatService.readRoom(request);

        verify(chatDao).readMessagesInRoom(8L, 5L, 19L);
    }
}
