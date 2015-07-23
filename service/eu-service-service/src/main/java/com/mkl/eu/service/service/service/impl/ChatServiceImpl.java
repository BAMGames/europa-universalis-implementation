package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.CreateRoomRequest;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.country.IPlayableCountryDao;
import com.mkl.eu.service.service.persistence.oe.chat.*;
import com.mkl.eu.service.service.persistence.oe.country.PlayableCountryEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
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
    /** Chat DAO. */
    @Autowired
    private IChatDao chatDao;
    /** PlayablleCountry DAO. */
    @Autowired
    private IPlayableCountryDao playableCountryDao;

    /** {@inheritDoc} */
    @Override
    public DiffResponse createRoom(Request<CreateRoomRequest> createRoom) throws FunctionalException, TechnicalException {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse speakInRoom(Request<SpeakInRoomRequest> speakInRoom) throws FunctionalException, TechnicalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM).setParams(METHOD_SPEAK_IN_ROOM));

        GameDiffsInfo gameDiffs = checkGameAndGetDiffs(speakInRoom.getGame(), METHOD_SPEAK_IN_ROOM, PARAMETER_SPEAK_IN_ROOM);

        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom.getRequest()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST).setParams(METHOD_SPEAK_IN_ROOM));
        failIfNull(new AbstractService.CheckForThrow<>().setTest(speakInRoom.getRequest().getIdCountry()).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(PARAMETER_SPEAK_IN_ROOM, PARAMETER_REQUEST, PARAMETER_ID_COUNTRY).setParams(METHOD_SPEAK_IN_ROOM));
        failIfFalse(new AbstractService.CheckForThrow<Boolean>().setTest(!StringUtils.isEmpty(speakInRoom.getRequest().getMessage())).setCodeError(IConstantsCommonException.NULL_PARAMETER)
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

        return response;
    }
}
