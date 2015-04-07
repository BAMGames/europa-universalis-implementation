package com.mkl.eu.client.common.exception;

/**
 * <p>
 * Fonctional Exception.
 * </p>
 * <p>
 * These exceptions often occured due to business logic or user input.
 * </p>
 *
 * @author MKL
 */
public class FunctionalException extends Exception {

    /** serial. */
    private static final long serialVersionUID = 3983229995248851206L;

    /** Code of the exception. Can be used to internationalize the error message. */
    private String code;

    /** Parameters of the exception. Add context to the exception. */
    private Object[] params;

    /**
     * Constructor.
     *
     * @param code    should be unique. Cannot be <code>null</code>.
     * @param message to be logged.<code>null</code> value is possible but not recommanded.
     * @param cause   exception that causes this one. Used for the stack logging. Can be <code>null</code>.
     * @param params  context of te exception. Used with code to display a suitable message to the user. Can be <code>null</code>.
     */
    public FunctionalException(String code, String message, Throwable cause, Object... params) {
        super(message, cause);
        this.code = code;
        this.params = params;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the params
     */
    public Object[] getParams() {
        Object[] returnedParams = null;
        if (params != null) {
            returnedParams = params.clone();
        }
        return returnedParams;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Object[] params) {
        this.params = params == null ? null : params.clone();
    }
}
