package com.mkl.eu.client.common.vo;

/**
 * Information on authentication.
 *
 * @author MKL.
 */
public class AuthentInfo {
    /** Username used for not logged purpose. */
    public static final String USERNAME_ANONYMOUS = "anonymous";
    /** AuthentInfo for not logged account. */
    public static final AuthentInfo ANONYMOUS = new AuthentInfo(USERNAME_ANONYMOUS, null);
    /** Username doing the request. */
    private String username;
    /** Password linked to the username. */
    private String password;

    /**
     * Constructor with no arg.
     */
    public AuthentInfo() {
    }

    /**
     * Constructor with username and password.
     *
     * @param username the username to set.
     * @param password the password to set.
     */
    public AuthentInfo(String username, String password) {
        this.username = username;
        this.password = password;
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
