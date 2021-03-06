package com.mkl.eu.client.service.util;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.enumeration.InvestmentEnum;
import com.mkl.eu.client.service.vo.enumeration.WarImplicationEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility around the game.
 *
 * @author MKL.
 */
public final class GameUtil {
    public static final String ROUND_END = "B_MR_End";

    /**
     * Constructor.
     */
    private GameUtil() {

    }

    /**
     * Get the stability from the box (province) where the counter is.
     *
     * @param provinceBox name of the province/box where the stability counter is.
     * @return the stability.
     */
    public static Integer getStability(String provinceBox) {
        Integer stab = null;
        if (!StringUtils.isEmpty(provinceBox)) {
            Matcher matcher = Pattern.compile("B_STAB_(-?\\d)").matcher(provinceBox);
            if (matcher.matches()) {
                stab = Integer.parseInt(matcher.group(1));
            }
        }

        return stab;
    }

    /**
     * Filter to use to filter the provinces which can hold stability counters.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a stability counter, <code>false</code> otherwise.
     */
    public static boolean isStabilityBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_STAB_");
    }

    /**
     * Get the stability box for a given stab.
     *
     * @param stab actual stability.
     * @return the stability box.
     */
    public static String getStabilityBox(int stab) {
        return "B_STAB_" + stab;
    }

    /**
     * Get the technology advance from the box (province) where the counter is.
     *
     * @param techBox name of the province/box where the technology counter is.
     * @return the technology advance.
     */
    public static Integer getTechnology(String techBox) {
        Integer tech = null;
        if (!StringUtils.isEmpty(techBox)) {
            Matcher matcher = Pattern.compile("B_TECH_(\\d*)").matcher(techBox);
            if (matcher.matches()) {
                tech = Integer.parseInt(matcher.group(1));
            }
        }

        return tech;
    }

    /**
     * Filter to use to filter the provinces which can hold technology counters.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a technology counter, <code>false</code> otherwise.
     */
    public static boolean isTechnologyBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_TECH_");
    }

    /**
     * Get the technology box for a given advance.
     *
     * @param tech the technology advance.
     * @return the technology box.
     */
    public static String getTechnologyBox(int tech) {
        return "B_TECH_" + tech;
    }

    /**
     * Get the inflation from the box (province) where the counter is.
     *
     * @param provinceBox name of the province/box where the inflation counter is.
     * @param exploitGold flag saying that we want the inflation for those exploiting gold in America or not.
     * @return the inflation.
     */
    public static Integer getInflation(String provinceBox, boolean exploitGold) {
        int internalNumber = inflationBoxToNumber(provinceBox);
        if (!exploitGold) {
            internalNumber--;
        }

        Integer inflation;
        switch (internalNumber) {
            case 10:
                inflation = 33;
                break;
            case 9:
                inflation = 25;
                break;
            case 8:
            case 7:
                inflation = 20;
                break;
            case 6:
            case 5:
            case 4:
                inflation = 10;
                break;
            case 3:
            case 2:
            case 1:
            case 0:
                inflation = 5;
                break;
            default:
                inflation = 0;
                break;
        }

        return inflation;
    }

    /**
     * Transform the province box where the inflation counter is into an abstract number representing the inflation.
     *
     * @param provinceBox the province box where the inflation counter is.
     * @return abstract representation of inflation box.
     */
    public static int inflationBoxToNumber(String provinceBox) {
        int internalNumber = -1;
        if (!StringUtils.isEmpty(provinceBox)) {
            Matcher matcher = Pattern.compile("B_PB_(\\d)([DG])").matcher(provinceBox);
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                boolean right = StringUtils.equals("D", matcher.group(2));
                internalNumber = 2 * number + 1;
                if (right) {
                    internalNumber++;
                }
            }
        }

        return internalNumber;
    }

    /**
     * Transforms an abstract number representing the inflation into the province box where the inflation counter is.
     *
     * @param internalNumber the abstract representation of inflation box.
     * @return the province box where the inflation counter is.
     */
    public static String inflationBoxFromNumber(int internalNumber) {
        StringBuilder sb = new StringBuilder("B_PB_");
        sb.append((internalNumber - 1) / 2);
        if ((internalNumber - 1) % 2 == 1) {
            sb.append("D");
        } else {
            sb.append("G");
        }

        return sb.toString();
    }

    /**
     * @param provinceBox the province box where the inflation counter is.
     * @return if the inflation is at its max position.
     */
    public static boolean isInflationMax(String provinceBox) {
        return StringUtils.equals("B_PB_4D", provinceBox);
    }

    /**
     * @param provinceBox the province box where the inflation counter is.
     * @return if the inflation is at its max position.
     */
    public static boolean isInflationMin(String provinceBox) {
        return StringUtils.equals("B_PB_0G", provinceBox);
    }

    /**
     * Filter to use to filter the provinces which can hold inflation counter.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a inflation counter, <code>false</code> otherwise.
     */
    public static boolean isInflationBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_PB_");
    }

    /**
     * Filter to use to filter the provinces which can hold round counter.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a round counter, <code>false</code> otherwise.
     */
    public static boolean isRoundBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_MR_");
    }

    /**
     * Method to use to know whether it is summer or winter.
     *
     * @param provinceBox name of the province/box where the round marker is.
     * @return <code>true</code> if the round is a winter round, <code>false</code> otherwise.
     */
    public static boolean isWinterRoundBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_MR_W");
    }

    /**
     * @param provinceBox name of the province/box where the round marker is.
     * @return the number of the military round.
     */
    public static int getRoundBox(String provinceBox) {
        int round = -1;

        if (isRoundBox(provinceBox)) {
            Matcher m = Pattern.compile("B_MR_[W|S](\\d)").matcher(provinceBox);
            if (m.matches()) {
                round = Integer.parseInt(m.group(1));
            }
        }

        return round;
    }

    /**
     * Comparator for round box provinces.
     *
     * @param roundBox1 the first round box.
     * @param roundBox2 the second round box.
     * @return a positive value if first province is temporally after second province, negative if it is before, and 0 if it is the same province.
     */
    public static int compareRoundBoxes(String roundBox1, String roundBox2) {
        int round1 = getRoundBox(roundBox1);
        int round2 = getRoundBox(roundBox2);
        int diff = round1 - round2;
        if (round1 == -1 || round2 == -1) {
            return -diff;
        }
        if (diff == 0) {
            boolean isWinter1 = isWinterRoundBox(roundBox1);
            boolean isWinter2 = isWinterRoundBox(roundBox2);
            diff = (isWinter1 == isWinter2 ? 0 : isWinter1 ? 1 : -1);
        }
        return diff;
    }

    /**
     * @param provinceBox the initial round box.
     * @param add         the number of round to add.
     * @return the round box that is the result of the provinceBox given and a number of round to add.
     */
    public static String getRoundBoxAdd(String provinceBox, int add) {
        int round = getRoundBox(provinceBox);
        boolean winter = isWinterRoundBox(provinceBox);
        int remain = add % 2;
        round += add / 2 + ((winter && remain == 1) ? 1 : 0);
        winter = winter && (remain == 0) || !winter && (remain == 1);
        if (round >= 6) {
            return ROUND_END;
        } else {
            return "B_MR_" + (winter ? "W" : "S") + round;
        }
    }

    /**
     * @param provinceBox the province where the round is.
     * @return wether the round box is the last one.
     */
    public static boolean isLastRound(String provinceBox) {
        return StringUtils.equals(provinceBox, ROUND_END);
    }

    /**
     * Filter to use to filter the provinces which can hold turn counter.
     *
     * @param provinceBox name of the province/box.
     * @return <code>true</code> if the province can hold a turn counter, <code>false</code> otherwise.
     */
    public static boolean isTurnBox(String provinceBox) {
        return !StringUtils.isEmpty(provinceBox) && provinceBox.startsWith("B_Turn_");
    }

    /**
     * @param turn of the game.
     * @return the province where the turn counter should be at the given turn.
     */
    public static String getTurnBox(int turn) {
        return "B_Turn_" + Math.max(Math.min(turn, 62), 1);
    }

    /**
     * @param province to test.
     * @return <code>true</code> if the province is in the rotw, <code>false</code> otherwise. Does not work for sea provinces.
     */
    public static boolean isRotwProvince(String province) {
        return StringUtils.isNotEmpty(province) && province.startsWith("r");
    }

    /**
     * @param game the game.
     * @return the List of PlayableCountry whose it is the turn.
     */
    public static List<PlayableCountry> getActivePlayers(Game game) {
        List<PlayableCountry> countries;

        switch (game.getStatus()) {
            case ADMINISTRATIVE_ACTIONS_CHOICE:
            case MILITARY_HIERARCHY:
            case EXCHEQUER:
                countries = game.getCountries().stream()
                        .filter(country -> StringUtils.isNotEmpty(country.getUsername()) &&
                                !country.isReady())
                        .collect(Collectors.toList());
                break;
            case MILITARY_CAMPAIGN:
            case MILITARY_SUPPLY:
            case MILITARY_MOVE:
            case MILITARY_BATTLES:
            case MILITARY_SIEGES:
            case MILITARY_NEUTRALS:
                countries = game.getOrders().stream()
                        .filter(CountryOrder::isActive)
                        .map(CountryOrder::getCountry)
                        .collect(Collectors.toList());
                break;
            default:
                countries = new ArrayList<>();
        }

        return countries;
    }

    /**
     * Tells if a specific country is fully at war in a specific game.
     *
     * @param countryName name of the country.
     * @param game        the game.
     * @return if the country is at war.
     */
    public static boolean isAtWar(String countryName, Game game) {
        return game.getWars().stream()
                .flatMap(war -> war.getCountries().stream())
                .anyMatch(country -> country.getImplication() == WarImplicationEnum.FULL && StringUtils.equals(countryName, country.getCountry().getName()));
    }

    /**
     * Table for improve stability action.
     *
     * @param modifiedDie the modified die roll.
     * @return the stability improve.
     */
    public static int improveStability(int modifiedDie) {
        int stabDiff = 0;
        if (modifiedDie <= 5) {
            stabDiff = -1;
        } else if (modifiedDie >= 18) {
            stabDiff = 3;
        } else if (modifiedDie >= 15) {
            stabDiff = 2;
        } else if (modifiedDie >= 11) {
            stabDiff = 1;
        }
        return stabDiff;
    }

    /**
     * @param cost of the action.
     * @return the investment of a domestic, technology or improve stability action given its price.
     */
    public static InvestmentEnum reverseInvestment(Integer cost) {
        if (cost == null) {
            return null;
        } else if (cost == 30) {
            return InvestmentEnum.S;
        } else if (cost == 50) {
            return InvestmentEnum.M;
        } else if (cost == 100) {
            return InvestmentEnum.L;
        } else {
            return null;
        }
    }
}
