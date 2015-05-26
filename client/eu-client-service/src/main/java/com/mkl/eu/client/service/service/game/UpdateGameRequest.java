package com.mkl.eu.client.service.service.game;

/**
 * Request for updateGame service.
 *
 * @author MKL.
 */
public class UpdateGameRequest {
    /** Id of the game to update. */
    private Long idGame;
    /** Previous version of the game to update. */
    private Long versionGame;

    /**
     * Constructor for jaxb.
     */
    public UpdateGameRequest() {
    }

    /**
     * Constructor.
     *
     * @param idGame      the idGame to set.
     * @param versionGame the versionGame to set.
     */
    public UpdateGameRequest(Long idGame, Long versionGame) {
        this.idGame = idGame;
        this.versionGame = versionGame;
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
}
