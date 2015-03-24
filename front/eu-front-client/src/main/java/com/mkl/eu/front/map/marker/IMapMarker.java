package com.mkl.eu.front.map.marker;

import java.util.List;

/**
 * Interface of all the map markers.
 *
 * @author MKL
 */
public interface IMapMarker {
    /**
     * Sets the highlightColor.
     *
     * @param highlightColor the highlightColor to set.
     */
    public void setHighlightColor(int highlightColor);

    /** @return the neighbours. */
    public List<BorderMarker> getNeighbours();

    /**
     * Add a neighbour.
     *
     * @param neighbour the neighbour to add.
     */
    public void addNeighbours(BorderMarker neighbour);

    /** @return the id; */
    public String getId();
}
