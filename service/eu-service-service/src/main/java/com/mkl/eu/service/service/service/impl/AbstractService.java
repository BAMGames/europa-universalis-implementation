package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Abstract service, parent of services who want some utility methods.
 *
 * @author MKL.
 */
public abstract class AbstractService {
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
    /** Error message when an object is not found (in database mostly). */
    public static final String MSG_ACCESS_RIGHT = "{1}: {2} has not the right to perform this action.";

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
