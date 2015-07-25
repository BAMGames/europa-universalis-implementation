package com.mkl.eu.front.client.event;

import com.mkl.eu.client.service.vo.diff.DiffResponse;

/**
 * Event when a game is being updated.
 *
 * @author MKL.
 */
public class DiffEvent {
    /** Diffs to spread to the client. */
    private DiffResponse response;
    /** Id of the game (to be sure the correct game is being updated). */
    private Long idGame;

    /**
     * Constructor.
     *
     * @param response      the response.
     * @param idGame     the id of the game.
     */
    public DiffEvent(DiffResponse response, Long idGame) {
        this.response = response;
        this.idGame = idGame;
    }

    /** @return the response. */
    public DiffResponse getResponse() {
        return response;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }
}
