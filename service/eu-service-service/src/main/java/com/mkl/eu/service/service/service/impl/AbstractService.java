package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.IConstantsCommonException;
import com.mkl.eu.client.common.vo.ChatInfo;
import com.mkl.eu.client.common.vo.GameInfo;
import com.mkl.eu.client.service.service.INameConstants;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.service.service.mapping.chat.ChatMapping;
import com.mkl.eu.service.service.mapping.diff.DiffMapping;
import com.mkl.eu.service.service.persistence.IGameDao;
import com.mkl.eu.service.service.persistence.chat.IChatDao;
import com.mkl.eu.service.service.persistence.diff.IDiffDao;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.chat.ChatEntity;
import com.mkl.eu.service.service.persistence.oe.chat.MessageGlobalEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;
import com.mkl.eu.service.service.service.GameDiffsInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract service, parent of services who want some utility methods.
 *
 * @author MKL.
 */
public abstract class AbstractService implements INameConstants {
    /** Logger. */
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
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
    /** Game DAO. */
    @Autowired
    protected IGameDao gameDao;
    /** Diff DAO. */
    @Autowired
    protected IDiffDao diffDao;
    /** Chat DAO. */
    @Autowired
    protected IChatDao chatDao;
    /** Diff mapping. */
    @Autowired
    protected DiffMapping diffMapping;
    /** Chat mapping. */
    @Autowired
    private ChatMapping chatMapping;

    /**
     * Will throw a FunctionalException if the test is <code>null</code>.
     * @throws FunctionalException the exception.
     */
    protected void failIfNull(CheckForThrow check) throws FunctionalException {
        if (check.getTest() == null) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code> or empty.
     * @throws FunctionalException the exception.
     */
    protected void failIfEmpty(CheckForThrow<String> check) throws FunctionalException {
        if (StringUtils.isEmpty(check.getTest())) {
            fail(check);
        }
    }

    /**
     * Will throw a FunctionalException if the test is <code>null</code>.
     * @throws FunctionalException the exception.
     */
    protected void failIfFalse(CheckForThrow<Boolean> check) throws FunctionalException {
        if (check.getTest() == null || !check.getTest()) {
            fail(check);
        }
    }

    /**
     * Log and throws a FunctionalException.
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
        throw new FunctionalException(check.getCodeError(), msg, null, check.getName());
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
     * Retrieve all the message (global and non global) since the last time specified in the chatInfo for a given game and a given country.
     *
     * @param idGame    id of the game.
     * @param idCountry id of the country for the non global messages (can be <code>null</code>).
     * @param chatInfo  information on the last message received.
     * @return all the message (global and non global) since the last time specified in the chatInfo for a given game and a given country.
     */
    protected List<MessageDiff> getMessagesSince(Long idGame, Long idCountry, ChatInfo chatInfo) {
        List<MessageDiff> messages = new ArrayList<>();

        if (chatInfo != null && chatInfo.getMaxIdGlobalMessage() != null && chatInfo.getMaxIdMessage() != null) {
            List<ChatEntity> chatEntities = chatDao.getMessagesSince(idGame, idCountry, chatInfo.getMaxIdMessage());
            List<MessageGlobalEntity> messageEntities = chatDao.getMessagesGlobalSince(idGame, chatInfo.getMaxIdGlobalMessage());


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
