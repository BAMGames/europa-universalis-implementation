package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.front.client.main.GlobalConfiguration;

/**
 * Interfaces for component wishing to display contextual menu.
 *
 * @author MKL.
 */
public interface IMenuContainer extends INotJavaFxServiceCaller {
    /**
     * @return a GlobalConfiguration for configuration not related to a specific game.
     */
    GlobalConfiguration getGlobalConfiguration();

    IBoardService getBoardService();

    IBattleService getBattleService();

    ISiegeService getSiegeService();

    IInterPhaseService getInterPhaseService();

    /**
     * @return the game for displaying context actions.
     */
    Game getGame();
}
