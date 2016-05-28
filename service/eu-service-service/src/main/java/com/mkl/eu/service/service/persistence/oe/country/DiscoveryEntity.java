package com.mkl.eu.service.service.persistence.oe.country;

import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.board.StackEntity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entoty for a discovery of a province of the ROTW.
 *
 * @author MKL
 */
@Entity
@Table(name = "DISCOVERY")
public class DiscoveryEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Country the discovery belongs to. */
    private PlayableCountryEntity country;
    /** Province of the discovery. */
    private String province;
    /** Stack where the discovery is if being repatriated. */
    private StackEntity stack;
    /** Turn it was repatriated in a national province (<code>null</code> if on going). */
    private Integer turn;

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the country.
     */
    @ManyToOne
    @JoinColumn(name = "ID_COUNTRY", nullable = true)
    public PlayableCountryEntity getCountry() {
        return country;
    }

    /**
     * @param country the country to set.
     */
    public void setCountry(PlayableCountryEntity country) {
        this.country = country;
    }

    /**
     * @return the province.
     */
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /**
     * @param province the province to set.
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * @return the stack.
     */
    @ManyToOne
    @JoinColumn(name = "ID_STACK", nullable = true)
    public StackEntity getStack() {
        return stack;
    }

    /**
     * @param stack the stack to set.
     */
    public void setStack(StackEntity stack) {
        this.stack = stack;
    }

    /**
     * @return the turn.
     */
    @Column(name = "TURN")
    public Integer getTurn() {
        return turn;
    }

    /**
     * @param turn the turn to set.
     */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }
}
