package com.mkl.eu.front.client.map.component;

import com.mkl.eu.front.client.event.IServiceCaller;
import com.mkl.eu.front.client.main.GlobalConfiguration;

/**
 * Interfaces for component wishing to display contextual menu.
 *
 * @author MKL.
 */
public interface IMenuContainer extends IServiceCaller {
    /**
     * @return a GlobalConfiguration for configuration not related to a specific game.
     */
    GlobalConfiguration getGlobalConfiguration();
}
