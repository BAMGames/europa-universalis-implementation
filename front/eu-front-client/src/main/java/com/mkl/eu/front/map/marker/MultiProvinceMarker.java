package com.mkl.eu.front.map.marker;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Province marker that consists of various pieces.
 *
 * @author MKL
 */
public class MultiProvinceMarker extends MultiMarker implements IMapMarker {
    /** Neighbours of the province. */
    private List<BorderMarker> neighbours = new ArrayList<>();
    /** Counters of the province. */
    private List<CounterMarker> counters = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param markers sons of the multi provinc emarker.
     */
    public MultiProvinceMarker(List<Marker> markers) {
        this.markers = markers;
    }

    /** {@inheritDoc} */
    @Override
    public void setHighlightColor(int highlightColor) {
        for (Marker subMarker : getMarkers()) {
            if (subMarker instanceof AbstractMarker) {
                ((AbstractMarker) subMarker).setHighlightColor(highlightColor);
            }
        }
    }

    /**
     * @return the first IMapMarker of the markers.
     */
    private IMapMarker getFirstMapMarker() {
        IMapMarker mapMarker = null;

        if (markers != null) {
            for (Marker marker : markers) {
                if (marker instanceof IMapMarker) {
                    mapMarker = (IMapMarker) marker;
                    break;
                }
            }
        }

        return mapMarker;
    }

    /** @return the neighbours. */
    public List<BorderMarker> getNeighbours() {
        return neighbours;
    }

    /**
     * Add a neighbour.
     *
     * @param neighbour the neighbour to add.
     */
    public void addNeighbours(BorderMarker neighbour) {
        neighbours.add(neighbour);
    }

    /** {@inheritDoc} */
    @Override
    public List<CounterMarker> getCounters() {
        return getFirstMapMarker().getCounters();
    }

    /** {@inheritDoc} */
    @Override
    public void setCounters(List<CounterMarker> counters) {
        getFirstMapMarker().setCounters(counters);
    }

    /** {@inheritDoc} */
    @Override
    public void addCounter(CounterMarker counter) {
        getFirstMapMarker().addCounter(counter);
    }

    /** {@inheritDoc} */
    @Override
    public void addCounters(List<CounterMarker> counters) {
        getFirstMapMarker().addCounters(counters);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCounter(CounterMarker counter) {
        getFirstMapMarker().removeCounter(counter);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCounters(List<CounterMarker> counters) {
        getFirstMapMarker().removeCounters(counters);
    }

    /** {@inheritDoc} */
    @Override
    public void hover(UnfoldingMap map, int x, int y) {
        getFirstMapMarker().hover(map, x, y);
    }
}
