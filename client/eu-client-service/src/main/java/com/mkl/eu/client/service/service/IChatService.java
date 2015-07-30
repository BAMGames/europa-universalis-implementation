package com.mkl.eu.client.service.service;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.chat.*;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.diff.DiffResponse;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * Interface for the chat service.
 *
 * @author MKL.
 */
@WebService
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

    /**
     * Toggle a room (set it visible / invisible) for the user asking for it.
     *
     * @param toggleRoom info about the room to toggle.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse toggleRoom(@WebParam(name = PARAMETER_TOGGLE_ROOM) Request<ToggleRoomRequest> toggleRoom) throws FunctionalException, TechnicalException;

    /**
     * Invite/kick a country in/from a room. Only the owner can do that and can't do it to himself.
     *
     * @param inviteKickRoom info about the room and user to invite/kick.
     * @return the diffs.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    DiffResponse inviteKickRoom(@WebParam(name = PARAMETER_INVITE_KICK_ROOM) Request<InviteKickRoomRequest> inviteKickRoom) throws FunctionalException, TechnicalException;


    /**
     * Load a room. Often used after an invite.
     *
     * @param loadRoom info about the room to load.
     * @return the room.
     * @throws FunctionalException functional exception.
     * @throws TechnicalException  technical exception.
     */
    @WebResult(name = RESPONSE)
    Room loadRoom(@WebParam(name = PARAMETER_TOGGLE_ROOM) SimpleRequest<LoadRoomRequest> loadRoom) throws FunctionalException, TechnicalException;
}
