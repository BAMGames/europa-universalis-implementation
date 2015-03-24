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

        float vPadding = 10;
        float hPadding = 10;

        float newX = x + hPadding;
        float newY = y + vPadding;
        float vSpace = 15;

        List<Marker> markers = markerManager.getSelectedMarkers();
        for (Marker marker: markers) {
            pApplet.text(marker.getId(), newX, newY);
            newY += vSpace;
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;

                pApplet.text("Neighbours:", newX, newY);
                newY += vSpace;
                for (BorderMarker neighbour: mapMarker.getNeighbours()) {
                    StringBuilder text = new StringBuilder(neighbour.getProvince().getId());
                    if (neighbour.getType() != null) {
                            text.append(" (").append(neighbour.getType()).append(")");
                    }
                    pApplet.text(text.toString(), newX + hPadding, newY);
                    newY += vSpace;
                }
            }
        }
    }
}