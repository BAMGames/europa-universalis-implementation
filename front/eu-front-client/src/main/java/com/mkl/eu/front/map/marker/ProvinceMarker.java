package com.mkl.eu.front.map.marker;

import com.mkl.eu.front.map.MapConfiguration;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author MKL. */
public class ProvinceMarker extends SimplePolygonMarker implements IMapMarker {
    /** Neighbours of the province. */
    private List<BorderMarker> neighbours = new ArrayList<>();
    /** Lower left location of the shape. */
    private Location topLeft;
    /** Upper right location of the shape. */
    private Location bottomRight;

    /**
     * Constructor.
     *
     * @param locations borders of the province.
     */
    public ProvinceMarker(List<Location> locations) {
        this(locations, null);
    }

    /**
     * Constructor.
     *
     * @param locations  borders of the province.
     * @param properties of the province.
     */
    public ProvinceMarker(List<Location> locations, HashMap<String, Object> properties) {
        super(locations, properties);

        computeExtremes();
    }

    /** Calculate the lower left and upper right locations of the province. */
    private void computeExtremes() {
        for (Location location : locations) {
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
        if ((MapConfiguration.isWithColor() || selected) &&
                bottomRightBorder.getLon() > topLeft.getLon() && topLeftBorder.getLon() < bottomRight.getLon()
                && bottomRightBorder.getLat() < topLeft.getLat() && topLeftBorder.getLat() > bottomRight.getLat()) {
            super.draw(map);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
        List<ScreenPosition> positions = new ArrayList<>();
        for (Location location : locations) {
            ScreenPosition pos = map.getScreenPosition(location);
            positions.add(pos);
        }

        List<List<ScreenPosition>> rings = new ArrayList<>();
        if (interiorRingLocationArray != null) {
            for (List<Location> ring : interiorRingLocationArray) {
                List<ScreenPosition> ringPositions = new ArrayList<>();

                for (Location location : ring) {
                    ScreenPosition pos = map.getScreenPosition(location);
                    ringPositions.add(pos);
                }

                if (!ringPositions.isEmpty()) {
                    rings.add(ringPositions);
                }
            }
        }

        return isInside(checkX, checkY, positions, rings);
    }

    /**
     * Checks whether the position is within the border of the vectors. Uses a polygon containment algorithm.
     * <p/>
     * This method is used for both ScreenPosition as well as Location checks.
     *
     * @param checkX  The x position to check if inside.
     * @param checkY  The y position to check if inside.
     * @param vectors The vectors of the polygon
     * @param vectors The vectors of the interior rings polygon
     * @return True if inside, false otherwise.
     */
    protected boolean isInside(float checkX, float checkY, List<? extends PVector> vectors, List<? extends List<? extends PVector>> interiorRings) {
        boolean inside = false;
        for (int i = 0, j = vectors.size() - 1; i < vectors.size(); j = i++) {
            PVector pi = vectors.get(i);
            PVector pj = vectors.get(j);
            if ((((pi.y <= checkY) && (checkY < pj.y)) || ((pj.y <= checkY) && (checkY < pi.y)))
                    && (checkX < (pj.x - pi.x) * (checkY - pi.y) / (pj.y - pi.y) + pi.x)) {
                inside = !inside;
            }
        }

        if (inside) {
            for (List<? extends PVector> ring : interiorRings) {
                if (isInside(checkX, checkY, ring)) {
                    inside = false;
                    break;
                }
            }
        }

        return inside;
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
