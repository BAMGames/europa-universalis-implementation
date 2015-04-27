package com.mkl.eu.client.service.vo.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Default response of services that involve modifications.
 *
 * @author MKL.
 */
public class DiffResponse {
    /** New version of the game. */
    private Long versionGame;
    /** Diff involved in the modification. */
    private List<Diff> diffs = new ArrayList<>();

    /** @return the versionGame. */
    public Long getVersionGame() {
        return versionGame;
    }

    /** @param versionGame the versionGame to set. */
    public void setVersionGame(Long versionGame) {
        this.versionGame = versionGame;
    }

    /** @return the diffs. */
    public List<Diff> getDiffs() {
        return diffs;
    }

    /** @param diffs the diffs to set. */
    public void setDiffs(List<Diff> diffs) {
        this.diffs = diffs;
    }
}
