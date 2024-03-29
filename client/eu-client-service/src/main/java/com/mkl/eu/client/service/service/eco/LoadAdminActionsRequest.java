package com.mkl.eu.client.service.service.eco;

/**
 * Sub request for loadAdminActions service.
 *
 * @author MKL.
 */
public class LoadAdminActionsRequest {
    /** Id of the game. */
    private Long idGame;
    /** Turn of the sheets to return. */
    private Integer turn;

    /**
     * Constructor for jaxb.
     */
    public LoadAdminActionsRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame the idGame to set.
     * @param turn   the turn to set.
     */
    public LoadAdminActionsRequest(Long idGame, Integer turn) {
        this.idGame = idGame;
        this.turn = turn;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the turn. */
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
