package com.mkl.eu.front.map.marker;

import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import processing.core.PConstants;
import processing.core.PGraphics;

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
    /** The stack being dragged. */
    private StackMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;

    /** {@inheritDoc} */
    public void draw() {
        if (!bEnableDrawing)
            return;

        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, dragged);
            } else {
                marker.draw(map);
            }
        }

        if (dragged != null && dragLocation != null) {
            PGraphics pg = map.mapDisplay.getOuterPG();

            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(dragLocation);
            float size = 0.08f * map.getZoom();
            for (int j = 0; j < dragged.getCounters().size(); j++) {
                CounterMarker counter = dragged.getCounters().get(j);
                float x0 = xy[0];
                pg.image(counter.getImage(), x0 + size * j / 10
                        , xy[1] + size * j / 10, size, size);
            }
            pg.popStyle();
        }
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

    /** @return the dragged. */
    public StackMarker getDragged() {
        return dragged;
    }

    /** @param dragged the dragged to set. */
    public void setDragged(StackMarker dragged) {
        this.dragged = dragged;
    }

    /** @return the dragLocation. */
    public Location getDragLocation() {
        return dragLocation;
    }

    /** @param dragLocation the dragLocation to set. */
    public void setDragLocation(Location dragLocation) {
        this.dragLocation = dragLocation;
    }
}
