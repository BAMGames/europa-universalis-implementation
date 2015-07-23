package com.mkl.eu.service.service.ws.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.CreateRoomRequest;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

/**
 * Description of file.
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
}
