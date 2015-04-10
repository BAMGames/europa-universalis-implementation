package com.mkl.eu.front.main;

import com.mkl.eu.front.map.marker.CounterMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.MyMarkerManager;
import com.mkl.eu.front.map.marker.StackMarker;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PApplet;
import processing.core.PConstants;

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
    /** Vertical Space taken by a text line. */
    private static final float V_TEXT = 20;
    /** Space between two objects. */
    private static final float SPACE = 10;
    /** Size of a counter. */
    private static final float SIZE = 30;
    /** PApplet for drawing purpose. */
    private PApplet pApplet;
    /** Marker manager to obtain the selected province. */
    private MyMarkerManager markerManager;
    /** The counter being dragged. */
    private CounterMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
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
     * @param x       X coordinate.
     * @param y       Y coordinate.
     * @param w       Width.
     * @param h       Height.
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
        pApplet.pushStyle();
        pApplet.fill(255, 255, 255);
        pApplet.rect(x, y, w, h);
        pApplet.fill(0, 0, 0);

        float newX = x + H_PADDING;
        float newY = y + V_PADDING;

        Marker marker = markerManager.getSelectedMarker();
        if (marker != null) {
            pApplet.text(marker.getId(), newX, newY);
            newY += V_TEXT;
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;

                pApplet.text("Stacks:", newX, newY);
                newY += V_TEXT;
                pApplet.imageMode(PConstants.CORNER);
                for (int i = 0; i < mapMarker.getStacks().size(); i++) {
                    for (int j = 0; j < mapMarker.getStacks().get(i).getCounters().size(); j++) {
                        CounterMarker counter = mapMarker.getStacks().get(i).getCounters().get(j);
                        if (counter != dragged) {
                            pApplet.image(counter.getImage(), newX + (SIZE + SPACE) * j
                                    , newY + (SIZE + SPACE) * i, SIZE, SIZE);
                        }
                    }
                }
            }
        }

        if (dragged != null && dragLocation != null) {
            pApplet.image(dragged.getImage(), dragLocation.getLat(), dragLocation.getLon(),
                    SIZE, SIZE);
        }

        pApplet.popStyle();
    }

    /**
     * Checks whether the given screen coordinates are on this View.
     *
     * @param checkX The vertical position to check.
     * @param checkY The horizontal position to check.
     * @return True if screen is hit, false otherwise.
     */
    public boolean isHit(float checkX, float checkY) {
        return checkX > x && checkX < x + w && checkY > y && checkY < y + h;
    }

    /** @return the dragged. */
    public CounterMarker getDragged() {
        return dragged;
    }

    /** @param dragged the dragged to set. */
    public void setDragged(CounterMarker dragged) {
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

    /** @return the selected marker. */
    public Marker getSelected() {
        return markerManager.getSelectedMarker();
    }

    /**
     * Returns the counter, if it exists, at the coordinates.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return the counter, if it exists, at the coordinates.
     */
    public CounterMarker getCounter(int x, int y) {
        CounterMarker counter = null;

        StackMarker stack = getStack(x, y);

        if (stack != null) {
            float newX = this.x + H_PADDING;
            int counterNumber = (int) ((x - newX) / (SIZE + SPACE));
            if (counterNumber >= 0 && counterNumber < stack.getCounters().size()) {
                counter = stack.getCounters().get(counterNumber);
            }
        }

        return counter;
    }

    /**
     * Returns the stack, if it exists, at the coordinates.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return the stack, if it exists, at the coordinates.
     */
    public StackMarker getStack(int x, int y) {
        StackMarker stack = null;
        float newY = this.y + V_PADDING + 2 * V_TEXT;

        int stackNumber = (int) ((y - newY) / (SIZE + SPACE));
        if (markerManager.getSelectedMarker() instanceof IMapMarker
                && stackNumber >= 0 && stackNumber < ((IMapMarker) markerManager.getSelectedMarker()).getStacks().size()) {
            stack = ((IMapMarker) markerManager.getSelectedMarker()).getStacks().get(stackNumber);
        }

        return stack;
    }
}