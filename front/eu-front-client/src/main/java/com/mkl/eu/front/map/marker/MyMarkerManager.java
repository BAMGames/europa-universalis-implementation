package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
public class MyMarkerManager extends MarkerManager<Marker> {
    /** Width of the contextual menu. */
    private static final int MENU_WIDTH = 50;
    /** Height of the contextual menu. */
    private static final int MENU_HEIGTH = 75;
    /** Padding in the contextual menu. */
    private static final int PADDING = 5;
    /** Vertical space used by each item in the contextual menu. */
    private static final int V_SPACE = 20;
    /** Utility to draw counters. */
    private MarkerUtils markerUtils;
    /** Selected marker. */
    private Marker selectedMarker;
    /** The stack being dragged. */
    private StackMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
    /** Marker being contextualized. */
    private IMapMarker contextualized;
    /** Location of the contextual menu. */
    private Location menuLocation;


    /**
     * Constructor.
     *
     * @param markerUtils markerUtils.
     */
    public MyMarkerManager(MarkerUtils markerUtils) {
        this.markerUtils = markerUtils;
    }

    /** {@inheritDoc} */
    public void draw() {
        if (!bEnableDrawing)
            return;

        PGraphics pg = map.mapDisplay.getOuterPG();

        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, dragged);
            } else {
                marker.draw(map);
            }
        }

        if (dragged != null && dragLocation != null) {
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

        if (contextualized != null && menuLocation != null) {
            pg.pushStyle();

            pg.fill(255, 255, 255);
            float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
            pg.rect(xy[0], xy[1], MENU_WIDTH, -MENU_HEIGTH);

            pg.fill(0, 0, 0);
            pg.text("New", xy[0] + PADDING, xy[1] - (MENU_HEIGTH - PADDING - V_SPACE));

            pg.popStyle();

        }
    }

    /**
     * Method called when a hit action is performed at the x/y coordinates.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return <code>true</code> if something related to the MarkerManager occured, so it does not try to trigger anything else, <code>false</code> otherwise.
     */
    public boolean hit(int x, int y) {
        if (menuLocation == null || contextualized == null) {
            return false;
        }

        boolean hit = false;
        float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);

        if (x >= xy[0] && x <= xy[0] + MENU_WIDTH
                && y <= xy[1] && y >= xy[1] - MENU_HEIGTH) {
            hit = true;

            if (y <= xy[1] - (MENU_HEIGTH - PADDING - V_SPACE) && y >= xy[1] - (MENU_HEIGTH - PADDING)) {
                Stack stack = new Stack();
                Counter counter = new Counter();
                counter.setCountry(new Country());
                counter.getCountry().setName("FRA");
                counter.setOwner(stack);
                counter.setType(CounterTypeEnum.ARMY_PLUS);
                stack.getCounters().add(counter);
                StackMarker stackMarker = new StackMarker(stack, contextualized);
                stackMarker.addCounter(new CounterMarker(counter, markerUtils.getImageFromCounter(counter)));
                contextualized.addStack(stackMarker);
            }
        }

        return hit;
    }

    /**
     * Select a marker and unselect the previous one.
     *
     * @param marker to be selected.
     */
    public void select(Marker marker) {
        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
        }

        selectedMarker = marker;

        selectedMarker.setSelected(true);
    }

    /**
     * Trigger the contextual menu on a marker and at a given location.
     *
     * @param marker       item to be contextualized.
     * @param menuLocation location of the menu.
     */
    public void contextualMenu(IMapMarker marker, Location menuLocation) {
        if (marker == contextualized) {
            resetContextualMenu();
        } else {
            this.contextualized = marker;
            this.menuLocation = menuLocation;
        }
    }

    /**
     * Reset all the informations related to the contextual menu.
     */
    public void resetContextualMenu() {
        this.contextualized = null;
        this.menuLocation = null;
    }

    /** @return the selectedMarker. */
    public Marker getSelectedMarker() {
        return selectedMarker;
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
