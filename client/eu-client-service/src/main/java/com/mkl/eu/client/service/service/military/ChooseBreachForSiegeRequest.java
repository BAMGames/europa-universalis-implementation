package com.mkl.eu.client.service.service.military;

/**
 * Request for chooseBreach service.
 *
 * @author MKL.
 */
public class ChooseBreachForSiegeRequest {
    private ChoiceBreachEnum choice;

    /**
     * Constructor for jaxb.
     */
    public ChooseBreachForSiegeRequest() {
    }

    /**
     * Constructor.
     *
     * @param choice the choice to set.
     */
    public ChooseBreachForSiegeRequest(ChoiceBreachEnum choice) {
        this.choice = choice;
    }

    /** @return the choice. */
    public ChoiceBreachEnum getChoice() {
        return choice;
    }

    /** @param choice the choice to set. */
    public void setChoice(ChoiceBreachEnum choice) {
        this.choice = choice;
    }

    public enum ChoiceBreachEnum {
        BREACH,
        WAR_HONORS,
        NOTHING
    }
}
