package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.chat.CreateRoomRequest;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;

/**
 * Interface for the chat service.
 *
 * @author MKL.
 */
public interface IChatService extends INameConstants {
    /**
     * Create a room.
     *
     * @param createRoom info of the room to create.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse createRoom(@WebParam(name = PARAMETER_CREATE_ROOM) Request<CreateRoomRequest> createRoom) throws FunctionalException, TechnicalException;

    /**
     * Speak in a room.
     *
     * @param speakInRoom info of the message to send in a room.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse speakInRoom(@WebParam(name = PARAMETER_SPEAK_IN_ROOM) Request<SpeakInRoomRequest> speakInRoom) throws FunctionalException, TechnicalException;
}
