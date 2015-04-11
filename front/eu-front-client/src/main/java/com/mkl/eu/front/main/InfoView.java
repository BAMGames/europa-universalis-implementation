package com.mkl.eu.front.main;

import com.mkl.eu.front.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.map.handler.mouse.IDragAndDropAware;
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
public class InfoView implements IDragAndDropAware<CounterMarker, StackMarker>, IContextualMenuAware<CounterMarker> {
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
    /** Width of the contextual menu. */
    private static final int MENU_WIDTH = 50;
    /** Height of the contextual menu. */
    private static final int MENU_HEIGHT = 75;
    /** Padding in the contextual menu. */
    private static final int PADDING = 5;
    /** Vertical space used by each item in the contextual menu. */
    private static final int V_SPACE = 20;
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
    /** Counter being contextualized. */
    private CounterMarker contextualized;
    /** Location of the contextual menu. */
    private Location menuLocation;

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

        if (contextualized != null && menuLocation != null) {
            pApplet.pushStyle();

            pApplet.fill(255, 255, 255, 255);
            pApplet.rect(menuLocation.getLat(), menuLocation.getLon(), MENU_WIDTH, -MENU_HEIGHT);

            pApplet.fill(0, 0, 0);
            pApplet.text("Disband", menuLocation.getLat() + PADDING, menuLocation.getLon() - (MENU_HEIGHT - PADDING - V_SPACE));

            pApplet.popStyle();

        }

        pApplet.popStyle();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHit(int checkX, int checkY) {
        return checkX > x && checkX < x + w && checkY > y && checkY < y + h;
    }

    /** {@inheritDoc} */
    @Override
    public CounterMarker getDragged() {
        return dragged;
    }

    /** {@inheritDoc} */
    @Override
    public void setDragged(CounterMarker dragged) {
        this.dragged = dragged;
    }

    /** {@inheritDoc} */
    @Override
    public Location getDragLocation() {
        return dragLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setDragLocation(Location dragLocation) {
        this.dragLocation = dragLocation;
    }

    /** @return the selected marker. */
    public Marker getSelected() {
        return markerManager.getSelectedMarker();
    }

    /** {@inheritDoc} */
    @Override
    public CounterMarker getDrag(int x, int y) {
        CounterMarker counter = null;

        StackMarker stack = getDrop(x, y);

        if (stack != null) {
            float newX = this.x + H_PADDING;
            int counterNumber = (int) ((x - newX) / (SIZE + SPACE));
            if (counterNumber >= 0 && counterNumber < stack.getCounters().size()) {
                counter = stack.getCounters().get(counterNumber);
            }
        }

        return counter;
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getDrop(int x, int y) {
        StackMarker stack = null;
        float newY = this.y + V_PADDING + 2 * V_TEXT;

        int stackNumber = (int) ((y - newY) / (SIZE + SPACE));
        if (markerManager.getSelectedMarker() instanceof IMapMarker
                && stackNumber >= 0 && stackNumber < ((IMapMarker) markerManager.getSelectedMarker()).getStacks().size()) {
            stack = ((IMapMarker) markerManager.getSelectedMarker()).getStacks().get(stackNumber);
        }

        return stack;
    }

    /** {@inheritDoc} */
    @Override
    public CounterMarker getContextualizedItem(int x, int y) {
        return getDrag(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public void contextualMenu(CounterMarker item, Location menuLocation) {
        if (item == contextualized) {
            resetContextualMenu();
        } else {
            this.contextualized = item;
            this.menuLocation = menuLocation;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetContextualMenu() {
        this.contextualized = null;
        this.menuLocation = null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hit(int x, int y) {
        if (menuLocation == null || contextualized == null) {
            return false;
        }

        boolean hit = false;

        if (x >= menuLocation.getLat() && x <= menuLocation.getLat() + MENU_WIDTH
                && y <= menuLocation.getLon() && y >= menuLocation.getLon() - MENU_HEIGHT) {
            hit = true;

            if (y <= menuLocation.getLon() - (MENU_HEIGHT - PADDING - V_SPACE) && y >= menuLocation.getLon() - (MENU_HEIGHT - PADDING)) {
                StackMarker stack = contextualized.getOwner();
                stack.removeCounter(contextualized);

                if (stack.getCounters().isEmpty()) {
                    ((IMapMarker) getSelected()).removeStack(stack);
                }
            }
        }

        return hit;
    }
}