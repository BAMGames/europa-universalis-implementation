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
    /** Padding in the contextual menu. */
    private static final int PADDING = 5;
    /** Inner padding in the contextual menu. */
    private static final int INNER_PADDING = 5;
    /** Vertical space used by each separator item in the contextual menu. */
    private static final int V_SEPARATOR = 10;
    /** Width of the contextual menu. */
    private float width;
    /** Height of the contextual menu. */
    private float height;
    /** Vertical space used by each text item in the contextual menu. */
    private float textHeight;
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
     * Initialize the settings of the menu.
     *
     * @param pg graphics.
     */
    public void init(PGraphics pg) {
        width = 0;
        height = 2 * PADDING;
        textHeight = pg.textSize;
        boolean first = true;

        for (ContextualMenuItem item : items) {

            switch (item.getType()) {
                case ITEM:
                case TEXT:
                    height += textHeight;
                    width = Math.max(width, pg.textWidth(item.getText()));
                    if (first) {
                        first = false;
                    } else {
                        height += INNER_PADDING;
                    }
                    break;
                case SEPARATOR:
                    height += V_SEPARATOR;
                    if (first) {
                        first = false;
                    } else {
                        height += INNER_PADDING;
                    }
                    break;
                default:
                    break;
            }
        }

        width += 2 * PADDING;
    }

    /**
     * Draw the component.
     *
     * @param pg graphics.
     */
    public void draw(PGraphics pg) {
        init(pg);
        pg.pushStyle();

        pg.fill(255, 255, 255, 255);
        pg.rect(location.getLat(), location.getLon(), width, -height);

        pg.fill(0, 0, 0);
        float y0 = location.getLon() - (height - PADDING);
        boolean first = true;
        for (ContextualMenuItem item : items) {
            if (first) {
                first = false;
            } else {
                y0 += INNER_PADDING;
            }

            switch (item.getType()) {
                case ITEM:
                    if (item == hovered) {
                        pg.fill(125, 125, 255, 255);
                        pg.rect(location.getLat() + PADDING, y0 - INNER_PADDING / 2, width - 2 * PADDING, textHeight + INNER_PADDING);
                        pg.fill(0, 0, 0);
                    }
                case TEXT:
                    y0 += textHeight;
                    pg.text(item.getText(), location.getLat() + PADDING, y0);
                    break;
                case SEPARATOR:
//                    if (item == hovered) {
//                        pg.fill(125, 125, 255, 255);
//                        pg.rect(location.getLat() + PADDING, y0 - INNER_PADDING / 2, width - 2 * PADDING, V_SEPARATOR + INNER_PADDING);
//                        pg.fill(0, 0, 0);
//                    }
                    y0 += V_SEPARATOR / 2;
                    pg.line(location.getLat() + PADDING, y0, location.getLat() + width - PADDING, y0);
                    y0 += V_SEPARATOR / 2;
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

        if (x >= location.getLat() && x <= location.getLat() + width
                && y <= location.getLon() && y >= location.getLon() - height) {
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

        if (x >= location.getLat() && x <= location.getLat() + width
                && y <= location.getLon() && y >= location.getLon() - height) {
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


        float y0 = location.getLon() - (height - PADDING);
        float y1;
        boolean first = true;
        for (ContextualMenuItem item : items) {
            if (first) {
                first = false;
            } else {
                y0 += INNER_PADDING;
            }
            switch (item.getType()) {
                case ITEM:
                case TEXT:
                    y1 = y0 + textHeight;
                    if (y >= y0 && y <= y1) {
                        hit = item;
                    }
                    y0 = y1;
                    break;
                case SEPARATOR:
                    y1 = y0 + V_SEPARATOR;
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
