package com.mkl.eu.client.service.service.game;

/**
 * Request for moveCounter service.
 *
 * @author MKL.
 */
public class MoveCounterRequest {
    /** Id of the game. */
    private Long idGame;
    /** Vrsion of the game. */
    private Long versionGame;
    /** Id of the counter to move. */
    private Long idCounter;
    /** Id of the stack where the counter will move to. Can be <code>null</code> for creation of a stack. */
    private Long idStack;

    /**
     * Constructor for jaxb.
     */
    public MoveCounterRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame      the idGame to set.
     * @param versionGame the versionGame to set.
     * @param idCounter   the idCounter to set.
     */
    public MoveCounterRequest(Long idGame, Long versionGame, Long idCounter) {
        this.idGame = idGame;
        this.versionGame = versionGame;
        this.idCounter = idCounter;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @param idGame the idGame to set. */
    public void setIdGame(Long idGame) {
        this.idGame = idGame;
    }

    /** @return the versionGame. */
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
    }

    /** @return the idCounter. */
    public Long getIdCounter() {
        return idCounter;
    }

    /** @param idCounter the idCounter to set. */
    public void setIdCounter(Long idCounter) {
        this.idCounter = idCounter;
    }

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }
}
