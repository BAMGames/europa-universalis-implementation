package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.CreateRoomRequest;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeObjectEnum;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffAttributesEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for chat purpose.
 *
 * @author MKL.
 */
@Service
@Transactional(rollbackFor = {TechnicalException.class, FunctionalException.class})
public class ChatServiceImpl extends AbstractService implements IChatService {
    /** Error message when an object is not found (in database mostly). */
    public static final String MSG_ROOM_ALREADY_EXIST = "{1}: {0} a room with name '{2}' already exists.";
    /** PlayablleCountry DAO. */
    @Autowired
    private IPlayableCountryDao playableCountryDao;

    /** {@inheritDoc} */
    @Override
    public DiffResponse createRoom(Request<CreateRoomRequest> createRoom) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in request AND chatInfo
        failIfNull(new AbstractService.CheckForThrow<>().setTest(createRoom).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM).setParams(METHOD_CREATE_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(createRoom.getGame(), METHOD_CREATE_ROOM, PARAMETER_CREATE_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(createRoom.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST).setParams(METHOD_CREATE_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(createRoom.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_CREATE_ROOM));
        failIfEmpty(new AbstractService.CheckForThrow<String>().setTest(createRoom.getRequest().getName()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_NAME).setParams(METHOD_CREATE_ROOM));

        String name = createRoom.getRequest().getName();
        PlayableCountryEntity owner = playableCountryDao.load(createRoom.getRequest().getIdCountry());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(owner).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_CREATE_ROOM, createRoom.getRequest().getIdCountry()));

        RoomEntity room = chatDao.getRoom(createRoom.getGame().getIdGame(), name);

        failIfNotNull(new AbstractService.CheckForThrow<>().setTest(room).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_ROOM_ALREADY_EXIST).setName(PARAMETER_CREATE_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_CREATE_ROOM, name));

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

        DiffEntity diff = new DiffEntity();
        diff.setIdGame(game.getId());
        diff.setVersionGame(game.getVersion());
        diff.setType(DiffTypeEnum.ADD);
        diff.setTypeObject(DiffTypeObjectEnum.ROOM);
        diff.setIdObject(room.getId());
        DiffAttributesEntity diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.NAME);
        diffAttributes.setValue(name);
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffAttributes = new DiffAttributesEntity();
        diffAttributes.setType(DiffAttributeTypeEnum.ID_COUNTRY);
        diffAttributes.setValue(owner.getId().toString());
        diffAttributes.setDiff(diff);
        diff.getAttributes().add(diffAttributes);
        diffDao.create(diff);

        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(createRoom.getGame().getIdGame(), createRoom.getChat()));

        return response;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse speakInRoom(Request<SpeakInRoomRequest> speakInRoom) throws FunctionalException, TechnicalException {
        // TODO check idCountry and authent in request AND chatInfo
        // TODO check if speaker is present in room
        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM).setParams(METHOD_SPEAK_IN_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(speakInRoom.getGame(), METHOD_SPEAK_IN_ROOM, PARAMETER_SPEAK_IN_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST).setParams(METHOD_SPEAK_IN_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_SPEAK_IN_ROOM));
        failIfEmpty(new AbstractService.CheckForThrow<String>().setTest(speakInRoom.getRequest().getMessage()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST, PARAMETER_MESSAGE).setParams(METHOD_SPEAK_IN_ROOM));

        PlayableCountryEntity sender = playableCountryDao.load(speakInRoom.getRequest().getIdCountry());

        failIfNull(new AbstractService.CheckForThrow<>().setTest(sender).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_SPEAK_IN_ROOM, speakInRoom.getRequest().getIdCountry()));

        if (speakInRoom.getRequest().getIdRoom() == null) {
            RoomGlobalEntity room = chatDao.getRoomGlobal(speakInRoom.getGame().getIdGame());
            MessageGlobalEntity message = new MessageGlobalEntity();
            message.setDateSent(ZonedDateTime.now());
            message.setMessage(speakInRoom.getRequest().getMessage());
            message.setRoom(room);
            message.setSender(sender);
            room.getMessages().add(message);
            chatDao.createMessage(message);
        } else {
            RoomEntity room = chatDao.getRoom(speakInRoom.getGame().getIdGame(), speakInRoom.getRequest().getIdRoom());
            MessageEntity message = new MessageEntity();
            message.setDateSent(ZonedDateTime.now());
            message.setMessage(speakInRoom.getRequest().getMessage());
            message.setSender(sender);

            List<ChatEntity> messages = new ArrayList<>();

            for (PresentEntity present : room.getPresents()) {
                if (!present.isPresent()) {
                    continue;
                }
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
        }

        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(gameDiffs.getDiffs()));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(speakInRoom.getGame().getIdGame(), speakInRoom.getChat()));

        return response;
    }
}
