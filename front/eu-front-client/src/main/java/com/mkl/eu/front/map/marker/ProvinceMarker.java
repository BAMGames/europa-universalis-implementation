package com.mkl.eu.front.map.marker;

import com.mkl.eu.front.main.Mine;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;

import java.util.HashMap;
import java.util.List;

/**
 * @author MKL.
 */
public class ProvinceMarker extends SimplePolygonMarker {
    /** Lower left location of the shape. */
    private Location topLeft;
    /** Upper right location of the shape. */
    private Location bottomRight;
    /** Id of the province. */
    private String id;

    /**
     * Constructor.
     * @param locations borders of the province.
     */
    public ProvinceMarker(List<Location> locations, String id) {
        this(locations, null, id);
    }

    /**
     * Constructor.
     * @param locations borders of the province.
     * @param properties of the province.
     */
    public ProvinceMarker(List<Location> locations, HashMap<String, Object> properties, String id) {
        super(locations, properties);

        this.id = id;

        computeExtremes();
    }

    /**
     * Calculate the lower left and upper right locations of the province.
     */
    private void computeExtremes() {
        for (Location location: locations) {
            if (topLeft == null) {
                topLeft = new Location(location);
            } else {
                if (location.getLon() < topLeft.getLon()) {
                    topLeft.setLon(location.getLon());
                }
                if (location.getLat() > topLeft.getLat()) {
                    topLeft.setLat(location.getLat());
                }
            }
            if (bottomRight == null) {
                bottomRight = new Location(location);
            } else {
                if (location.getLon() > bottomRight.getLon()) {
                    bottomRight.setLon(location.getLon());
                }
                if (location.getLat() < bottomRight.getLat()) {
                    bottomRight.setLat(location.getLat());
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void draw(UnfoldingMap map) {
        Location bottomRightBorder = map.getBottomRightBorder();

        Location topLeftBorder = map.getTopLeftBorder();

        // map.getBottomRightBorder and map.getTopLeftBorder have inversed locations (x and y).
        if (bottomRightBorder.getLon() > topLeft.getLon() && topLeftBorder.getLon() < bottomRight.getLon()
                && bottomRightBorder.getLat() < topLeft.getLat() && topLeftBorder.getLat() > bottomRight.getLat()
                && (Mine.isWithColor() || selected)) {
            super.draw(map);
        }
    }
}
