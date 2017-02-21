package com.mkl.eu.client.service.service.game;

import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;

/**
 * Sub request for loadTurnOrder service.
 *
 * @author MKL.
 */
public class LoadTurnOrderRequest {
    /** Id of the game. */
    private Long idGame;
    /** Phase of the game whose we want the turn order. */
    private GameStatusEnum gameStatus;

    /**
     * Constructor for jaxb.
     */
    public LoadTurnOrderRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame     the idGame to set.
     * @param gameStatus the gameStatus to set.
     */
    public LoadTurnOrderRequest(Long idGame, GameStatusEnum gameStatus) {
        this.idGame = idGame;
        this.gameStatus = gameStatus;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the gameStatus. */
    public GameStatusEnum getGameStatus() {
        return gameStatus;
    }

    /** @param gameStatus the gameStatus to set. */
    public void setGameStatus(GameStatusEnum gameStatus) {
        this.gameStatus = gameStatus;
    }
}
