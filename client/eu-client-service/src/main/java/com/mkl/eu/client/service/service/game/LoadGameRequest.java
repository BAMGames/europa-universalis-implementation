package com.mkl.eu.client.service.service.game;

/**
 * Request for loadGame service.
 *
 * @author MKL.
 */
public class LoadGameRequest {
    /** Id of the game to load. */
    private Long idGame;

    /**
     * Constructor for jaxb.
     */
    public LoadGameRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame the idGame to set.
     */
    public LoadGameRequest(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }
}
