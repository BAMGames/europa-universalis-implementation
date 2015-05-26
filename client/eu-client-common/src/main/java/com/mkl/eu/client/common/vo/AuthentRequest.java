package com.mkl.eu.client.common.vo;

/**
 * Request Wrapper with authentication info.
 *
 * @author MKL.
 */
public class AuthentRequest<T> {
    /** The wrapped request. */
    private T request;
    /** Username doing the request. */
    private String username;
    /** Password linked to the username. */
    private String password;

    /** @return the request. */
    public T getRequest() {
        return request;
    }

    /** @param request the request to set. */
    public void setRequest(T request) {
        this.request = request;
    }

    /** @return the username. */
    public String getUsername() {
        return username;
    }

    /** @param username the username to set. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the password. */
    public String getPassword() {
        return password;
    }

    /** @param password the password to set. */
    public void setPassword(String password) {
        this.password = password;
    }
}
