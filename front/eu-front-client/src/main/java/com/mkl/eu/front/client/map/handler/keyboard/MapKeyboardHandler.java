package com.mkl.eu.front.client.map.handler.keyboard;

import com.mkl.eu.front.client.map.MapConfiguration;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import processing.core.PApplet;

import java.util.List;

/**
 * @author MKL
 */
public class MapKeyboardHandler extends KeyboardHandler {

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps One or more maps.
     */
    public MapKeyboardHandler(PApplet p, UnfoldingMap... maps) {
        super(p, maps);
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
        }
    }
}
