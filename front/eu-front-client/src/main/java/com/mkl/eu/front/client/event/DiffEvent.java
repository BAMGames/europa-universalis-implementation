package com.mkl.eu.front.client.event;

import com.mkl.eu.client.service.vo.diff.Diff;

import java.util.List;

/**
 * Event when a game is being updated.
 *
 * @author MKL.
 */
public class DiffEvent {
    /** Diffs to spread to the client. */
    private List<Diff> diffs;
    /** Id of the game (to be sure the correct game is being updated). */
    private Long idGame;
    /** New version of the game. */
    private Long newVersion;

    /**
     * Constructor.
     *
     * @param diffs      the diffs.
     * @param idGame     the id of the game.
     * @param newVersion the new version of the game.
     */
    public DiffEvent(List<Diff> diffs, Long idGame, Long newVersion) {
        this.diffs = diffs;
        this.idGame = idGame;
        this.newVersion = newVersion;
    }

    /** @return the diffs. */
    public List<Diff> getDiffs() {
        return diffs;
    }

    /** @return the idGame. */
    public Long getIdGame() {
        return idGame;
    }

    /** @return the newVersion. */
    public Long getNewVersion() {
        return newVersion;
    }
}
