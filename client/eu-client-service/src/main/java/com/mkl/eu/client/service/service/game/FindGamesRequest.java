package com.mkl.eu.client.service.service.game;

/**
 * Sub request for loadGame service.
 *
 * @author MKL.
 */
public class FindGamesRequest {
    /** Login of one of the players of the game. */
    private String username;
    /** Flag saying that the game is finished. */
    private boolean finished;

    /**
     * Constructor for jaxb.
     */
    public FindGamesRequest() {
    }

    /** @return the username. */
    public String getUsername() {
        return username;
    }

    /** @param username the username to set. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the finished. */
    public boolean isFinished() {
        return finished;
    }

    /** @param finished the finished to set. */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
