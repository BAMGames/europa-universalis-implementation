package com.mkl.eu.front.main;

import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;

/**
 * Interactive finder box atop the overview map.
 * @author MKL
 */
public class ViewportRect {
    /** PApplet for drawing purpose. */
    private PApplet pApplet;
    /** X coordinate. */
    private float x;
    /** Y coordinate. */
    private float y;
    /** Width. */
    private float w;
    /** Height. */
    private float h;
    /** Flag pointing out that the viewport is being dragged. */
    private boolean dragged = false;

    /**
     * Constructor.
     * @param pApplet the pApplet.
     */
    public ViewportRect(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    /**
     * Returns <code>true</code> if the viewport is over the coordinates.
     * @param checkX the X coordinate.
     * @param checkY the Y coordinate.
     * @return <code>true</code> if the viewport is over the coordinates.
     */
    public boolean isOver(float checkX, float checkY) {
        return checkX > x && checkY > y && checkX < x + w && checkY < y + h;
    }

    /**
     * Sets the position of the viewport.
     * @param tl top left point.
     * @param br bottom right point.
     */
    public void setDimension(ScreenPosition tl, ScreenPosition br) {
        this.x = tl.x;
        this.y = tl.y;
        this.w = br.x - tl.x;
        this.h = br.y - tl.y;
    }

    /**
     * @return the center of the viewport.
     */
    public float[] getCenter() {
        return new float[]{x + w / 2, y + h / 2};
    }

    /**
     * @return the base coordinates of the viewport.
     */
    public float[] getXY() {
        return new float[]{x, y};
    }

    /**
     * Move the viewport to the selected area.
     * @param x the X coordinate.
     * @param y the Y coordinate.
     */
    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Move the viewport center to the selected area.
     * @param x the X coordinate.
     * @param y the Y coordinate.
     */
    public void moveCenter(float x, float y) {
        this.x = x - w / 2;
        this.y = y - h / 2;
    }

    /**
     * Draw the viewport.
     */
    public void draw() {
        pApplet.noFill();
        pApplet.stroke(251, 114, 0, 240);
        pApplet.rect(x, y, w, h);
    }

    /** @return the dragged. */
    public boolean isDragged() {
        return dragged;
    }

    /** @param dragged the dragged to set. */
    public void setDragged(boolean dragged) {
        this.dragged = dragged;
    }
}