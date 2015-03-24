package com.mkl.eu.front.map.handler;

import com.mkl.eu.front.main.Mine;
import com.mkl.eu.front.map.MapConfiguration;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;

import java.util.List;

/**
 * @author MKL
 */
public class MapKeyboardHandler extends KeyboardHandler {

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p
     *            The PApplet.
     * @param maps
     *            One or more maps.
     */
    public MapKeyboardHandler(Mine p, UnfoldingMap... maps) {
        super(p, maps);
    }

    /**
     * Creates a KeyboardHandler for the given maps.
     *
     * @param p
     *            The PApplet.
     * @param maps
     *            A list of maps.
     */
    public MapKeyboardHandler(Mine p, List<UnfoldingMap> maps) {
        super(p, maps);
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        super.keyPressed(key, keyCode);

        if (keyCode == 67) {
            MapConfiguration.switchColor();
        }
    }
}
