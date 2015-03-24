package com.mkl.eu.front.map.marker;

import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
public class MyMarkerManager extends MarkerManager<Marker> {
    /** Selected markers. */
    private List<Marker> selectedMarkers = new ArrayList<>();

    /** {@inheritDoc} */
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

    /**
     * Select a marker and unselect the previous ones.
     *
     * @param marker to be selected.
     */
    public void select(Marker marker) {
        for (Iterator<Marker> markers = selectedMarkers.iterator(); markers.hasNext(); ) {
            markers.next().setSelected(false);
            markers.remove();
        }

        marker.setSelected(true);

        selectedMarkers.add(marker);
    }

    /** @return the selectedMarkers. */
    public List<Marker> getSelectedMarkers() {
        return selectedMarkers;
    }
}
