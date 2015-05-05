package com.mkl.eu.front.client.map.handler.keyboard;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.map.MapConfiguration;
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
public class MapKeyboardHandler extends KeyboardHandler implements IDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MapKeyboardHandler.class);
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();
    /** Game service. */
    private IGameService gameService;

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps One or more maps.
     */
    public MapKeyboardHandler(PApplet p, IGameService gameService, UnfoldingMap... maps) {
        super(p, maps);
        this.gameService = gameService;
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
            MapConfiguration.switchColor();
        } else if (keyCode == 65) {
            for (UnfoldingMap map : maps) {
                map.setTweening(!map.isTweening());
            }
        } else if (keyCode == 85) {
            Long idGame = MapConfiguration.getIdGame();
            try {
                DiffResponse response = gameService.updateGame(idGame, MapConfiguration.getVersionGame());
                DiffEvent diff = new DiffEvent(response.getDiffs(), idGame, response.getVersionGame());
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when updating game.", e);
                // TODO exception handling
            }
        }
    }

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    public void addDiffListener(IDiffListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /**
     * Process a DiffEvent.
     *
     * @param event to process.
     */
    private void processDiffEvent(DiffEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }
}
