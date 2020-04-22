package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.tables.HasDice;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility around the tables.
 *
 * @author MKL.
 */
public final class TablesUtil {
    /**
     * Constructor.
     */
    private TablesUtil() {

    }

    /**
     * @param tables the list of table items.
     * @param die    the modified die roll.
     * @param <T>    type of table item.
     * @return the table item that should be returned for this die roll and this type of item.
     */
    public static <T extends HasDice> T getResult(List<T> tables, int die) {
        return getResult(tables, die, null);
    }

    /**
     * @param tables the list of table items.
     * @param die    the modified die roll.
     * @param filter on the table.
     * @param <T>    type of table item.
     * @return the table item that should be returned for this die roll and this type of item.
     */
    public static <T extends HasDice> T getResult(List<T> tables, int die, Predicate<T> filter) {
        if (filter != null) {
            tables = tables.stream()
                    .filter(filter)
                    .collect(Collectors.toList());
        }
        int min = tables.stream()
                .min((o1, o2) -> o1.getDice() - o2.getDice())
                .map(HasDice::getDice)
                .orElse(0);

        int max = tables.stream()
                .max((o1, o2) -> o1.getDice() - o2.getDice())
                .map(HasDice::getDice)
                .orElse(0);

        int realDie = Math.min(max, Math.max(min, die));
        return tables.stream()
                .filter(item -> Objects.equals(item.getDice(), realDie))
                .findAny()
                .orElse(null);
    }

    /**
     * @param third              the size, in exploration detachment, of the stack.
     * @param percentLoss        the percentage of loss suffered by the stack.
     * @param additionalCasualty the function that will tell whether a possible addition casualty is taken.
     * @return the casualties, in exploration detachment, of an attrition computed in percentage (naval or rotw).
     */
    public static int getAttritionOtherCasualtiesInThird(int third, int percentLoss, Supplier<Boolean> additionalCasualty) {
        int loss = third * percentLoss / 100;
        int remain = third * percentLoss - loss * 100;
        if (remain >= 60 || (remain > 20 && remain < 60 && additionalCasualty.get())) {
            loss++;
        }

        return loss;
    }
}
