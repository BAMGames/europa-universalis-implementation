package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
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

    /**
     * @param type of the counter to test.
     * @return <code>true</code> if the counter face type is the one of a manufacture, <code>false</code> otherwise.
     */
    public static boolean isManufacture(CounterFaceTypeEnum type) {
        boolean mnu = false;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_ART_PLUS:
                case MNU_CEREALS_MINUS:
                case MNU_CEREALS_PLUS:
                case MNU_CLOTHES_MINUS:
                case MNU_CLOTHES_PLUS:
                case MNU_FISH_MINUS:
                case MNU_FISH_PLUS:
                case MNU_INSTRUMENTS_MINUS:
                case MNU_INSTRUMENTS_PLUS:
                case MNU_METAL_MINUS:
                case MNU_METAL_PLUS:
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                case MNU_SALT_MINUS:
                case MNU_SALT_PLUS:
                case MNU_WINE_MINUS:
                case MNU_WINE_PLUS:
                case MNU_WOOD_MINUS:
                case MNU_WOOD_PLUS:
                    mnu = true;
                    break;
                default:
                    break;
            }
        }

        return mnu;
    }

    /**
     * @param type of the counter to test.
     * @return the level of the manufacture (0 if the type is not a manufacture).
     */
    public static int getManufactureLevel(CounterFaceTypeEnum type) {
        int level = 0;

        if (type != null) {
            switch (type) {
                case MNU_ART_MINUS:
                case MNU_CEREALS_MINUS:
                case MNU_CLOTHES_MINUS:
                case MNU_FISH_MINUS:
                case MNU_INSTRUMENTS_MINUS:
                case MNU_METAL_MINUS:
                case MNU_METAL_SCHLESIEN_MINUS:
                case MNU_SALT_MINUS:
                case MNU_WINE_MINUS:
                case MNU_WOOD_MINUS:
                    level = 1;
                    break;
                case MNU_ART_PLUS:
                case MNU_CEREALS_PLUS:
                case MNU_CLOTHES_PLUS:
                case MNU_FISH_PLUS:
                case MNU_INSTRUMENTS_PLUS:
                case MNU_METAL_PLUS:
                case MNU_METAL_SCHLESIEN_PLUS:
                case MNU_SALT_PLUS:
                case MNU_WINE_PLUS:
                case MNU_WOOD_PLUS:
                    level = 2;
                    break;
                default:
                    break;
            }
        }

        return level;
    }
}
