package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.*;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.util.DiffUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for chat purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class ChatServiceImpl extends AbstractService implements IChatService {
    /** Error message when an object is not found (in database mostly). */
    public static final String MSG_ROOM_ALREADY_EXIST = "{1}: {0} a room with name \"{2}\" already exists.";

    /** {@inheritDoc} */
    @Override
    public DiffResponse createRoom(Request<CreateRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in request AND chatInfo
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM).setParams(METHOD_CREATE_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_CREATE_ROOM, PARAMETER_CREATE_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST).setParams(METHOD_CREATE_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_CREATE_ROOM));
        failIfEmpty(new AbstractService.CheckForThrow<String>().setTest(request.getRequest().getName()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_NAME).setParams(METHOD_CREATE_ROOM));

        String name = request.getRequest().getName();
        PlayableCountryEntity owner = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(owner).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_CREATE_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_CREATE_ROOM, request.getIdCountry()));

        RoomEntity room = chatDao.getRoom(request.getGame().getIdGame(), name);

        failIfNotNull(new AbstractService.CheckForThrow<>().setTest(room).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_ROOM_ALREADY_EXIST).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_NAME).setParams(METHOD_CREATE_ROOM, name));

        GameEntity game = gameDiffs.getGame();
        room = new RoomEntity();
        room.setOwner(owner);
        room.setName(name);
        room.setGame(game);
        List<PresentEntity> presents = new ArrayList<>();
        PresentEntity present = new PresentEntity();
        present.setVisible(true);
        present.setPresent(true);
        present.setCountry(owner);
        present.setRoom(room);
        presents.add(present);
        room.setPresents(presents);

        chatDao.create(room);

        DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.ADD, DiffTypeObjectEnum.ROOM, room.getId(),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.NAME, name),
                DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, owner.getId()));
        createDiff(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse speakInRoom(Request<SpeakInRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in request AND chatInfo
        // TODO check if speaker is present in room
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM).setParams(METHOD_SPEAK_IN_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_SPEAK_IN_ROOM, PARAMETER_SPEAK_IN_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST).setParams(METHOD_SPEAK_IN_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_SPEAK_IN_ROOM));
        failIfEmpty(new AbstractService.CheckForThrow<String>().setTest(request.getRequest().getMessage()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST, PARAMETER_MESSAGE).setParams(METHOD_SPEAK_IN_ROOM));

        PlayableCountryEntity sender = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(sender).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_SPEAK_IN_ROOM, request.getIdCountry()));

        List<Long> idCountries = new ArrayList<>();
        MessageDiff msg = new MessageDiff();
        msg.setIdRoom(request.getRequest().getIdRoom());
        msg.setMessage(request.getRequest().getMessage());
        msg.setDateSent(ZonedDateTime.now());
        msg.setIdSender(sender.getId());

        if (request.getRequest().getIdRoom() == null) {
            RoomGlobalEntity room = chatDao.getRoomGlobal(request.getGame().getIdGame());
            MessageGlobalEntity message = new MessageGlobalEntity();
            message.setDateSent(ZonedDateTime.now());
            message.setMessage(request.getRequest().getMessage());
            message.setRoom(room);
            message.setSender(sender);
            room.getMessages().add(message);
            chatDao.createMessage(message);
            msg.setId(message.getId());
        } else {
            RoomEntity room = chatDao.getRoom(request.getGame().getIdGame(), request.getRequest().getIdRoom());
            MessageEntity message = new MessageEntity();
            message.setDateSent(ZonedDateTime.now());
            message.setMessage(request.getRequest().getMessage());
            message.setSender(sender);

            List<ChatEntity> messages = new ArrayList<>();

            for (PresentEntity present : room.getPresents()) {
                if (!present.isPresent()) {
                    continue;
                }
                idCountries.add(present.getCountry().getId());
                ChatEntity chat = new ChatEntity();
                chat.setMessage(message);
                chat.setReceiver(present.getCountry());
                chat.setRoom(room);
                if (sender == present.getCountry()) {
                    chat.setDateRead(message.getDateSent());
                }
                room.getMessages().add(chat);
                messages.add(chat);
            }

            chatDao.createMessage(messages);
            msg.setId(message.getId());
        }

        push(msg, request.getGame().getIdGame(), idCountries);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(gameDiffs.getDiffs()));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse toggleRoom(Request<ToggleRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in request AND chatInfo
        // TODO check if the user is present in room
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_TOGGLE_ROOM).setParams(METHOD_TOGGLE_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_TOGGLE_ROOM, PARAMETER_TOGGLE_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_REQUEST).setParams(METHOD_TOGGLE_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_TOGGLE_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdRoom()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_TOGGLE_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().isVisible()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_REQUEST, PARAMETER_VISIBLE).setParams(METHOD_TOGGLE_ROOM));

        PlayableCountryEntity sender = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(sender).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_TOGGLE_ROOM, request.getIdCountry()));

        RoomEntity room = chatDao.getRoom(request.getGame().getIdGame(), request.getRequest().getIdRoom());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(room).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_TOGGLE_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_TOGGLE_ROOM, request.getRequest().getIdRoom()));

        PresentEntity present = CommonUtil.findFirst(room.getPresents(), presentEntity -> request.getIdCountry().equals(presentEntity.getCountry().getId()));

        if (present != null) {
            present.setVisible(request.getRequest().isVisible());
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(gameDiffs.getDiffs()));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse inviteKickRoom(Request<InviteKickRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in chatInfo
        // TODO check if the user is owner of the room

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM).setParams(METHOD_INVITE_KICK_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_INVITE_KICK_ROOM, PARAMETER_INVITE_KICK_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST).setParams(METHOD_INVITE_KICK_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getChat()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_CHAT).setParams(METHOD_INVITE_KICK_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_INVITE_KICK_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_INVITE_KICK_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdRoom()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_INVITE_KICK_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().isInvite()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST, PARAMETER_INVITE).setParams(METHOD_INVITE_KICK_ROOM));

        PlayableCountryEntity owner = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(owner).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_INVITE_KICK_ROOM, request.getIdCountry()));

        PlayableCountryEntity target = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                                                            c -> c.getId().equals(request.getRequest().getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(target).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_INVITE_KICK_ROOM, request.getRequest().getIdCountry()));

        RoomEntity room = chatDao.getRoom(request.getGame().getIdGame(), request.getRequest().getIdRoom());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(room).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_INVITE_KICK_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_INVITE_KICK_ROOM, request.getRequest().getIdRoom()));

        PresentEntity present = CommonUtil.findFirst(room.getPresents(), presentEntity -> request.getRequest().getIdCountry().equals(presentEntity.getCountry().getId()));

        boolean change = false;
        if (present != null) {
            change = !present.isPresent().equals(request.getRequest().isInvite());
            if (request.getRequest().isInvite()) {
                present.setPresent(true);
            } else {
                present.setPresent(false);
            }
        } else {
            if (request.getRequest().isInvite()) {
                change = true;
                present = new PresentEntity();
                present.setPresent(true);
                present.setVisible(true);
                present.setRoom(room);
                present.setCountry(target);
                chatDao.createPresent(present);
            }
        }

        GameEntity game = gameDiffs.getGame();
        List<DiffEntity> diffs = gameDiffs.getDiffs();

        if (change) {
            DiffEntity diff = DiffUtil.createDiff(game, DiffTypeEnum.LINK, DiffTypeObjectEnum.ROOM, room.getId(),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.ID_COUNTRY, target.getId()),
                    DiffUtil.createDiffAttributes(DiffAttributeTypeEnum.INVITE, request.getRequest().isInvite()));
            createDiff(diff);

            diffs.add(diff);
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public Room loadRoom(SimpleRequest<LoadRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in chatInfo

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_LOAD_ROOM).setParams(METHOD_LOAD_ROOM));

        RoomEntity roomEntity = chatDao.getRoom(request.getRequest().getIdGame(), request.getRequest().getIdRoom());
        List<ChatEntity> messages = chatDao.getMessages(request.getRequest().getIdCountry());

        Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();
        Room room = chatMapping.oeToVo(roomEntity, objectsCreated, request.getRequest().getIdCountry());
        room.setMessages(chatMapping.oesToVosChat(messages, objectsCreated));

        return room;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse readRoom(Request<ReadRoomRequest> request) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in chatInfo

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_READ_ROOM).setParams(METHOD_READ_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(request.getGame(), METHOD_READ_ROOM, PARAMETER_READ_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_READ_ROOM, PARAMETER_REQUEST).setParams(METHOD_READ_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getRequest().getIdRoom()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_READ_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_READ_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getChat()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_READ_ROOM, PARAMETER_CHAT).setParams(METHOD_READ_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(request.getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_READ_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_READ_ROOM));

        PlayableCountryEntity owner = CommonUtil.findFirst(gameDiffs.getGame().getCountries().stream(),
                c -> c.getId().equals(request.getIdCountry()));

        failIfNull(new AbstractService.CheckForThrow<>().setTest(owner).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_READ_ROOM, PARAMETER_ID_COUNTRY).setParams(METHOD_READ_ROOM, request.getIdCountry()));

        RoomEntity room = chatDao.getRoom(request.getGame().getIdGame(), request.getRequest().getIdRoom());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(room).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_READ_ROOM, PARAMETER_REQUEST, PARAMETER_ID_ROOM).setParams(METHOD_READ_ROOM, request.getRequest().getIdRoom()));

        chatDao.readMessagesInRoom(request.getRequest().getIdRoom(), request.getIdCountry(), request.getRequest().getMaxId());

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(gameDiffs.getDiffs()));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }
}
