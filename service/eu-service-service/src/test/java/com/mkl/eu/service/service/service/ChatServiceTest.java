package com.mkl.eu.service.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.AuthentInfo;
import com.mkl.eu.client.common.vo.ChatInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.chat.CreateRoomRequest;
import com.mkl.eu.client.service.service.chat.InviteKickRoomRequest;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.service.chat.ToggleRoomRequest;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.impl.ChatServiceImpl;
import com.mkl.eu.service.service.socket.SocketHandler;
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
import static org.mockito.Mockito.*;

/**
 * Test of ChatService.
 *
 * @author MKL.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChatServiceTest {
    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private IGameDao gameDao;

    @Mock
    private IDiffDao diffDao;

    @Mock
    private IChatDao chatDao;

    @Mock
    private IPlayableCountryDao playableCountryDao;

    @Mock
    private DiffMapping diffMapping;

    @Mock
    private ChatMapping chatMapping;

    @Mock
    private SocketHandler socketHandler;

    /** Variable used to store something coming from a mock. */
    private DiffEntity diffEntity;

    /** Variable used to store something coming from a mock. */
    private List<ChatEntity> chatEntities;

    @Test
    public void testCreateRoomFail() {
        try {
            chatService.createRoom(null);
            Assert.fail("Should break because createRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom", e.getParams()[0]);
        }

        Request<CreateRoomRequest> request = new Request<>();

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because createRoom.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            chatService.createRoom(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("createRoom.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
            Assert.assertEquals("createRoom.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(4L);

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
            Assert.assertEquals("createRoom.request.idCountry", e.getParams()[0]);
        }

        when(playableCountryDao.load(4L)).thenReturn(new PlayableCountryEntity());
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
        request.getRequest().setIdCountry(4L);
        request.getRequest().setName("Title");

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(4L)).thenReturn(sender);

        when(diffDao.create(anyObject())).thenAnswer(invocation -> {
            diffEntity = (DiffEntity) invocation.getArguments()[0];
            return diffEntity;
        });

        when(chatDao.create(anyObject())).thenAnswer(invocation -> {
            RoomEntity roomEntity = (RoomEntity) invocation.getArguments()[0];
            roomEntity.setId(13L);
            return diffEntity;
        });

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        DiffResponse response = chatService.createRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(4L);
        inOrder.verify(chatDao).getRoom(12L, "Title");
        inOrder.verify(chatDao).create(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
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
        Assert.assertEquals(diffAfter, response.getDiffs());
    }

    @Test
    public void testSpeakInRoomFail() {
        try {
            chatService.speakInRoom(null);
            Assert.fail("Should break because speakInRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom", e.getParams()[0]);
        }

        Request<SpeakInRoomRequest> request = new Request<>();

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because speakInRoom.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            chatService.speakInRoom(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("speakInRoom.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
            Assert.assertEquals("speakInRoom.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(4L);

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
        request.getRequest().setIdCountry(4L);
        request.getRequest().setMessage("Message");
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.getChat().setIdCountry(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);

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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(4L)).thenReturn(sender);

        when(chatDao.getRoomGlobal(12L)).thenReturn(room);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.speakInRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(4L);
        inOrder.verify(chatDao).getRoomGlobal(12L);
        inOrder.verify(chatDao).createMessage((MessageGlobalEntity) anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
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
        request.getRequest().setIdCountry(4L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setMessage("Message");
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.getChat().setIdCountry(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);
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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(4L)).thenReturn(game.getCountries().get(0));

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        doAnswer(invocation -> {
            chatEntities = (List<ChatEntity>) invocation.getArguments()[0];
            return null;
        }).when(chatDao).createMessage((List<ChatEntity>) anyObject());

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.speakInRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(4L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        //noinspection unchecked
        inOrder.verify(chatDao).createMessage((List<ChatEntity>) anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
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
        try {
            chatService.toggleRoom(null);
            Assert.fail("Should break because toggleRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom", e.getParams()[0]);
        }

        Request<ToggleRoomRequest> request = new Request<>();

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because toggleRoom.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            chatService.toggleRoom(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("toggleRoom.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
            Assert.assertEquals("toggleRoom.request.idCountry", e.getParams()[0]);
        }

        request.getRequest().setIdCountry(4L);

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
            Assert.assertEquals("toggleRoom.request.idCountry", e.getParams()[0]);
        }

        when(playableCountryDao.load(4L)).thenReturn(new PlayableCountryEntity());

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
        request.getRequest().setIdCountry(4L);
        request.getRequest().setIdRoom(9L);
        request.getRequest().setVisible(true);
        request.setChat(new ChatInfo());
        request.getChat().setMaxIdMessage(21L);
        request.getChat().setMaxIdGlobalMessage(22L);
        request.getChat().setIdCountry(4L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(4L);

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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(4L)).thenReturn(sender);

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.toggleRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(4L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 4L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
        Assert.assertTrue(presentToChange.isVisible());
    }

    @Test
    public void testInviteKickRoomFail() {
        try {
            chatService.inviteKickRoom(null);
            Assert.fail("Should break because inviteKickRoom is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom", e.getParams()[0]);
        }

        Request<InviteKickRoomRequest> request = new Request<>();

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because inviteKickRoom.game is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.game", e.getParams()[0]);
        }

        request.setGame(new GameInfo());

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because idGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.game.idGame", e.getParams()[0]);
        }

        request.getGame().setIdGame(12L);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because versionGame is null");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.NULL_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.game.versionGame", e.getParams()[0]);
        }

        request.getGame().setVersionGame(1L);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because game does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.game.idGame", e.getParams()[0]);
        }

        GameEntity game = new GameEntity();
        game.setId(12L);

        when(gameDao.lock(12L)).thenReturn(game);

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because versions does not match");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.game.versionGame", e.getParams()[0]);
        }

        game.setVersion(5L);

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
            Assert.assertEquals("inviteKickRoom.chat.idCountry", e.getParams()[0]);
        }

        request.getChat().setIdCountry(5L);

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
            Assert.assertEquals("inviteKickRoom.chat.idCountry", e.getParams()[0]);
        }

        when(playableCountryDao.load(5L)).thenReturn(new PlayableCountryEntity());

        try {
            chatService.inviteKickRoom(request);
            Assert.fail("Should break because inviteKickRoom.idCountry does not exist");
        } catch (FunctionalException e) {
            Assert.assertEquals(IConstantsCommonException.INVALID_PARAMETER, e.getCode());
            Assert.assertEquals("inviteKickRoom.request.idCountry", e.getParams()[0]);
        }

        when(playableCountryDao.load(4L)).thenReturn(new PlayableCountryEntity());

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
        request.getChat().setIdCountry(5L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(5L);

        PlayableCountryEntity receiver = new PlayableCountryEntity();
        receiver.setId(4L);

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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(5L)).thenReturn(sender);
        when(playableCountryDao.load(4L)).thenReturn(receiver);

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.inviteKickRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(5L);
        inOrder.verify(playableCountryDao).load(4L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        if (presentBefore == presentAfter) {
            inOrder.verify(diffDao, never()).create(anyObject());
            inOrder.verify(diffMapping, never()).oeToVo((DiffEntity) anyObject());
            inOrder.verify(socketHandler, never()).push(anyObject(), anyObject(), anyObject());
        } else {
            inOrder.verify(diffDao).create(anyObject());
            inOrder.verify(diffMapping).oeToVo((DiffEntity) anyObject());
            inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        }
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 5L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
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
        request.getChat().setIdCountry(5L);

        GameEntity game = new GameEntity();
        game.setId(12L);
        game.setVersion(5L);

        PlayableCountryEntity sender = new PlayableCountryEntity();
        sender.setId(5L);

        PlayableCountryEntity receiver = new PlayableCountryEntity();
        receiver.setId(3L);

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

        when(gameDao.lock(12L)).thenReturn(game);

        List<DiffEntity> diffBefore = new ArrayList<>();
        diffBefore.add(new DiffEntity());
        diffBefore.add(new DiffEntity());

        when(diffDao.getDiffsSince(12L, 1L)).thenReturn(diffBefore);

        when(playableCountryDao.load(5L)).thenReturn(sender);
        when(playableCountryDao.load(3L)).thenReturn(receiver);

        when(chatDao.getRoom(12L, 9L)).thenReturn(room);

        List<Diff> diffAfter = new ArrayList<>();
        diffAfter.add(new Diff());
        diffAfter.add(new Diff());

        when(diffMapping.oesToVos(anyObject())).thenReturn(diffAfter);

        when(chatDao.getMessagesSince(12L, 4L, 21L)).thenReturn(messages);
        when(chatDao.getMessagesGlobalSince(12L, 21L)).thenReturn(messagesGlobal);


        when(chatMapping.oesToVosChatSince(anyObject(), anyObject())).thenReturn(messagesVos);
        when(chatMapping.oesToVosMessageSince(anyObject(), anyObject())).thenReturn(messagesGlobalVos);

        DiffResponse response = chatService.inviteKickRoom(request);

        InOrder inOrder = inOrder(gameDao, diffDao, socketHandler, playableCountryDao, chatDao, chatMapping, diffMapping);

        inOrder.verify(gameDao).lock(12L);
        inOrder.verify(diffDao).getDiffsSince(12L, 1L);
        inOrder.verify(playableCountryDao).load(5L);
        inOrder.verify(playableCountryDao).load(3L);
        inOrder.verify(chatDao).getRoom(12L, 9L);
        inOrder.verify(chatDao).createPresent(anyObject());
        inOrder.verify(diffDao).create(anyObject());
        inOrder.verify(diffMapping).oeToVo((DiffEntity) anyObject());
        inOrder.verify(socketHandler).push(anyObject(), anyObject(), anyObject());
        inOrder.verify(diffMapping).oesToVos(anyObject());
        inOrder.verify(chatDao).getMessagesSince(12L, 5L, 21L);
        inOrder.verify(chatDao).getMessagesGlobalSince(12L, 22L);
        inOrder.verify(chatMapping).oesToVosChatSince(anyObject(), anyObject());
        inOrder.verify(chatMapping).oesToVosMessageSince(anyObject(), anyObject());

        Assert.assertEquals(game.getVersion(), response.getVersionGame().longValue());
        Assert.assertEquals(diffAfter, response.getDiffs());
        Assert.assertEquals(3, response.getMessages().size());
    }
}
