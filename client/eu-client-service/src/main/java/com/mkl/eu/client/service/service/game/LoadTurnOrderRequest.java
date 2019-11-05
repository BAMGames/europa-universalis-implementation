package com.mkl.eu.client.service.service.game;

/**
 * Sub request for loadTurnOrder service.
 *
 * @author MKL.
 */
public class LoadTurnOrderRequest {
    /** Id of the game. */
    private Long idGame;

    /**
     * Constructor for jaxb.
     */
    public LoadTurnOrderRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame     the idGame to set.
     */
    public LoadTurnOrderRequest(Long idGame) {
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
