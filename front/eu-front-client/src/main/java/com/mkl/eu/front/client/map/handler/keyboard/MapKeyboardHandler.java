package com.mkl.eu.front.client.map.handler.keyboard;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IServiceCaller;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.map.MapConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MKL
 */
public class MapKeyboardHandler extends KeyboardHandler implements IServiceCaller {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MapKeyboardHandler.class);
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();
    /** Board service. */
    private IGameService gameService;
    /** Component holding the authentication information. */
    private AuthentHolder authentHolder;
    /** Game configuration. */
    private GameConfiguration gameConfig;

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps One or more maps.
     */
    public MapKeyboardHandler(PApplet p, IGameService gameService, AuthentHolder authentHolder, GameConfiguration gameConfig, UnfoldingMap... maps) {
        super(p, maps);
        this.gameService = gameService;
        this.authentHolder = authentHolder;
        this.gameConfig = gameConfig;
    }

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps A list of maps.
     */
    public MapKeyboardHandler(PApplet p, List<UnfoldingMap> maps) {
        super(p, maps);
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        super.keyPressed(key, keyCode);

        if (keyCode == 67) {
            // 'c' key
            MapConfiguration.switchColor();
        } else if (keyCode == 65) {
            // 'a' key
            for (UnfoldingMap map : maps) {
                map.setTweening(!map.isTweening());
            }
        } else if (keyCode == 85) {
            // 'u' key
            callService(gameService::updateGame, () -> null, "Error when updating game.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public GameConfiguration getGameConfig() {
        return gameConfig;
    }

    /** {@inheritDoc} */
    @Override
    public AuthentHolder getAuthentHolder() {
        return authentHolder;
    }

    /** {@inheritDoc} */
    @Override
    public void addDiffListener(IDiffListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processDiffEvent(DiffEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processExceptionEvent(ExceptionEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.handleException(event);
        }
    }
}
