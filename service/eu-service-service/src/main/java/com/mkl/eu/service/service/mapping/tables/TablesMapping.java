package com.mkl.eu.service.service.mapping.tables;

import com.mkl.eu.client.service.vo.tables.*;
import com.mkl.eu.service.service.mapping.AbstractMapping;
import com.mkl.eu.service.service.persistence.oe.tables.*;
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
}
