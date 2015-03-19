package com.mkl.eu.front.main;

import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
public class MyMarkerManager extends MarkerManager<Marker> {

    /**
     * {@inheritDoc}
     */
    public void draw() {
        long begin = System.currentTimeMillis();
        if (!bEnableDrawing)
            return;

        for (Marker marker : markers) {
            marker.draw(map);
        }

        long end = System.currentTimeMillis();

        System.out.println("Draw: " + (end - begin) / 1000f + " secondes.");
    }
}
