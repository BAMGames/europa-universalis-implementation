package com.mkl.eu.front.client.map.handler.event;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEvent;

/**
 * Description of file.
 *
 * @author MKL.
 */
public class DragEvent extends MapEvent {
    /** Type of the drag event. */
    public static final String TYPE_DRAG = "drag";
    /** Subtype of drag first step: take. */
    public static final String DRAG_TAKE = "dragTake";
    /** Subtype of drag middle step: travel. */
    public static final String DRAG_TO = "dragTo";
    /** Subtype of drag final step: drop. */
    public static final String DRAG_DROP = "dragDrop";
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
    public DragEvent(Object source, String mapId, String subType) {
        super(source, TYPE_DRAG, mapId);
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
