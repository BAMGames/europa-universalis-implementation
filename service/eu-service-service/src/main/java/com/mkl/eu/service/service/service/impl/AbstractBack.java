package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.service.INameConstants;
import com.mkl.eu.client.service.vo.ref.Referential;
import com.mkl.eu.client.service.vo.tables.Tables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Abstract class, parent of services and domains.
 *
 * @author MKL.
 */
public abstract class AbstractBack implements INameConstants {
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
    /** Error message when a table entry is missing. */
    public static final String MSG_MISSING_TABLE = "Entry {1} for table {0} does not exist.";
    /** Error message when an entity is missing. */
    public static final String MSG_MISSING_ENTITY = "Entity of type {1} with identifier {0} does not exist.";

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
     * @param code      of the exception.
     * @param msgFormat message template of the exception.
     * @param params    parameters of the exception.
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
            cleanParams();
            return params;
        }

        /**
         * Remove generic classes for developer, but not for cxf, which does not understand these types.
         */
        private void cleanParams() {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Collection) {
                    params[i] = ((Collection<?>) params[i]).stream().map(Objects::toString).collect(Collectors.joining(","));
                } else if (params[i] instanceof Enum) {
                    params[i] = ((Enum<?>) params[i]).name();
                }
            }
        }

        /** @param params the params to set. */
        public CheckForThrow<T> setParams(Object... params) {
            this.params = params;
            return this;
        }
    }
}
