package com.mkl.eu.client.common.vo;

/**
 * Information on authentication.
 *
 * @author MKL.
 */
public class AuthentInfo {
    /** Username doing the request. */
    private String username;
    /** Password linked to the username. */
    private String password;

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
