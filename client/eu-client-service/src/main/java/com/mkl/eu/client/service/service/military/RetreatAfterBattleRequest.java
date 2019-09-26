package com.mkl.eu.client.service.service.military;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for retreatAfterBattle service.
 *
 * @author MKL.
 */
public class RetreatAfterBattleRequest {
    /** List of counter ids that will retreat in fortress. */
    private List<Long> retreatInFortress = new ArrayList<>();
    /** Province where the remaining forces will retreat. Can be <code>null</code> or empty if every forces are already in fortress. */
    private String provinceTo;
    /** Flag saying that the unit remaining should be disbanded. */
    private boolean disbandRemaining;

    /**
     * Constructor for jaxb.
     */
    public RetreatAfterBattleRequest() {
    }

    /**
     * Constructor.
     *
     * @param retreatInFortress the retreatInFortress.
     * @param provinceTo        the provinceTo.
     * @param disbandRemaining  the disbandRemaining.
     */
    public RetreatAfterBattleRequest(List<Long> retreatInFortress, String provinceTo, boolean disbandRemaining) {
        this.retreatInFortress = retreatInFortress;
        this.provinceTo = provinceTo;
        this.disbandRemaining = disbandRemaining;
    }

    /** @return the retreatInFortress. */
    public List<Long> getRetreatInFortress() {
        return retreatInFortress;
    }

    /** @param retreatInFortress the retreatInFortress to set. */
    public void setRetreatInFortress(List<Long> retreatInFortress) {
        this.retreatInFortress = retreatInFortress;
    }

    /** @return the provinceTo. */
    public String getProvinceTo() {
        return provinceTo;
    }

    /** @param provinceTo the provinceTo to set. */
    public void setProvinceTo(String provinceTo) {
        this.provinceTo = provinceTo;
    }

    /** @return the disbandRemaining. */
    public boolean isDisbandRemaining() {
        return disbandRemaining;
    }

    /** @param disbandRemaining the disbandRemaining to set. */
    public void setDisbandRemaining(boolean disbandRemaining) {
        this.disbandRemaining = disbandRemaining;
    }
}
