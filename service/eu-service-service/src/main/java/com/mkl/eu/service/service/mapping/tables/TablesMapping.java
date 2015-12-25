package com.mkl.eu.service.service.mapping.tables;

import com.mkl.eu.client.service.vo.tables.Tables;
import com.mkl.eu.client.service.vo.tables.TradeIncome;
import com.mkl.eu.service.service.persistence.oe.tables.TradeIncomeEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapping between VO and OE for the tables.
 *
 * @author MKL.
 */
@Component
public class TablesMapping {

    /**
     * Fill the trade income trade tables.
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
}
