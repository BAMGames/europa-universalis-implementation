package com.mkl.eu.front.main;

import com.mkl.eu.front.map.marker.BorderMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.MyMarkerManager;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PApplet;

import java.util.List;

/**
 * Information panel.
 *
 * @author MKL
 */
public class InfoView {
    /** Vertical Padding. */
    private static final float V_PADDING = 20;
    /** Horizontal Padding. */
    private static final float H_PADDING = 10;
    /** Vertical Space between two objects. */
    private static final float V_SPACE = 20;
    /** PApplet for drawing purpose. */
    private PApplet pApplet;
    /** Marker manager to obtain the selected province. */
    private MyMarkerManager markerManager;
    /** X coordinate. */
    private float x;
    /** Y coordinate. */
    private float y;
    /** Width. */
    private float w;
    /** Height. */
    private float h;

    /**
     * Constructor.
     *
     * @param pApplet the pApplet.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param w Width.
     * @param h Height.
     */
    public InfoView(PApplet pApplet, MyMarkerManager markerManager, float x, float y, float w, float h) {
        this.pApplet = pApplet;
        this.markerManager = markerManager;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /** Draw the viewport. */
    public void draw() {
        pApplet.fill(255, 255, 255);
        pApplet.rect(x, y, w, h);
        pApplet.fill(0, 0, 0);

        float newX = x + H_PADDING;
        float newY = y + V_PADDING;

        List<Marker> markers = markerManager.getSelectedMarkers();
        for (Marker marker: markers) {
            pApplet.text(marker.getId(), newX, newY);
            newY += V_SPACE;
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;

                pApplet.text("Neighbours:", newX, newY);
                newY += V_SPACE;
                for (BorderMarker neighbour: mapMarker.getNeighbours()) {

                    StringBuilder text = new StringBuilder(neighbour.getProvince().getId());
                    if (neighbour.getType() != null) {
                            text.append(" (").append(neighbour.getType()).append(")");
                    }
                    pApplet.text(text.toString(), newX + H_PADDING, newY);
                    newY += V_SPACE;
                }
            }
        }
    }

    /**
     * Checks whether the given screen coordinates are on this View.
     *
     * @param checkX
     *            The vertical position to check.
     * @param checkY
     *            The horizontal position to check.
     * @return True if screen is hit, false otherwise.
     */
    public boolean isHit(float checkX, float checkY) {
        return checkX > x && checkX < x + w && checkY > y && checkY < y + h;
    }

    /**
     * Method called when a clic is performed on (x, y).
     * @param x the vertical position.
     * @param y the horizontal position.
     */
    public void trigger(int x, int y) {
        float y0 = this.y + V_PADDING;
        int i = (int)(((y + V_SPACE) - y0) / V_SPACE);

        List<Marker> markers = markerManager.getSelectedMarkers();
        for (Marker marker: markers) {
            i -= 1;
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;
                i -= 1;

                if (i >= mapMarker.getNeighbours().size()) {
                    i -= mapMarker.getNeighbours().size();
                } else if (i >= 0) {
                    BorderMarker destination = mapMarker.getNeighbours().get(i);
                    destination.getProvince().addCounters(mapMarker.getCounters());
                    mapMarker.removeCounters(mapMarker.getCounters());
                }
            }
        }
    }
}