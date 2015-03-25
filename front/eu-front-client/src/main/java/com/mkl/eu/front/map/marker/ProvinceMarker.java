package com.mkl.eu.front.map.marker;

import com.mkl.eu.front.map.MapConfiguration;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** @author MKL. */
public class ProvinceMarker extends SimplePolygonMarker implements IMapMarker {
    /** Neighbours of the province. */
    private List<BorderMarker> neighbours = new ArrayList<>();
    /** Counters of the province. */
    private List<CounterMarker> counters = new ArrayList<>();
    /** Lower left location of the shape. */
    private Location topLeft;
    /** Upper right location of the shape. */
    private Location bottomRight;
    /** Center of the shape. */
    private Location center;
    /** Flag saying that the counters are being hovered. */
    private boolean hovered = false;

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

        center = GeoUtils.getCentroid(locations);
    }

    /** {@inheritDoc} */
    @Override
    public void draw(UnfoldingMap map) {
        Location bottomRightBorder = map.getBottomRightBorder();

        Location topLeftBorder = map.getTopLeftBorder();

        // map.getBottomRightBorder and map.getTopLeftBorder have inversed locations (x and y).
        if (bottomRightBorder.getLon() > topLeft.getLon() && topLeftBorder.getLon() < bottomRight.getLon()
                && bottomRightBorder.getLat() < topLeft.getLat() && topLeftBorder.getLat() > bottomRight.getLat()) {
            if ((MapConfiguration.isWithColor() || selected)) {
                super.draw(map);
            }


            PGraphics pg = map.mapDisplay.getOuterPG();

            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(center);
            float size = 0.08f * map.getZoom();
            for (int i = 0; i < counters.size(); i++) {
                CounterMarker counter = counters.get(i);
                if (hovered) {
                    pg.image(counter.getImage(), xy[0] + size * i * 2
                            , xy[1], size, size);
                    pg.stroke(255, 255, 0);
                    drawRectBorder(pg, xy[0] + size * i * 2
                            , xy[1], size, size, 2.5f);
                } else {
                    pg.image(counter.getImage(), xy[0] + size * i / 10
                            , xy[1] + size * i / 10, size, size);
                }
            }
            pg.popStyle();
        }
    }

    /**
     * Draw the borders of a rectangle.
     *
     * @param pg    the graphics.
     * @param x     X coordinate of the rectangle.
     * @param y     Y coordinate of the rectangle.
     * @param w     width of the rectangle.
     * @param h     height of the rectangle.
     * @param depth of the line.
     */
    private void drawRectBorder(PGraphics pg, float x, float y, float w, float h, float depth) {
        pg.strokeWeight(2 * depth);
        pg.line(x - depth, y - depth, x + w + depth, y - depth);
        pg.line(x + w + depth, y - depth, x + w + depth, y + h + depth);
        pg.line(x + w + depth, y + h + depth, x - depth, y + h + depth);
        pg.line(x - depth, y + h + depth, x - depth, y - depth);
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
     * @param checkX        The x position to check if inside.
     * @param checkY        The y position to check if inside.
     * @param vectors       The vectors of the polygon
     * @param interiorRings The vectors of the interior rings polygon
     * @return True if inside, false otherwise.
     */
    protected boolean isInside(float checkX, float checkY, List<? extends PVector> vectors, List<? extends List<? extends PVector>> interiorRings) {
        boolean inside = false;
        for (int i = 0, j = vectors.size() - 1; i < vectors.size(); j = i++) {
            PVector pi = vectors.get(i);
            PVector pj = vectors.get(j);
            boolean first = (((pi.y <= checkY) && (checkY < pj.y)) || ((pj.y <= checkY) && (checkY < pi.y)));
            boolean second = (checkX < (pj.x - pi.x) * (checkY - pi.y) / (pj.y - pi.y) + pi.x);
            if (first && second) {
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
     *
     * @param neighbour the neighbour to add.
     */
    public void addNeighbours(BorderMarker neighbour) {
        neighbours.add(neighbour);
    }

    /** {@inheritDoc} */
    @Override
    public List<CounterMarker> getCounters() {
        return counters;
    }

    /** {@inheritDoc} */
    @Override
    public void setCounters(List<CounterMarker> counters) {
        this.counters = counters;
    }

    /** {@inheritDoc} */
    @Override
    public void addCounter(CounterMarker counter) {
        counters.add(counter);
    }

    /** {@inheritDoc} */
    @Override
    public void addCounters(List<CounterMarker> counters) {
        this.counters.addAll(counters);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCounter(CounterMarker counter) {
        counters.remove(counter);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCounters(List<CounterMarker> counters) {
        this.counters.removeAll(counters);
    }

    /** {@inheritDoc} */
    @Override
    public void hover(UnfoldingMap map, int x, int y) {
        float[] xy = map.mapDisplay.getObjectFromLocation(center);
        float size = 0.08f * map.getZoom();

        hovered = x >= xy[0] && x <= xy[0] + size && y >= xy[1] && y <= xy[1] + size;
    }
}
