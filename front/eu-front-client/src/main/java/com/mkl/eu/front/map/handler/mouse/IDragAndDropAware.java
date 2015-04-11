package com.mkl.eu.front.map.handler.mouse;

import de.fhpotsdam.unfolding.geo.Location;

/**
 * Description of file.
 *
 * @author MKL.
 */
public interface IDragAndDropAware<T, U> {
    /**
     * Checks whether the given screen coordinates are on this View.
     *
     * @param checkX The vertical position to check.
     * @param checkY The horizontal position to check.
     * @return <code>true</code> if screen is hit, <code>false</code> otherwise.
     */
    boolean isHit(int checkX, int checkY);


    /**
     * Returns the drag object, if it exists, at the coordinates.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return the counter, if it exists, at the coordinates.
     */
    T getDrag(int x, int y);

    /**
     * Set the object being dragged.
     *
     * @param dragged object
     */
    void setDragged(T dragged);

    /**
     * @return the object being dragged.
     */
    T getDragged();

    /**
     * @return the location of the object being dragged.
     */
    Location getDragLocation();

    /**
     * Set the location of the object being dragged.
     *
     * @param newLocation location.
     */
    void setDragLocation(Location newLocation);

    /**
     * Returns the drop object, if it exists, at the coordinates.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return the stack, if it exists, at the coordinates.
     */
    U getDrop(int x, int y);
}
