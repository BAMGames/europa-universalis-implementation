package com.mkl.eu.client.service.service.test;

import com.mkl.eu.client.service.vo.diff.Diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for testDiff service.
 *
 * @author MKL.
 */
public class TestDiffRequest {
    List<Diff> diffs = new ArrayList<>();

    /**
     * Constructor for jaxb.
     */
    public TestDiffRequest() {

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
