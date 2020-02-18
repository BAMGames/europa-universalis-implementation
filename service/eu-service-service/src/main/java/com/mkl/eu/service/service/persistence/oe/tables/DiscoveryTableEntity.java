package com.mkl.eu.service.service.persistence.oe.tables;

import com.mkl.eu.client.service.vo.enumeration.ResultEnum;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity for the discovery table.
 *
 * @author MKL.
 */
@Entity
@Table(name = "T_DISCOVERY")
public class DiscoveryTableEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Result of the modified dice. */
    private Integer dice;
    /** If it is for land or sea. */
    private boolean land;
    /** If leader should check death. */
    private boolean checkLeader;
    /** If without troops, leader should check death. */
    private boolean checkLeaderNoTroops;
    /** Result of a discovery. */
    private ResultEnum result;

    /** @return the id. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    /** @param id the id to set. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the dice. */
    public Integer getDice() {
        return dice;
    }

    /** @param dice the dice to set. */
    public void setDice(Integer dice) {
        this.dice = dice;
    }

    /** @return the land. */
    public boolean isLand() {
        return land;
    }

    /** @param land the land to set. */
    public void setLand(boolean land) {
        this.land = land;
    }

    /** @return the checkLeader. */
    public boolean isCheckLeader() {
        return checkLeader;
    }

    /** @param checkLeader the checkLeader to set. */
    public void setCheckLeader(boolean checkLeader) {
        this.checkLeader = checkLeader;
    }

    /** @return the checkLeaderNoTroops. */
    public boolean isCheckLeaderNoTroops() {
        return checkLeaderNoTroops;
    }

    /** @param checkLeaderNoTroops the checkLeaderNoTroops to set. */
    public void setCheckLeaderNoTroops(boolean checkLeaderNoTroops) {
        this.checkLeaderNoTroops = checkLeaderNoTroops;
    }

    /** @return the result. */
    @Enumerated(EnumType.STRING)
    public ResultEnum getResult() {
        return result;
    }

    /** @param result the result to set. */
    public void setResult(ResultEnum result) {
        this.result = result;
    }
}
