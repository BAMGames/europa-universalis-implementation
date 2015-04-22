package com.mkl.eu.front.client.map.handler.event;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEvent;

/**
 * Map event of type Drag and drop.
 *
 * @author MKL.
 */
public class ClickEvent extends MapEvent {
    /** Type of the drag event. */
    public static final String TYPE_CLICK = "click";
    /** Subtype of click: left click. */
    public static final String CLICK_LEFT = "clickLeft";
    /** Subtype of click: right click. */
    public static final String CLICK_RIGHT = "clickRight";
    /** Subtype of click: middle click. */
    public static final String CLICK_MIDDLE = "clickMiddle";
    /** Coordinate x where it is dragged. */
    private int x;
    /** Coordinate y where it is dragged. */
    private int y;

    /**
     * Constructor.
     *
     * @param source  of the drag event.
     * @param mapId   id of the map where the drag occured.
     * @param subType of the event.
     */
    public ClickEvent(Object source, String mapId, String subType) {
        super(source, TYPE_CLICK, mapId);
        setSubType(subType);
    }

    /** @return the y. */
    public int getY() {
        return y;
    }

    /** @param y the y to set. */
    public void setY(int y) {
        this.y = y;
    }

    /** @return the x. */
    public int getX() {
        return x;
    }

    /** @param x the x to set. */
    public void setX(int x) {
        this.x = x;
    }

    /** {@inheritDoc} */
    @Override
    public void executeManipulationFor(UnfoldingMap map) {

    }
}
