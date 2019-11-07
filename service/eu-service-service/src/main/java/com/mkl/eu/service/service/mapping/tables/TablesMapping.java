package com.mkl.eu.service.service.mapping.tables;

import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.mapping.WithLossMapping;
import com.mkl.eu.service.service.persistence.oe.tables.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Mapping between VO and OE for the tables.
 *
 * @author MKL.
 */
@Component
public class TablesMapping extends AbstractMapping {
    /** Loss Mapping. */
    @Autowired
    private WithLossMapping withLossMapping;

    /**
     * Fill the trade income tables.
     *
     * @param sources List of trade income entity.
     * @param tables  the target tables.
     */
    public void fillTradeIncomeTables(List<TradeIncomeEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (TradeIncomeEntity source : sources) {
                TradeIncome target = oeToVo(source);
                if (target.isForeignTrade()) {
                    tables.getForeignTrades().add(target);
                } else {
                    tables.getDomesticTrades().add(target);
                }
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public TradeIncome oeToVo(TradeIncomeEntity source) {
        if (source == null) {
            return null;
        }

        TradeIncome target = new TradeIncome();

        target.setId(source.getId());
        target.setCountryValue(source.getCountryValue());
        target.setMinValue(source.getMinValue());
        target.setMaxValue(source.getMaxValue());
        target.setValue(source.getValue());
        target.setForeignTrade(source.isForeignTrade());

        return target;
    }

    /**
     * Fill the technologies tables.
     *
     * @param sources        List of technologies entity.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @param tables         the target tables.
     */
    public void fillTechsTables(List<TechEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated, Tables tables) {
        if (tables != null && sources != null) {
            for (TechEntity source : sources) {
                tables.getTechs().add(storeVo(Tech.class, source, objectsCreated, (ITransformation<TechEntity, Tech>) this::oeToVo));
            }
        }
    }

    /**
     * Fill the basic forces tables.
     *
     * @param sources        List of basic forces entity.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @param tables         the target tables.
     */
    public void fillBasicForcesTables(List<BasicForceTableEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated, Tables tables) {
        if (tables != null && sources != null) {
            for (BasicForceTableEntity source : sources) {
                tables.getBasicForces().add(oeToVo(source, objectsCreated));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public BasicForce oeToVo(BasicForceTableEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        BasicForce target = new BasicForce();

        target.setId(source.getId());
        target.setNumber(source.getNumber());
        target.setType(source.getType());
        target.setCountry(source.getCountry());
        target.setPeriod(storeVo(Period.class, source.getPeriod(), objectsCreated, (ITransformation<PeriodEntity, Period>) this::oeToVo));

        return target;
    }

    /**
     * Fill the periods tables.
     *
     * @param sources        List of periods entity.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @param tables         the target tables.
     */
    public void fillPeriodsTables(List<PeriodEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated, Tables tables) {
        if (tables != null && sources != null) {
            for (PeriodEntity source : sources) {
                Period target = storeVo(Period.class, source, objectsCreated, (ITransformation<PeriodEntity, Period>) this::oeToVo);
                tables.getPeriods().add(target);
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Period oeToVo(PeriodEntity source) {
        if (source == null) {
            return null;
        }

        Period target = new Period();

        target.setId(source.getId());
        target.setName(source.getName());
        target.setBegin(source.getBegin());
        target.setEnd(source.getEnd());

        return target;
    }

    /**
     * Fill the units tables.
     *
     * @param sources        List of units entity.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @param tables         the target tables.
     */
    public void fillUnitsTables(List<UnitEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated, Tables tables) {
        if (tables != null && sources != null) {
            for (UnitEntity source : sources) {
                tables.getUnits().add(oeToVo(source, objectsCreated));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public Unit oeToVo(UnitEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Unit target = new Unit();

        target.setId(source.getId());
        target.setPrice(source.getPrice());
        target.setAction(source.getAction());
        target.setCountry(source.getCountry());
        target.setType(source.getType());
        target.setSpecial(source.isSpecial());
        target.setTech(storeVo(Tech.class, source.getTech(), objectsCreated, (ITransformation<TechEntity, Tech>) this::oeToVo));

        return target;
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Tech oeToVo(TechEntity source) {
        if (source == null) {
            return null;
        }

        Tech target = new Tech();

        target.setId(source.getId());
        target.setName(source.getName());
        target.setCountry(source.getCountry());
        target.setBeginBox(source.getBeginBox());
        target.setBeginTurn(source.getBeginTurn());
        target.setLand(source.isLand());

        return target;
    }

    /**
     * Fill the actions limits tables.
     *
     * @param sources        List of basic forces entity.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @param tables         the target tables.
     */
    public void fillLimitsTables(List<LimitTableEntity> sources, final Map<Class<?>, Map<Long, Object>> objectsCreated, Tables tables) {
        if (tables != null && sources != null) {
            for (LimitTableEntity source : sources) {
                tables.getLimits().add(oeToVo(source, objectsCreated));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source         object source.
     * @param objectsCreated Objects created by the mappings (sort of caching).
     * @return object mapped.
     */
    public Limit oeToVo(LimitTableEntity source, final Map<Class<?>, Map<Long, Object>> objectsCreated) {
        if (source == null) {
            return null;
        }

        Limit target = new Limit();

        target.setId(source.getId());
        target.setNumber(source.getNumber());
        target.setType(source.getType());
        target.setCountry(source.getCountry());
        target.setPeriod(storeVo(Period.class, source.getPeriod(), objectsCreated, (ITransformation<PeriodEntity, Period>) this::oeToVo));

        return target;
    }

    /**
     * Fill the results tables.
     *
     * @param sources List of results entity.
     * @param tables  the target tables.
     */
    public void fillResultsTables(List<ResultEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (ResultEntity source : sources) {
                tables.getResults().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Result oeToVo(ResultEntity source) {
        if (source == null) {
            return null;
        }

        Result target = new Result();

        target.setId(source.getId());
        target.setDie(source.getDie());
        target.setColumn(source.getColumn());
        target.setResult(source.getResult());

        return target;
    }

    /**
     * Fill the battle tech tables.
     *
     * @param sources List of battle tech entity.
     * @param tables  the target tables.
     */
    public void fillBattleTechTables(List<BattleTechEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (BattleTechEntity source : sources) {
                tables.getBattleTechs().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public BattleTech oeToVo(BattleTechEntity source) {
        if (source == null) {
            return null;
        }

        BattleTech target = new BattleTech();

        target.setId(source.getId());
        target.setTechnologyFor(source.getTechnologyFor());
        target.setTechnologyAgainst(source.getTechnologyAgainst());
        target.setLand(source.isLand());
        target.setColumnFire(source.getColumnFire());
        target.setColumnShock(source.getColumnShock());
        target.setMoral(source.getMoral());
        target.setMoralBonusVeteran(source.isMoralBonusVeteran());

        return target;
    }

    /**
     * Fill the combat result tables.
     *
     * @param sources List of combat result entity.
     * @param tables  the target tables.
     */
    public void fillCombatResultTables(List<CombatResultEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (CombatResultEntity source : sources) {
                tables.getCombatResults().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public CombatResult oeToVo(CombatResultEntity source) {
        if (source == null) {
            return null;
        }

        CombatResult target = new CombatResult();

        withLossMapping.oeToVo(source, target);
        target.setId(source.getId());
        target.setColumn(source.getColumn());
        target.setDice(source.getDice());

        return target;
    }

    /**
     * Fill the army classe tables.
     *
     * @param sources List of army class entity.
     * @param tables  the target tables.
     */
    public void fillArmyClasseTables(List<ArmyClasseEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (ArmyClasseEntity source : sources) {
                tables.getArmyClasses().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public ArmyClasse oeToVo(ArmyClasseEntity source) {
        if (source == null) {
            return null;
        }

        ArmyClasse target = new ArmyClasse();

        target.setId(source.getId());
        target.setArmyClass(source.getArmyClass());
        target.setPeriod(source.getPeriod());
        target.setSize(source.getSize());

        return target;
    }

    /**
     * Fill the army artillery tables.
     *
     * @param sources List of army artillery entity.
     * @param tables  the target tables.
     */
    public void fillArmyArtilleryTables(List<ArmyArtilleryEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (ArmyArtilleryEntity source : sources) {
                tables.getArmyArtilleries().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public ArmyArtillery oeToVo(ArmyArtilleryEntity source) {
        if (source == null) {
            return null;
        }

        ArmyArtillery target = new ArmyArtillery();

        target.setId(source.getId());
        target.setCountry(source.getCountry());
        target.setArmyClass(source.getArmyClass());
        target.setPeriod(source.getPeriod());
        target.setArtillery(source.getArtillery());

        return target;
    }

    /**
     * Fill the artillery siege tables.
     *
     * @param sources List of artillery siege entity.
     * @param tables  the target tables.
     */
    public void fillArtillerySiegeTables(List<ArtillerySiegeEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (ArtillerySiegeEntity source : sources) {
                tables.getArtillerySieges().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public ArtillerySiege oeToVo(ArtillerySiegeEntity source) {
        if (source == null) {
            return null;
        }

        ArtillerySiege target = new ArtillerySiege();

        target.setId(source.getId());
        target.setArtillery(source.getArtillery());
        target.setFortress(source.getFortress());
        target.setBonus(source.getBonus());

        return target;
    }

    /**
     * Fill the fortress resistance tables.
     *
     * @param sources List of fortress resistance entity.
     * @param tables  the target tables.
     */
    public void fillFortressResistanceTables(List<FortressResistanceEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (FortressResistanceEntity source : sources) {
                tables.getFortressResistances().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public FortressResistance oeToVo(FortressResistanceEntity source) {
        if (source == null) {
            return null;
        }

        FortressResistance target = new FortressResistance();

        target.setId(source.getId());
        target.setFortress(source.getFortress());
        target.setRound(source.getRound());
        target.setThird(source.getThird());
        target.setBreach(source.isBreach());

        return target;
    }

    /**
     * Fill the assault result tables.
     *
     * @param sources List of assault result entity.
     * @param tables  the target tables.
     */
    public void fillAssaultResultTables(List<AssaultResultEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (AssaultResultEntity source : sources) {
                tables.getAssaultResults().add(oeToVo(source));
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public AssaultResult oeToVo(AssaultResultEntity source) {
        if (source == null) {
            return null;
        }

        AssaultResult target = new AssaultResult();

        withLossMapping.oeToVo(source, target);
        target.setId(source.getId());
        target.setDice(source.getDice());
        target.setFire(source.isFire());
        target.setBreach(source.isBreach());
        target.setBesieger(source.isBesieger());

        return target;
    }

    /**
     * Fill the exchequer tables.
     *
     * @param sources List of entity.
     * @param tables  the target tables.
     */
    public void fillExchequerTables(List<ExchequerEntity> sources, Tables tables) {
        if (tables != null && sources != null) {
            for (ExchequerEntity source : sources) {
                Exchequer target = oeToVo(source);
                tables.getExchequers().add(target);
            }
        }
    }

    /**
     * OE to VO.
     *
     * @param source object source.
     * @return object mapped.
     */
    public Exchequer oeToVo(ExchequerEntity source) {
        if (source == null) {
            return null;
        }

        Exchequer target = new Exchequer();

        target.setId(source.getId());
        target.setResult(source.getResult());
        target.setRegular(source.getRegular());
        target.setPrestige(source.getPrestige());
        target.setNatLoan(source.getNatLoan());
        target.setInterLoan(source.getInterLoan());

        return target;
    }
}
