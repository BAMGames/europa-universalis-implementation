package com.mkl.eu.front.map.handler;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class MultipleMapMouseHandler extends MapEventBroadcaster {
    /** Small map showing the overview, i.e. the world. */
    private UnfoldingMap mapOverviewStatic;
    /** Interactive finder box atop the overview map. */
    private ViewportRect viewportRect;
    /** previous X of the mouse. */
    private float oldX;
    /** previous Y of the mouse. */
    private float oldY;

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------
    /** mouseX. */
    private int mouseX;
    /** mouseY. */
    private int mouseY;

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
     * @param p    The PApplet.
     * @param mapOverviewStatic overview map.
     * @param viewportRect      Interactive finder box
     * @param maps A list of maps.
     */
    public MultipleMapMouseHandler(PApplet p, UnfoldingMap mapOverviewStatic, ViewportRect viewportRect, List<UnfoldingMap> maps) {
        super(maps);

        this.mapOverviewStatic = mapOverviewStatic;
        this.viewportRect = viewportRect;

        p.registerMethod("mouseEvent", this);
    }

    /** Move the maps to the new location. */
    public void panViewportOnDetailMap() {
        float[] xy = viewportRect.getCenter();
        Location newLocation = mapOverviewStatic.mapDisplay.getLocation(xy[0], xy[1]);
        for (UnfoldingMap map : maps) {
            map.panTo(newLocation);
        }
    }

    /** Mouse event of type PRESSED. */
    public void mousePressed() {
        if (viewportRect.isOver(mouseX, mouseY)) {
            viewportRect.setDragged(true);
            float[] xy = viewportRect.getXY();
            oldX = mouseX - xy[0];
            oldY = mouseY - xy[1];
        }
    }

    /** Mouse event of type RELEASED. */
    public void mouseReleased() {
        viewportRect.setDragged(false);
    }

    /** Mouse event of type DRAGGED. */
    public void mouseDragged() {
        if (viewportRect.isDragged()) {
            viewportRect.move(mouseX - oldX, mouseY - oldY);

            panViewportOnDetailMap();
        }
    }

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------

    /**
     * Method called by the PApplet.
     *
     * @param event mouse event.
     */
    public void mouseEvent(MouseEvent event) {
        int action = event.getAction();

        if ((action == MouseEvent.DRAG) || (action == MouseEvent.MOVE)) {
            mouseX = event.getX();
            mouseY = event.getY();
        }

        switch (action) {
            case MouseEvent.PRESS:
                mousePressed();
                break;
            case MouseEvent.DRAG:
                mouseDragged();
                break;
            case MouseEvent.RELEASE:
                mouseReleased();
                break;
            default:
                break;
        }
    }
}
