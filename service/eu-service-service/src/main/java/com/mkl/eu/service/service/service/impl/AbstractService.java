package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.CountryOrderEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import com.mkl.eu.service.service.socket.AfterCommitTransaction;
import com.mkl.eu.service.service.socket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract service, parent of services who want some utility methods.
 *
 * @author MKL.
 */
public abstract class AbstractService extends AbstractBack {
    /** Game DAO. */
    @Autowired
    protected IGameDao gameDao;
    /** Diff DAO. */
    @Autowired
    private IDiffDao diffDao;
    /** Chat DAO. */
    @Autowired
    protected IChatDao chatDao;
    /** Diff mapping. */
    @Autowired
    protected DiffMapping diffMapping;
    /** Chat mapping. */
    @Autowired
    protected ChatMapping chatMapping;

    /**
     * Check that game info are properly assigned, retrieve the game and its diffs and return it.
     * Does not lock the game.
     *
     * @param gameInfo to check.
     * @param method   calling this. For logging purpose.
     * @param param    name of the param holding the gameInfo. For logging purpose.
     * @return the game and its diffs.
     * @throws FunctionalException functional exception.
     */
    protected GameDiffsInfo checkGameAndGetDiffsAsReader(GameInfo gameInfo, String method, String param) throws FunctionalException {
        return checkGameAndGetDiffs(gameInfo, method, param, gameDao::load);
    }

    /**
     * Check that game info are properly assigned, retrieve the game and its diffs and return it.
     * Lock the game until the end of the transaction.
     *
     * @param gameInfo to check.
     * @param method   calling this. For logging purpose.
     * @param param    name of the param holding the gameInfo. For logging purpose.
     * @return the game and its diffs.
     * @throws FunctionalException functional exception.
     */
    protected GameDiffsInfo checkGameAndGetDiffsAsWriter(GameInfo gameInfo, String method, String param) throws FunctionalException {
        return checkGameAndGetDiffs(gameInfo, method, param, gameDao::lock);
    }

    /**
     * Check that game info are properly assigned, retrieve the game and its diffs and return it.
     *
     * @param gameInfo      to check.
     * @param method        calling this. For logging purpose.
     * @param param         name of the param holding the gameInfo. For logging purpose.
     * @param gameRetriever the dao function to call to retrieve the game.
     * @return the game and its diffs.
     * @throws FunctionalException functional exception.
     */
    protected GameDiffsInfo checkGameAndGetDiffs(GameInfo gameInfo, String method, String param, Function<Long, GameEntity> gameRetriever) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(gameInfo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME).setParams(method));

        Long idGame = gameInfo.getIdGame();
        Long versionGame = gameInfo.getVersionGame();

        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method));

        GameEntity game = gameRetriever.apply(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame <= game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, gameInfo.getIdCountry(), versionGame);

        return new GameDiffsInfo(game, diffs);
    }

    /**
     * Check if the country is active.
     *
     * @param game      game to check.
     * @param idCountry id of the country asking if it is active.
     * @return <code>true</code> if the country is active.
     */
    protected boolean isCountryActive(GameEntity game, Long idCountry) {
        boolean active;
        switch (game.getStatus()) {
            case ADMINISTRATIVE_ACTIONS_CHOICE:
            case MILITARY_HIERARCHY:
                active = true;
                break;
            case MILITARY_CAMPAIGN:
            case MILITARY_SUPPLY:
            case MILITARY_MOVE:
            case MILITARY_BATTLES:
            case MILITARY_SIEGES:
            case MILITARY_NEUTRALS:
                CountryOrderEntity activeOrder = CommonUtil.findFirst(game.getOrders().stream(),
                        order -> order.isActive() &&
                                order.getCountry().getId().equals(idCountry));
                active = activeOrder != null;
                break;
            default:
                active = false;
                break;
        }

        return active;
    }

    /**
     * Check if game has the right status.
     *
     * @param game      game to check.
     * @param idCountry id of the country asking for an action (some status are country ordered).
     * @param method    calling this. For logging purpose.
     * @param param     name of the param holding the gameInfo. For logging purpose.
     * @param status    status the game should have. If there is multiple values, then one of them must be true.
     * @throws FunctionalException functional exception.
     */
    protected void checkGameStatus(GameEntity game, Long idCountry, String method, String param, GameStatusEnum... status) throws FunctionalException {
        List<GameStatusEnum> statuses = Arrays.asList(status);
        boolean ok = statuses.contains(game.getStatus());
        if (ok && statuses.stream().anyMatch(GameStatusEnum::isNotSimultaneous)) {
            ok = isPhasingPlayer(game, idCountry);
        }

        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(ok)
                .setCodeError(IConstantsServiceException.INVALID_STATUS)
                .setMsgFormat(MSG_INVALID_STATUS)
                .setName(param, PARAMETER_REQUEST)
                .setParams(method, game.getStatus(), status));
    }

    /**
     * @param game      the game.
     * @param idCountry the country.
     * @return <code>true</code> if the country is the phasing side during a non simultaneous phase, <code>false</code> otherwise.
     */
    protected boolean isPhasingPlayer(GameEntity game, Long idCountry) {
        return game.getOrders().stream()
                .anyMatch(order -> order.isActive() &&
                        order.getCountry().getId().equals(idCountry));
    }

    /**
     * Check if game has the right simple status (no player order).
     *
     * @param game   game to check.
     * @param status status the game should have.
     * @param method calling this. For logging purpose.
     * @param param  name of the param holding the gameInfo. For logging purpose.
     * @throws FunctionalException
     */
    protected void checkSimpleStatus(GameEntity game, GameStatusEnum status, String method, String param) throws FunctionalException {
        failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                .setTest(game.getStatus() == status)
                .setCodeError(IConstantsServiceException.INVALID_STATUS)
                .setMsgFormat(MSG_INVALID_STATUS)
                .setName(param, PARAMETER_REQUEST)
                .setParams(method, game.getStatus(), status));
    }

    /**
     * Creates the diff and then return a Response.
     *
     * @param diff      the diff to create.
     * @param gameDiffs the existing game and diffs.
     * @param request   the request.
     * @param <T>       type of request.
     * @return a Response.
     */
    protected <T> DiffResponse createDiff(DiffEntity diff, GameDiffsInfo gameDiffs, Request<T> request) {
        return createDiffs(Collections.singletonList(diff), gameDiffs, request);
    }

    /**
     * Creates the diffs and then return a Response.
     *
     * @param newDiffs  the diffs to create.
     * @param gameDiffs the existing game and diffs.
     * @param request   the request.
     * @param <T>       type of request.
     * @return a Response.
     */
    protected <T> DiffResponse createDiffs(List<DiffEntity> newDiffs, GameDiffsInfo gameDiffs, Request<T> request) {
        newDiffs.stream()
                .forEach(diffDao::create);
        push(newDiffs);

        List<DiffEntity> diffs = new ArrayList<>(gameDiffs.getDiffs());
        diffs.addAll(newDiffs);
        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
    }

    /**
     * Push some diffs to all clients listening to this game.
     *
     * @param diffEntities to push.
     */
    protected void push(List<DiffEntity> diffEntities) {
        Long versionGame = diffEntities.stream()
                .map(DiffEntity::getVersionGame)
                .findAny()
                .orElse(null);
        Long idGame = diffEntities.stream()
                .map(DiffEntity::getIdGame)
                .findAny()
                .orElse(null);

        DiffResponse response = new DiffResponse();
        List<Diff> diffs = diffEntities.stream()
                .map(diffMapping::oeToVo)
                .collect(Collectors.toList());
        response.getDiffs().addAll(diffs);
        response.setVersionGame(versionGame);

        TransactionSynchronizationManager.registerSynchronization((AfterCommitTransaction) () -> WebSocketServer.push(idGame, response, null));
    }

    /**
     * Push a message to all clients listening to this game.
     *
     * @param message     to be pushed.
     * @param idGame      id of the game.
     * @param idCountries list of countries that will receive the message.
     */
    protected void push(MessageDiff message, Long idGame, List<Long> idCountries) {
        DiffResponse response = new DiffResponse();
        response.getMessages().add(message);

        TransactionSynchronizationManager.registerSynchronization((AfterCommitTransaction) () -> WebSocketServer.push(idGame, response, idCountries));
    }

    /**
     * Retrieve all the message (global and non global) since the last time specified in the chatInfo for a given game and a given country.
     *
     * @param request the request containing all the info needed.
     * @param <T>     type of request.
     * @return all the message (global and non global) since the last time specified in the chatInfo for a given game and a given country.
     */
    protected <T> List<MessageDiff> getMessagesSince(Request<T> request) {
        List<MessageDiff> messages = new ArrayList<>();

        if (request != null && request.getGame() != null && request.getChat() != null) {
            List<ChatEntity> chatEntities = chatDao.getMessagesSince(request.getGame().getIdGame(), request.getGame().getIdCountry(), request.getChat().getMaxIdMessage());
            List<MessageGlobalEntity> messageEntities = chatDao.getMessagesGlobalSince(request.getGame().getIdGame(), request.getChat().getMaxIdGlobalMessage());


            Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();
            messages.addAll(chatMapping.oesToVosChatSince(chatEntities, objectsCreated));
            messages.addAll(chatMapping.oesToVosMessageSince(messageEntities, objectsCreated));
        }

        return messages;
    }
}
