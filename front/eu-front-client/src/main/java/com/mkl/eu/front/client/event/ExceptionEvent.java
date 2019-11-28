package com.mkl.eu.front.client.event;

import com.mkl.eu.front.client.window.InteractiveMap;

/**
 * Event when an exception is being thrown.
 *
 * @author MKL.
 */
public class ExceptionEvent {
    /** Exception thrown. */
    private Exception exception;
    /** Possible Interactive Map. */
    private InteractiveMap map;

    /**
     * Constructor.
     *
     * @param exception the exception.
     * @param map       possible interactive map.
     */
    public ExceptionEvent(Exception exception, InteractiveMap map) {
        this.exception = exception;
        this.map = map;
    }

    /** @return the exception. */
    public Exception getException() {
        return exception;
    }

    /** @param exception the exception to set. */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /** @return the map. */
    public InteractiveMap getMap() {
        return map;
    }

    /** @param map the map to set. */
    public void setMap(InteractiveMap map) {
        this.map = map;
    }
}
