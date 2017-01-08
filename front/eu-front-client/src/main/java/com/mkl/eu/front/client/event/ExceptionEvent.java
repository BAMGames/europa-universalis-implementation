package com.mkl.eu.front.client.event;

/**
 * Event when an exception is being thrown.
 *
 * @author MKL.
 */
public class ExceptionEvent {
    /** Exception thrown. */
    private Exception exception;

    /**
     * Constructor.
     *
     * @param exception the exception.
     */
    public ExceptionEvent(Exception exception) {
        this.exception = exception;
    }

    /** @return the exception. */
    public Exception getException() {
        return exception;
    }

    /** @param exception the exception to set. */
    public void setException(Exception exception) {
        this.exception = exception;
    }
}
