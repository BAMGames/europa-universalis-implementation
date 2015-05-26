package com.mkl.eu.client.service.service.game;

/**
 * Request for updateGame service.
 *
 * @author MKL.
 */
public class MoveStackRequest {
    /** Id of the game. */
    private Long idGame;
    /** Vrsion of the game. */
    private Long versionGame;
    /** Id of the stack to move. */
    private Long idStack;
    /** Province where the stack should move. */
    private String provinceTo;

    /**
     * Constructor for jaxb.
     */
    public MoveStackRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame      the idGame to set.
     * @param versionGame the versionGame to set.
     * @param idStack     the idStack to set.
     * @param provinceTo  the provinceTo to set.
     */
    public MoveStackRequest(Long idGame, Long versionGame, Long idStack, String provinceTo) {
        this.idGame = idGame;
        this.versionGame = versionGame;
        this.idStack = idStack;
        this.provinceTo = provinceTo;
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

    /** @return the idStack. */
    public Long getIdStack() {
        return idStack;
    }

    /** @param idStack the idStack to set. */
    public void setIdStack(Long idStack) {
        this.idStack = idStack;
    }

    /** @return the provinceTo. */
    public String getProvinceTo() {
        return provinceTo;
    }

    /** @param provinceTo the provinceTo to set. */
    public void setProvinceTo(String provinceTo) {
        this.provinceTo = provinceTo;
    }
}
