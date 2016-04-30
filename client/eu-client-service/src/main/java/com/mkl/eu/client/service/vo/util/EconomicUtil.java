package com.mkl.eu.client.service.vo.util;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;

/**
 * Utility for economics/administrative action.
 *
 * @author MKL.
 */
public final class EconomicUtil {

    /**
     * Constructor.
     */
    private EconomicUtil() {

    }

    /**
     * @param type       of the administrative action.
     * @param investment the investment.
     * @return the cost of an administrative action given its type and investment.
     */
    public static Integer getAdminActionCost(AdminActionTypeEnum type, InvestmentEnum investment) {
        Integer cost = null;

        if (type != null && investment != null) {
            switch (type) {
                case TFI:
                case TP:
                case TFC:
                case ERC:
                    switch (investment) {
                        case S:
                            cost = 10;
                            break;
                        case M:
                            cost = 30;
                            break;
                        case L:
                            cost = 50;
                            break;
                        default:
                            break;
                    }
                    break;
                case COL:
                case MNU:
                case FTI:
                case DTI:
                case ELT:
                case ENT:
                    switch (investment) {
                        case S:
                            cost = 30;
                            break;
                        case M:
                            cost = 50;
                            break;
                        case L:
                            cost = 100;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        return cost;
    }

    /**
     * @param type       of the administrative action.
     * @param investment the investment.
     * @return the column bonus of an administrative action given its type and investment.
     */
    public static Integer getAdminActionColumnBonus(AdminActionTypeEnum type, InvestmentEnum investment) {
        Integer bonus = null;

        if (type != null && investment != null) {
            switch (type) {
                case TFI:
                case TP:
                case TFC:
                case ERC:
                case COL:
                case MNU:
                case FTI:
                case DTI:
                case ELT:
                case ENT:
                    switch (investment) {
                        case S:
                            bonus = 0;
                            break;
                        case M:
                            bonus = 1;
                            break;
                        case L:
                            bonus = 3;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        return bonus;
    }
}
