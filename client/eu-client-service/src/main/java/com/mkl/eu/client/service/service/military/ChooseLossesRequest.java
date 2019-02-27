package com.mkl.eu.client.service.service.military;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for chooseLosses service.
 *
 * @author MKL.
 */
public class ChooseLossesRequest {
    /** Losses chosen. */
    private List<UnitLoss> losses = new ArrayList<>();

    /**
     * Constructor for jaxb.
     */
    public ChooseLossesRequest() {
    }

    /** @return the losses. */
    public List<UnitLoss> getLosses() {
        return losses;
    }

    /** @param losses the losses to set. */
    public void setLosses(List<UnitLoss> losses) {
        this.losses = losses;
    }

    /**
     * Class for losses taken by a single unit (or counter).
     */
    public static class UnitLoss {
        /** Id of the counter that will take this loss. */
        private Long idCounter;
        /** Number of regiment that will be destroyed in this unit. */
        private int roundLosses;
        /** Number of third of regiment that will be destroyed in this unit. */
        private int thirdLosses;

        /** @return the idCounter. */
        public Long getIdCounter() {
            return idCounter;
        }

        /** @param idCounter the idCounter to set. */
        public void setIdCounter(Long idCounter) {
            this.idCounter = idCounter;
        }

        /** @return the roundLosses. */
        public int getRoundLosses() {
            return roundLosses;
        }

        /** @param roundLosses the roundLosses to set. */
        public void setRoundLosses(int roundLosses) {
            this.roundLosses = roundLosses;
        }

        /** @return the thirdLosses. */
        public int getThirdLosses() {
            return thirdLosses;
        }

        /** @param thirdLosses the thirdLosses to set. */
        public void setThirdLosses(int thirdLosses) {
            this.thirdLosses = thirdLosses;
        }
    }
}
