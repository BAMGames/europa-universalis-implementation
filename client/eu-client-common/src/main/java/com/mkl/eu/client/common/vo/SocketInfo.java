package com.mkl.eu.client.common.vo;

import java.io.Serializable;

/**
 * Information about a socket.
 *
 * @author MKL.
 */
public class SocketInfo implements Serializable {
    /** Username doing the request. */
    private String username;
    /** Password linked to the username. */
    private String password;
    /** Id of the game. */
    private Long idGame;
    /** Id of the country. */
    private Long idCountry;

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

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the idCountry. */
    public Long getIdCountry() {
        return idCountry;
    }

    /** @param idCountry the idCountry to set. */
    public void setIdCountry(Long idCountry) {
        this.idCountry = idCountry;
    }
}
