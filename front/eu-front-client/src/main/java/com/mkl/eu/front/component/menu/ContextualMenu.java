package com.mkl.eu.front.component.menu;

import de.fhpotsdam.unfolding.geo.Location;
import javafx.event.ActionEvent;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Contextual Menu on the map.
 * I'm sure I could have found something similar already coded, but wanted to see what I could do.
 *
 * @author MKL.
 */
public class ContextualMenu {
    /** Width of the contextual menu. */
    private static final int MENU_WIDTH = 50;
    /** Height of the contextual menu. */
    private static final int MENU_HEIGHT = 75;
    /** Padding in the contextual menu. */
    private static final int PADDING = 5;
    /** Vertical space used by each item in the contextual menu. */
    private static final int V_SPACE = 20;
    /** Location of the contextual menu. */
    private Location location;
    /** Items of the menu. */
    private List<ContextualMenuItem> items = new ArrayList<>();
    /** Item being hovered. */
    private ContextualMenuItem hovered;

    /**
     * Add the item to the menu.
     *
     * @param item to be added.
     */
    public void addMenuItem(ContextualMenuItem item) {
        items.add(item);
    }

    /**
     * Draw the component.
     *
     * @param pg graphics.
     */
    public void draw(PGraphics pg) {
        pg.pushStyle();

        pg.fill(255, 255, 255, 255);
        pg.rect(location.getLat(), location.getLon(), MENU_WIDTH, -MENU_HEIGHT);

        pg.fill(0, 0, 0);
        float y0 = location.getLon() - (MENU_HEIGHT - PADDING);
        for (ContextualMenuItem item : items) {

            switch (item.getType()) {
                case ITEM:
                case TEXT:
                    if (item == hovered) {
                        pg.fill(125, 125, 255, 255);
                        pg.rect(location.getLat() + PADDING, y0 + V_SPACE / 4, MENU_WIDTH - 2 * PADDING, V_SPACE);
                        pg.fill(0, 0, 0);
                    }
                    y0 += V_SPACE;
                    pg.text(item.getText(), location.getLat() + PADDING, y0);
                    break;
                case SEPARATOR:
                    if (item == hovered) {
                        pg.fill(125, 125, 255, 255);
                        pg.rect(location.getLat() + PADDING, y0 + V_SPACE / 4, MENU_WIDTH - 2 * PADDING, V_SPACE);
                        pg.fill(0, 0, 0);
                    }
                    y0 += V_SPACE / 2;
                    pg.line(location.getLat() + PADDING, y0, location.getLat() + MENU_WIDTH - PADDING, y0);
                    y0 += V_SPACE / 2;
                    break;
                default:
                    break;
            }
        }

        pg.popStyle();
    }


    /**
     * Method called when a hit action is performed at the x/y coordinates.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return <code>true</code> if something related to the contextual menu occurred, so it does not try to trigger anything else, <code>false</code> otherwise.
     */
    public boolean hit(int x, int y) {
        boolean hit = false;

        if (x >= location.getLat() && x <= location.getLat() + MENU_WIDTH
                && y <= location.getLon() && y >= location.getLon() - MENU_HEIGHT) {
            hit = true;

            ContextualMenuItem item = getItemAtLocation(x, y);
            if (item != null && item.getType() == ContextualMenuItemTypeEnum.ITEM) {
                item.getHandler().handle(new ActionEvent());
            }
        }

        return hit;
    }

    /**
     * Hover the given location.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return <code>true</code> if something was hovered, <ocde>false</ocde> otherwise.
     */
    public boolean hover(int x, int y) {
        boolean hover = false;

        if (x >= location.getLat() && x <= location.getLat() + MENU_WIDTH
                && y <= location.getLon() && y >= location.getLon() - MENU_HEIGHT) {
            hover = true;

            hovered = getItemAtLocation(x, y);
        }

        return hover;
    }

    /**
     * Returns the item at the given location.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return the item at the given location.
     */
    private ContextualMenuItem getItemAtLocation(int x, int y) {
        ContextualMenuItem hit = null;


        float y0 = location.getLon() - (MENU_HEIGHT - PADDING);
        float y1;
        for (ContextualMenuItem item : items) {
            switch (item.getType()) {
                case ITEM:
                case TEXT:
                case SEPARATOR:
                    y1 = y0 + V_SPACE;
                    if (y >= y0 && y <= y1) {
                        hit = item;
                    }
                    y0 = y1;
                    break;
                default:
                    break;
            }
        }

        return hit;
    }

    /** @param location the location to set. */
    public void setLocation(Location location) {
        this.location = location;
    }
}
