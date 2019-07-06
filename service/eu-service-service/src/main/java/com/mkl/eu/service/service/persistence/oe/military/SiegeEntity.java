package com.mkl.eu.service.service.persistence.oe.military;

import com.mkl.eu.client.service.vo.enumeration.SiegeStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.SiegeUndermineResultEnum;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.persistence.oe.IEntity;
import com.mkl.eu.service.service.persistence.oe.diplo.WarEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity for a siege.
 *
 * @author MKL.
 */
@Entity
@Table(name = "SIEGE")
public class SiegeEntity implements IEntity, Serializable {
    /** Id. */
    private Long id;
    /** Status. */
    private SiegeStatusEnum status;
    /** Province where the competition occurs. */
    private String province;
    /** Turn of the competition. */
    private Integer turn;
    /** Level of the besieged fortress. */
    private int fortressLevel;
    /** If there was a breach in this siege previously. */
    private boolean breach;
    /** Bonus for undermining. */
    private int bonus;
    /** If mode is UNDERMINE, the undermine unmodified die. */
    private int undermineDie;
    /** If mode is UNDERMINE, the undermine result (in case of multiple results, the one selected by the besieger). */
    private SiegeUndermineResultEnum undermineResult;
    /** Flag saying that the fortress has fallen to the besieger. */
    private boolean fortressFalls;
    /** Phasing side for a possible assault. */
    private SiegeSideEntity phasing = new SiegeSideEntity();
    /** Non phasing side for a possible assault. */
    private SiegeSideEntity nonPhasing = new SiegeSideEntity();
    /** Counters involved in the battle. */
    private Set<SiegeCounterEntity> counters = new HashSet<>();
    /** War in which the siege occurs. */
    private WarEntity war;
    /** If the besieging side of the siege is the offensive side of the war. */
    private boolean besiegingOffensive;
    /** Game in which the competition occurs. */
    private GameEntity game;

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

    /** @return the status. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    public SiegeStatusEnum getStatus() {
        return status;
    }

    /** @param status the status to set. */
    public void setStatus(SiegeStatusEnum status) {
        this.status = status;
    }

    /** @return the province. */
    @Column(name = "R_PROVINCE")
    public String getProvince() {
        return province;
    }

    /** @param province the province to set. */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return the turn. */
    @Column(name = "TURN")
    public Integer getTurn() {
        return turn;
    }

    /** @param turn the turn to set. */
    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    /** @return the fortressLevel. */
    public int getFortressLevel() {
        return fortressLevel;
    }

    /** @param fortressLevel the fortressLevel to set. */
    public void setFortressLevel(int fortressLevel) {
        this.fortressLevel = fortressLevel;
    }

    /** @return the breach. */
    @Column(name = "BREACH")
    public boolean isBreach() {
        return breach;
    }

    /** @param breach the breach to set. */
    public void setBreach(boolean breach) {
        this.breach = breach;
    }

    /** @return the bonus. */
    @Column(name = "BONUS")
    public int getBonus() {
        return bonus;
    }

    /** @param bonus the bonus to set. */
    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    /** @return the undermineDie. */
    public int getUndermineDie() {
        return undermineDie;
    }

    /** @param undermineDie the undermineDie to set. */
    public void setUndermineDie(int undermineDie) {
        this.undermineDie = undermineDie;
    }

    /** @return the undermineResult. */
    @Enumerated(EnumType.STRING)
    public SiegeUndermineResultEnum getUndermineResult() {
        return undermineResult;
    }

    /** @param undermineResult the undermineResult to set. */
    public void setUndermineResult(SiegeUndermineResultEnum undermineResult) {
        this.undermineResult = undermineResult;
    }

    /** @return the fortressFalls. */
    public boolean isFortressFalls() {
        return fortressFalls;
    }

    /** @param fortressFalls the fortressFalls to set. */
    public void setFortressFalls(boolean fortressFalls) {
        this.fortressFalls = fortressFalls;
    }

    /** @return the phasing. */
    public SiegeSideEntity getPhasing() {
        return phasing;
    }

    /** @param phasing the phasing to set. */
    public void setPhasing(SiegeSideEntity phasing) {
        this.phasing = phasing;
    }

    /** @return the nonPhasing. */
    public SiegeSideEntity getNonPhasing() {
        return nonPhasing;
    }

    /** @param nonPhasing the nonPhasing to set. */
    public void setNonPhasing(SiegeSideEntity nonPhasing) {
        this.nonPhasing = nonPhasing;
    }

    /** @return the counters. */
    @OneToMany(mappedBy = "id.siege", cascade = CascadeType.ALL)
    public Set<SiegeCounterEntity> getCounters() {
        return counters;
    }

    /** @param counters the counters to set. */
    public void setCounters(Set<SiegeCounterEntity> counters) {
        this.counters = counters;
    }

    /** @return the war. */
    @ManyToOne
    @JoinColumn(name = "ID_WAR")
    public WarEntity getWar() {
        return war;
    }

    /** @param war the war to set. */
    public void setWar(WarEntity war) {
        this.war = war;
    }

    /** @return the besiegingOffensive. */
    public boolean isBesiegingOffensive() {
        return besiegingOffensive;
    }

    /** @param besiegingOffensive the besiegingOffensive to set. */
    public void setBesiegingOffensive(boolean besiegingOffensive) {
        this.besiegingOffensive = besiegingOffensive;
    }

    /** @return the game. */
    @ManyToOne
    @JoinColumn(name = "ID_GAME")
    public GameEntity getGame() {
        return game;
    }

    /** @param game the game to set. */
    public void setGame(GameEntity game) {
        this.game = game;
    }
}
