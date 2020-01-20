package com.mkl.eu.service.service.service.impl;

import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.service.service.persistence.oe.GameEntity;
import com.mkl.eu.service.service.util.IOEUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract service for military services (battle, sieges,...).
 *
 * @author MKL.
 */
public class AbstractMilitaryService extends AbstractService {
    /** OeUtil. */
    @Autowired
    protected IOEUtil oeUtil;

    /**
     * Roll for a replacement leader.
     *
     * @param countryName name of the country for which we need a replacement leader.
     * @param game        the game.
     */
    protected String getReplacementLeader(String countryName, GameEntity game) {
        int die = oeUtil.rollDie(game);
        CountryReferential country = getReferential().getCountry(countryName);
        String leaderCountry;
        if (country != null) {
            if (country.getType() == CountryTypeEnum.MAJOR || country.getType() == CountryTypeEnum.MINORMAJOR) {
                leaderCountry = country.getName();
            } else {
                leaderCountry = Leader.REPLACEMENT_MINOR;
            }
        } else {
            leaderCountry = Leader.REPLACEMENT_NATIVES;
        }

        // TODO TG-10 admiral for naval battle
        String code = leaderCountry + "-general-" + die;
        Leader leader = getTables().getLeader(code, countryName);
        return leader.getCode();
    }
}
