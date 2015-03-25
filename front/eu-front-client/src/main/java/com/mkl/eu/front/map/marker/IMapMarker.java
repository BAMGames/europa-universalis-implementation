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

    /** @return the counters. */
    public List<CounterMarker> getCounters();

    /**
     * Sets the counters to the marker.
     *
     * @param counters the counters to set.
     */
    public void setCounters(List<CounterMarker> counters);

    /**
     * Add a counter to the marker.
     *
     * @param counter the counter to add.
     */
    public void addCounter(CounterMarker counter);

    /**
     * Add counters to the marker.
     *
     * @param counters the counters to add.
     */
    public void addCounters(List<CounterMarker> counters);

    /**
     * Remove a counter to the marker.
     *
     * @param counter the counter to remove.
     */
    public void removeCounter(CounterMarker counter);

    /**
     * Remove counters to the marker.
     *
     * @param counters the counters to remove.
     */
    public void removeCounters(List<CounterMarker> counters);

    /**
     * Method called when the marker is hovered.
     *
     * @param map the map.
     * @param x   X coordinate.
     * @param y   Y coordinate.
     */
    void hover(UnfoldingMap map, int x, int y);
}
