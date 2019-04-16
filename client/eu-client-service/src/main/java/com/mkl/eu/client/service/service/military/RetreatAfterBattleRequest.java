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

    /**
     * Constructor for jaxb.
     */
    public RetreatAfterBattleRequest() {
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
}
