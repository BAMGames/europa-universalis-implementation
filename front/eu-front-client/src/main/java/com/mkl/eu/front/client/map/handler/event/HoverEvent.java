package com.mkl.eu.front.client.map.handler.event;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEvent;

/**
 * Map event of type hover.
 *
 * @author MKL.
 */
public class HoverEvent extends MapEvent {
    /** Type of the drag event. */
    public static final String TYPE_HOVER = "hover";
    /** Coordinate x where it is dragged. */
    private int x;
    /** Coordinate y where it is dragged. */
    private int y;

    /**
     * Constructor.
     *
     * @param source of the drag event.
     * @param mapId  id of the map where the drag occured.
     */
    public HoverEvent(Object source, String mapId) {
        super(source, TYPE_HOVER, mapId);
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
