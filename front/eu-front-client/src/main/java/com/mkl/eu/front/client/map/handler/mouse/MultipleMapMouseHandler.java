package com.mkl.eu.front.client.map.handler.mouse;

import com.mkl.eu.front.client.map.component.ViewportRect;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class MultipleMapMouseHandler extends AbstractMouseHandler {
    /** Small map showing the overview, i.e. the world. */
    private UnfoldingMap mapOverviewStatic;
    /** Interactive finder box atop the overview map. */
    private ViewportRect viewportRect;
    /** previous X of the mouse. */
    private float oldX;
    /** previous Y of the mouse. */
    private float oldY;

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p                 The PApplet.
     * @param mapOverviewStatic overview map.
     * @param viewportRect      Interactive finder box
     * @param maps              One or more maps.
     */
    public MultipleMapMouseHandler(PApplet p, UnfoldingMap mapOverviewStatic, ViewportRect viewportRect, UnfoldingMap... maps) {
        this(p, mapOverviewStatic, viewportRect, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p                 The PApplet.
     * @param mapOverviewStatic overview map.
     * @param viewportRect      Interactive finder box
     * @param maps              A list of maps.
     */
    public MultipleMapMouseHandler(PApplet p, UnfoldingMap mapOverviewStatic, ViewportRect viewportRect, List<UnfoldingMap> maps) {
        super(p, maps);

        this.mapOverviewStatic = mapOverviewStatic;
        this.viewportRect = viewportRect;
    }

    /** Move the maps to the new location. */
    public void panViewportOnDetailMap() {
        float[] xy = viewportRect.getCenter();
        Location newLocation = mapOverviewStatic.mapDisplay.getLocation(xy[0], xy[1]);
        for (UnfoldingMap map : maps) {
            map.panTo(newLocation);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean mousePressed() {
        boolean stop = super.mousePressed();
        if (!stop && viewportRect.isOver(getMouseX(), getMouseY())) {
            viewportRect.setDragged(true);
            float[] xy = viewportRect.getXY();
            oldX = getMouseX() - xy[0];
            oldY = getMouseY() - xy[1];
            stop = true;
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseReleased() {
        boolean stop = super.mouseReleased();
        if (!stop) {
            viewportRect.setDragged(false);
            stop = true;
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseDragged() {
        boolean stop = super.mouseDragged();
        if (!stop && viewportRect.isDragged()) {
            viewportRect.move(getMouseX() - oldX, getMouseY() - oldY);

            panViewportOnDetailMap();

            stop = true;
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseClicked() {
        boolean stop = super.mouseClicked();
        if (!stop && mapOverviewStatic.isHit(getMouseX(), getMouseY())) {
            viewportRect.moveCenter(getMouseX(), getMouseY());

            panViewportOnDetailMap();

            stop = true;
        }

        return stop;
    }
}
