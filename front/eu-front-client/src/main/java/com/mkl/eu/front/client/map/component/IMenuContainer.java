package com.mkl.eu.front.client.map.component;

import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import org.springframework.context.MessageSource;

/**
 * Interfaces for component wishing to display contextual menu.
 *
 * @author MKL.
 */
public interface IMenuContainer extends IDiffListenerContainer {
    /**
     * @return a MessageSource for internationalization.
     */
    MessageSource getMessage();

    /**
     * @return a GlobalConfiguration for configuration not related to a specific game.
     */
    GlobalConfiguration getGlobalConfiguration();

    /**
     * @return a GameConfiguration for configuration related to a game.
     */
    GameConfiguration getGameConfig();

    /**
     * @return a AuthentHolder for info about authentication.
     */
    AuthentHolder getAuthentHolder();
}
