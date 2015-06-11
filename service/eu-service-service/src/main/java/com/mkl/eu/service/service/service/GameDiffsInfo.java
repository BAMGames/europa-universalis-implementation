package com.mkl.eu.service.service.service;

import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.diff.DiffEntity;

import java.util.List;

/**
 * Holder of a GameEntity and its DiffEntity.
 *
 * @author MKL.
 */
public class GameDiffsInfo {
    /** Game. */
    private GameEntity game;
    /** List of diffs of the game. */
    private List<DiffEntity> diffs;

    /**
     * Constructor.
     *
     * @param game  the game to set.
     * @param diffs the diffs to set.
     */
    public GameDiffsInfo(GameEntity game, List<DiffEntity> diffs) {
        this.game = game;
        this.diffs = diffs;
    }

    /** @return the game. */
    public GameEntity getGame() {
        return game;
    }

    /** @return the diffs. */
    public List<DiffEntity> getDiffs() {
        return diffs;
    }
}
