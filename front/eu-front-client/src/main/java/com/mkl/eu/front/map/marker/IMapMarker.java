package com.mkl.eu.front.map.marker;

import de.fhpotsdam.unfolding.UnfoldingMap;

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

    /** @return the stacks. */
    public List<StackMarker> getStacks();

    /**
     * Sets the stacks to the marker.
     *
     * @param stacks the stacks to set.
     */
    public void setStacks(List<StackMarker> stacks);

    /**
     * Add a stack to the marker.
     *
     * @param stack the stack to add.
     */
    public void addStack(StackMarker stack);

    /**
     * Remove a stack to the marker.
     *
     * @param stack the stack to remove.
     */
    public void removeStack(StackMarker stack);

    /**
     * Method called when the marker is hovered.
     *
     * @param map the map.
     * @param x   X coordinate.
     * @param y   Y coordinate.
     */
    void hover(UnfoldingMap map, int x, int y);
}
