package com.mkl.eu.client.common.vo;

/**
 * Request Wrapper with authentication info.
 *
 * @author MKL.
 */
public class SimpleRequest<T> {
    /** The wrapped request. */
    private T request;
    /** Authentication info. */
    private AuthentInfo authent;

    /** @return the request. */
    public T getRequest() {
        return request;
    }

    /** @param request the request to set. */
    public void setRequest(T request) {
        this.request = request;
    }

    /** @return the authent. */
    public AuthentInfo getAuthent() {
        return authent;
    }

    /** @param authent the authent to set. */
    public void setAuthent(AuthentInfo authent) {
        this.authent = authent;
    }
}
