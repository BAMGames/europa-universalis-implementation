package com.mkl.eu.front.map.marker;

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

    /**
     * Constructor.
     * @param markers sons of the multi provinc emarker.
     */
    public MultiProvinceMarker (List<Marker> markers) {
        this.markers = markers;
    }

    /** {@inheritDoc} */
    @Override
    public void setHighlightColor(int highlightColor) {
        for (Marker subMarker: getMarkers()) {
            if (subMarker instanceof AbstractMarker) {
                ((AbstractMarker)subMarker).setHighlightColor(highlightColor);
            }
        }
    }

    /** @return the neighbours. */
    public List<BorderMarker> getNeighbours() {
        return neighbours;
    }

    /**
     * Add a neighbour.
     * @param neighbour the neighbour to add.
     */
    public void addNeighbours(BorderMarker neighbour) {
        neighbours.add(neighbour);
    }
}
