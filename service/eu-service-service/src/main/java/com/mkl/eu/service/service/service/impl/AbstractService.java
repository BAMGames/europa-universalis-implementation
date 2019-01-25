package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IConstantsServiceException;
import com.mkl.eu.client.service.service.INameConstants;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
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
import com.mkl.eu.service.service.socket.SocketHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Abstract service, parent of services who want some utility methods.
 *
 * @author MKL.
 */
public abstract class AbstractService implements INameConstants {
    /** Logger. */
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    /** Tables cached. */
    public static Tables TABLES;
    /** Referential cached. */
    public static Referential REFERENTIAL;
    /** Error message when a parameter is missing. */
    public static final String MSG_MISSING_PARAMETER = "{1}: {0} missing.";
    /** Error message when an object is not found (in database mostly). */
    public static final String MSG_OBJECT_NOT_FOUND = "{1}: {0} {2} does not exist.";
    /** Error message when the game version is incorrect (greater than the one in database). */
    public static final String MSG_VERSION_INCORRECT = "{1}: {0} {2} is greater than actual ({3}).";
    /** Error message when a province is not the neighbor of another (assuming it should have been). */
    public static final String MSG_NOT_NEIGHBOR = "{1}: {0} {2} is not a neighbor of {3}.";
    /** Error message when an action should be done in a province and it is not. */
    public static final String MSG_NOT_SAME_PROVINCE = "{1}: {0} / The action should be in {2} but was in {3}.";
    /** Error message when an action should be done in a different stack. */
    public static final String MSG_NOT_SAME_STACK = "{1}: {0} / The stack should be another one.";
    /** Error message when an action is not authorized. */
    public static final String MSG_ACCESS_RIGHT = "{1}: {2} has not the right to perform this action. Should be {3}.";
    /** Error message when a counter limit would exceed. */
    public static final String MSG_COUNTER_LIMIT_EXCEED = "{1}: {0} The counter type {2} can''t be created by {3} because country limits were exceeded ({4}/{5}).";
    /** Error message when a counter is missing. */
    public static final String MSG_MISSING_COUNTER = "{1}: {0} The counter {2} for country {3} is missing. Please ask an admin for correction.";
    /** Error message when the status is invalid. */
    public static final String MSG_INVALID_STATUS = "{1}: {0} The status {2} is invalid: it should be {3}.";
    /** Error message when a table netry is missing. */
    public static final String MSG_MISSING_TABLE = "Entry {1} for table {0} does not exist.";
    /** Socket Handler. */
    @Autowired
    private SocketHandler socketHandler;
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
     * @return the Tables.
     */
    protected Tables getTables() {
        return TABLES;
    }

    /**
     * @return the Referential.
     */
    protected Referential getReferential() {
        return REFERENTIAL;
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code>.
     *
     * @throws FunctionalException the exception.
     */
    protected void failIfNull(CheckForThrow check) throws FunctionalException {
        if (check.getTest() == null) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is not <code>null</code>.
     *
     * @throws FunctionalException the exception.
     */
    protected void failIfNotNull(CheckForThrow check) throws FunctionalException {
        if (check.getTest() != null) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code> or empty.
     *
     * @throws FunctionalException the exception.
     */
    protected void failIfEmpty(CheckForThrow<String> check) throws FunctionalException {
        if (StringUtils.isEmpty(check.getTest())) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code> or <code>false</code>.
     *
     * @throws FunctionalException the exception.
     */
    protected void failIfFalse(CheckForThrow<Boolean> check) throws FunctionalException {
        if (check.getTest() == null || !check.getTest()) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code> or <code>true</code>.
     *
     * @throws FunctionalException the exception.
     */
    protected void failIfTrue(CheckForThrow<Boolean> check) throws FunctionalException {
        if (check.getTest() != null && check.getTest()) {
            fail(check);
        }
    }

    /**
     * Log and throws a FunctionalException.
     *
     * @throws FunctionalException the exception.
     */
    private void fail(CheckForThrow check) throws FunctionalException {
        Object[] args;
        if (check.getParams() != null) {
            args = new Object[check.getParams().length + 1];
            System.arraycopy(check.getParams(), 0, args, 1, check.getParams().length);
        } else {
            args = new Object[1];
        }
        args[0] = check.getName();
        String msg = MessageFormat.format(check.getMsgFormat(), args);
        LOGGER.error(msg);
        throw new FunctionalException(check.getCodeError(), msg, null, args);
    }

    /**
     *
     * @param code of the exception.
     * @param msgFormat message template of the exception.
     * @param params parameters of the exception.
     * @return a supplier of technical exception that will also log it.
     */
    protected Supplier<TechnicalException> createTechnicalExceptionSupplier(String code, String msgFormat, String... params) {
        return () -> createTechnicalException(code, msgFormat, params);
    }

    /**
     * @param code      of the exception.
     * @param msgFormat message template of the exception.
     * @param params    parameters of the exception.
     * @return a technical exception after logging it.
     */
    protected TechnicalException createTechnicalException(String code, String msgFormat, String... params) {
        String msg = MessageFormat.format(msgFormat, params);
        LOGGER.error(msg);
        return new TechnicalException(code, msg, null, params);
    }

    /**
     * Check that game info are properly assigned, retrieve the game and its diffs and return it.
     *
     * @param gameInfo to check.
     * @param method   calling this. For logging purpose.
     * @param param    name of the param holding the gameInfo. For logging purpose.
     * @return the game and its diffs.
     * @throws FunctionalException functional exception.
     */
    protected GameDiffsInfo checkGameAndGetDiffs(GameInfo gameInfo, String method, String param) throws FunctionalException {
        failIfNull(new AbstractService.CheckForThrow<>().setTest(gameInfo).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME).setParams(method));

        Long idGame = gameInfo.getIdGame();
        Long versionGame = gameInfo.getVersionGame();

        failIfNull(new CheckForThrow<>().setTest(idGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method));
        failIfNull(new CheckForThrow<>().setTest(versionGame).setCodeError(IConstantsCommonException.NULL_PARAMETER)
                .setMsgFormat(MSG_MISSING_PARAMETER).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method));

        GameEntity game = gameDao.lock(idGame);

        failIfNull(new CheckForThrow<>().setTest(game).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_OBJECT_NOT_FOUND).setName(param, PARAMETER_GAME, PARAMETER_ID_GAME).setParams(method, idGame));
        failIfFalse(new CheckForThrow<Boolean>().setTest(versionGame < game.getVersion()).setCodeError(IConstantsCommonException.INVALID_PARAMETER)
                .setMsgFormat(MSG_VERSION_INCORRECT).setName(param, PARAMETER_GAME, PARAMETER_VERSION_GAME).setParams(method, versionGame, game.getVersion()));

        List<DiffEntity> diffs = diffDao.getDiffsSince(idGame, versionGame);

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
                                order.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
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
     * @param status    status the game should have.
     * @param idCountry id of the country asking for an action (some status are country ordered).
     * @param method    calling this. For logging purpose.
     * @param param     name of the param holding the gameInfo. For logging purpose.
     * @throws FunctionalException functional exception.
     */
    protected void checkGameStatus(GameEntity game, GameStatusEnum status, Long idCountry, String method, String param) throws FunctionalException {
        checkSimpleStatus(game, status, method, param);
        switch (status) {
            case MILITARY_CAMPAIGN:
            case MILITARY_SUPPLY:
            case MILITARY_MOVE:
            case MILITARY_BATTLES:
            case MILITARY_SIEGES:
            case MILITARY_NEUTRALS:
                failIfFalse(new AbstractService.CheckForThrow<Boolean>()
                        .setTest(isPhasingPlayer(game, idCountry))
                        .setCodeError(IConstantsServiceException.INVALID_STATUS)
                        .setMsgFormat(MSG_INVALID_STATUS)
                        .setName(param, PARAMETER_REQUEST)
                        .setParams(method, game.getStatus(), status));
                break;
            default:
                break;
        }
    }

    /**
     * @param game      the game.
     * @param idCountry the country.
     * @return <code>true</code> if the country is the phasing side during a military round, <code>false</code> otherwise.
     */
    protected boolean isPhasingPlayer(GameEntity game, Long idCountry) {
        return game.getOrders().stream()
                .anyMatch(order -> order.isActive() &&
                        order.getGameStatus() == GameStatusEnum.MILITARY_MOVE &&
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
     * Creates and push a diff.
     *
     * @param diff to create and push.
     */
    protected void createDiff(DiffEntity diff) {
        diffDao.create(diff);

        push(Collections.singletonList(diff));
    }

    /**
     * Creates and push some diffs.
     *
     * @param diffs to create and push.
     */
    protected void createDiffs(List<DiffEntity> diffs) {
        diffs.stream()
                .forEach(diffDao::create);

        push(diffs);
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
        createDiff(diff);
        List<DiffEntity> diffs = gameDiffs.getDiffs();
        diffs.add(diff);
        DiffResponse response = new DiffResponse();
        response.setDiffs(diffMapping.oesToVos(diffs));
        response.setVersionGame(gameDiffs.getGame().getVersion());

        response.setMessages(getMessagesSince(request));

        return response;
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
        createDiffs(newDiffs);
        List<DiffEntity> diffs = gameDiffs.getDiffs();
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

        socketHandler.push(idGame, response, null);
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

        socketHandler.push(idGame, response, idCountries);
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
            List<ChatEntity> chatEntities = chatDao.getMessagesSince(request.getGame().getIdGame(), request.getIdCountry(), request.getChat().getMaxIdMessage());
            List<MessageGlobalEntity> messageEntities = chatDao.getMessagesGlobalSince(request.getGame().getIdGame(), request.getChat().getMaxIdGlobalMessage());


            Map<Class<?>, Map<Long, Object>> objectsCreated = new HashMap<>();
            messages.addAll(chatMapping.oesToVosChatSince(chatEntities, objectsCreated));
            messages.addAll(chatMapping.oesToVosMessageSince(messageEntities, objectsCreated));
        }

        return messages;
    }

    /**
     * Class substituting parameters for failIf* methods.
     */
    protected static class CheckForThrow<T> {
        /** Object to test for failure. */
        private T test;
        /** Name of the parameter responsible of this test. Will be put in the exception parameters. */
        private String name;
        /** Code error to throw if it fails. */
        private String codeError;
        /** Message format used to log and to put in the exception if it fails. */
        private String msgFormat;
        /** Additional params to give to the message format (name is the first one). */
        private Object[] params;

        /** @return the test. */
        public T getTest() {
            return test;
        }

        /** @param test the test to set. */
        public CheckForThrow<T> setTest(T test) {
            this.test = test;
            return this;
        }

        /** @return the name. */
        public String getName() {
            return name;
        }

        /** @param name the name to set. */
        public CheckForThrow<T> setName(String name) {
            this.name = name;
            return this;
        }

        /** @param names the name to set. */
        public CheckForThrow<T> setName(String... names) {
            this.name = String.join(".", names);
            return this;
        }

        /** @return the codeError. */
        public String getCodeError() {
            return codeError;
        }

        /** @param codeError the codeError to set. */
        public CheckForThrow<T> setCodeError(String codeError) {
            this.codeError = codeError;
            return this;
        }

        /** @return the msgFormat. */
        public String getMsgFormat() {
            return msgFormat;
        }

        /** @param msgFormat the msgFormat to set. */
        public CheckForThrow<T> setMsgFormat(String msgFormat) {
            this.msgFormat = msgFormat;
            return this;
        }

        /** @return the params. */
        public Object[] getParams() {
            return params;
        }

        /** @param params the params to set. */
        public CheckForThrow<T> setParams(Object... params) {
            this.params = params;
            return this;
        }
    }
}
