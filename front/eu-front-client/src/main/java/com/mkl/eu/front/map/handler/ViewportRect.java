package com.mkl.eu.front.map.handler;

import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;

public class ViewportRect {
    private PApplet pApplet;
    private float x;
    private float y;
    private float w;
    private float h;
    private boolean dragged = false;

    public ViewportRect(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    public boolean isOver(float checkX, float checkY) {
        return checkX > x && checkY > y && checkX < x + w && checkY < y + h;
    }

    public void setDimension(ScreenPosition tl, ScreenPosition br) {
        this.x = tl.x;
        this.y = tl.y;
        this.w = br.x - tl.x;
        this.h = br.y - tl.y;
    }

    public float[] getCenter() {
        return new float[]{x + w / 2, y + h / 2};
    }

    public float[] getXY() {
        return new float[]{x, y};
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;
    }

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