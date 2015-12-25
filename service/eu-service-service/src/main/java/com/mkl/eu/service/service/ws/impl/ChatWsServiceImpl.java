package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.*;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Separation from ChatService because cxf can't handle @Transactional.
 *
 * @author MKL.
 */
@WebService(endpointInterface = "com.mkl.eu.client.service.service.IChatService")
public class ChatWsServiceImpl extends SpringBeanAutowiringSupport implements IChatService {
    /** Chat Service. */
    @Autowired
    @Qualifier(value = "chatServiceImpl")
    private IChatService chatService;

    /** {@inheritDoc} */
    @Override
    public DiffResponse createRoom(Request<CreateRoomRequest> createRoom) throws FunctionalException, TechnicalException {
        return chatService.createRoom(createRoom);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse speakInRoom(Request<SpeakInRoomRequest> speakInRoom) throws FunctionalException, TechnicalException {
        return chatService.speakInRoom(speakInRoom);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse toggleRoom(Request<ToggleRoomRequest> toggleRoom) throws FunctionalException, TechnicalException {
        return chatService.toggleRoom(toggleRoom);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse inviteKickRoom(Request<InviteKickRoomRequest> inviteKickRoom) throws FunctionalException, TechnicalException {
        return chatService.inviteKickRoom(inviteKickRoom);
    }

    /** {@inheritDoc} */
    @Override
    public Room loadRoom(SimpleRequest<LoadRoomRequest> loadRoom) throws FunctionalException, TechnicalException {
        return chatService.loadRoom(loadRoom);
    }

    /** {@inheritDoc} */
    @Override
    public DiffResponse readRoom(Request<ReadRoomRequest> readRoom) throws FunctionalException, TechnicalException {
        return chatService.readRoom(readRoom);
    }
}
