package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.vo.Game;

/**
 * Interfaces for component wishing to display contextual menu.
 *
 * @author MKL.
 */
public interface IMenuContainer extends INotJavaFxServiceCaller {
    /**
     * @return the boardService.
     */
    IBoardService getBoardService();

    /**
     * @return the battleService.
     */
    IBattleService getBattleService();

    /**
     * @return the siegeService.
     */
    ISiegeService getSiegeService();

    /**
     * @return the interPhaseService.
     */
    IInterPhaseService getInterPhaseService();

    /**
     * @return the game for displaying context actions.
     */
    Game getGame();
}
