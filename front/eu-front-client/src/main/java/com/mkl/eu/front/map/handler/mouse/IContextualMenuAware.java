package com.mkl.eu.front.map.handler.mouse;

import de.fhpotsdam.unfolding.geo.Location;

/**
 * Description of file.
 *
 * @param <T> type of the item being contextualized.
 * @author MKL.
 */
public interface IContextualMenuAware<T> {
    /**
     * Return an object that can be contextualized at the given coordinates.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return an object that can be contextualized at the given coordinates.
     */
    T getContextualizedItem(int x, int y);

    /**
     * Trigger the contextual menu on a marker and at a given location.
     *
     * @param item         item to be contextualized.
     * @param menuLocation location of the menu.
     */
    void contextualMenu(T item, Location menuLocation);

    /**
     * Reset all the information related to the contextual menu.
     */
    void resetContextualMenu();

    /**
     * Method called when a hit action is performed at the x/y coordinates.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return <code>true</code> if something related to the contextual menu occurred, so it does not try to trigger anything else, <code>false</code> otherwise.
     */
    boolean hit(int x, int y);
}
