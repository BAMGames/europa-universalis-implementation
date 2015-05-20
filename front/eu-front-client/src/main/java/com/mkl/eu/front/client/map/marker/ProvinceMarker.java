package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.front.client.map.MapConfiguration;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePolygonMarker;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.apache.commons.lang3.StringUtils;
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
    private List<StackMarker> stacks = new ArrayList<>();
    /** Stacks that are in special locations (fortress and praesidios mostly). */
    /** Stacks on the fortress (a better fortress and besieged units in it). */
    private List<StackMarker> stacksFortress = new ArrayList<>();
    /** Stacks on the port (a praesidio, besieged units in it, fleet unit in or besieging port). */
    private List<StackMarker> stacksPort = new ArrayList<>();
    /** Lower left location of the shape. X and Y are inversed due to unfolding bug. */
    private Location topLeft;
    /** Upper right location of the shape. X and Y are inversed due to unfolding bug. */
    private Location bottomRight;
    /** Center of the shape. */
    private Location center;
    /** Parent. */
    private IMapMarker parent;

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
    public void draw(UnfoldingMap map, List<StackMarker> stacksToIgnore) {
        Location bottomRightBorder = map.getBottomRightBorder();

        Location topLeftBorder = map.getTopLeftBorder();

        // map.getBottomRightBorder and map.getTopLeftBorder have inversed locations (x and y).
        if (bottomRightBorder.getLon() > topLeft.getLon() && topLeftBorder.getLon() < bottomRight.getLon()
                && bottomRightBorder.getLat() < topLeft.getLat() && topLeftBorder.getLat() > bottomRight.getLat()) {
            if ((MapConfiguration.isWithColor() || selected)) {
                super.draw(map);
            }

            if (id.equals("eAndalucia")) {
                int a = 1;
            }

            PGraphics pg = map.mapDisplay.getOuterPG();

            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(center);
            float relativeSize = Math.abs(xy[0] - map.mapDisplay.getObjectFromLocation(new Location(center.getLat()
                    + COUNTER_SIZE, center.getLon() + COUNTER_SIZE))[0]);

            if (getProperties().get(PROP_TERRAIN) == null
                    && (getId().startsWith("ZM") || getId().startsWith("ZP"))) {
                drawStacksInCircle(stacks, xy, relativeSize, pg, stacksToIgnore);
            } else {
                drawStacks(stacks, xy, relativeSize, pg, stacksToIgnore);
            }

            Location location = getFortressLocation();
            if (location != null) {
                xy = map.mapDisplay.getObjectFromLocation(location);
                drawStacks(stacksFortress, xy, relativeSize, pg, stacksToIgnore);
            }

            location = getPortLocation();
            if (location != null) {
                xy = map.mapDisplay.getObjectFromLocation(location);
                drawStacks(stacksPort, xy, relativeSize, pg, stacksToIgnore);
            }

            pg.popStyle();
        }
    }

    /**
     * Draw stacks at a starting location.
     *
     * @param stacks         stacks to draw.
     * @param xy             starting location.
     * @param relativeSize   size of the counters.
     * @param pg             graphics.
     * @param stacksToIgnore stacks not to draw.
     */
    protected void drawStacks(List<StackMarker> stacks, float[] xy, float relativeSize, PGraphics pg, List<StackMarker> stacksToIgnore) {
        for (int i = 0; i < stacks.size(); i++) {
            // The stack being dragged or hovered is drawn by the MarkerManager.
            if (stacksToIgnore.contains(stacks.get(i))) {
                continue;
            }
            for (int j = 0; j < stacks.get(i).getCounters().size(); j++) {
                CounterMarker counter = stacks.get(i).getCounters().get(j);

                float x0 = xy[0] - relativeSize * (stacks.size()) / 2;
                pg.image(counter.getImage(), x0 + relativeSize * j / 10 + relativeSize * i
                        , xy[1] + relativeSize * (j - 5) / 10, relativeSize, relativeSize);
            }
        }
    }

    /**
     * Draw stacks in circle around a location.
     *
     * @param stacks         stacks to draw.
     * @param xy             center location.
     * @param relativeSize   size of the counters.
     * @param pg             graphics.
     * @param stacksToIgnore stacks not to draw.
     */
    protected void drawStacksInCircle(List<StackMarker> stacks, float[] xy, float relativeSize, PGraphics pg, List<StackMarker> stacksToIgnore) {
        for (int i = 0; i < stacks.size(); i++) {
            // The stack being dragged or hovered is drawn by the MarkerManager.
            if (stacksToIgnore.contains(stacks.get(i))) {
                continue;
            }
            for (int j = 0; j < stacks.get(i).getCounters().size(); j++) {
                CounterMarker counter = stacks.get(i).getCounters().get(j);

                float degree = 360 * i / stacks.size();
                float x0 = (float) (xy[0] + Math.sin(Math.toRadians(degree)) * relativeSize);
                float y0 = (float) (xy[1] - Math.cos(Math.toRadians(degree)) * relativeSize);
                pg.image(counter.getImage(), x0 + relativeSize * j / 10
                        , y0 + relativeSize * j / 10, relativeSize, relativeSize);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public IMapMarker getParent() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(IMapMarker parent) {
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInside(UnfoldingMap map, float checkX, float checkY) {
        ScreenPosition tl = map.getScreenPosition(topLeft);
        ScreenPosition br = map.getScreenPosition(bottomRight);
        // fast response for false using the smaller rectangle possible to size the shape.
        if (tl.x > checkX || br.x < checkX
                || tl.y > checkY || br.y < checkY) {
            return false;
        }

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

    /** @return the stacks. */
    @Override
    public List<StackMarker> getStacks() {
        return stacks;
    }

    /** {@inheritDoc} */
    @Override
    public void setStacks(List<StackMarker> stacks) {
        this.stacks = stacks;
    }

    /** {@inheritDoc} */
    @Override
    public void addStack(StackMarker stack) {
        if (stack.getProvince() != null) {
            stack.getProvince().removeStack(stack);
        }

        if (getFortressLocation() != null && isFortressStack(stack)) {
            stacksFortress.add(stack);
        } else if (getPortLocation() != null && isPortStack(stack)) {
            stacksPort.add(stack);
        } else {
            stacks.add(stack);
        }

        stack.setProvince(getRealStackOwner());
    }

    /** {@inheritDoc} */
    @Override
    public void removeStack(StackMarker stack) {
        stack.setProvince(null);

        stacksFortress.remove(stack);
        stacksPort.remove(stack);
        stacks.remove(stack);
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getStack(UnfoldingMap map, int x, int y) {
        if (getProperties().get(PROP_TERRAIN) == null
                && (getId().startsWith("ZM") || getId().startsWith("ZP"))) {
            return null;
        }

        float[] xy = map.mapDisplay.getObjectFromLocation(center);
        float relativeSize = Math.abs(xy[0] - map.mapDisplay.getObjectFromLocation(new Location(center.getLat()
                + COUNTER_SIZE, center.getLon() + COUNTER_SIZE))[0]);

        StackMarker stack = getStack(stacks, xy, x, y, relativeSize);
        Location location = getFortressLocation();
        if (stack == null && location != null) {
            xy = map.mapDisplay.getObjectFromLocation(location);
            stack = getStack(stacksFortress, xy, x, y, relativeSize);
        }
        location = getPortLocation();
        if (stack == null && location != null) {
            xy = map.mapDisplay.getObjectFromLocation(location);
            stack = getStack(stacksPort, xy, x, y, relativeSize);
        }

        return stack;
    }

    /**
     * Returns a stack within a list given a starting location and coordinates.
     *
     * @param stacks       existing stacks.
     * @param xy           location where the stacks are drawn.
     * @param x            X coordinate.
     * @param y            Y coordinate.
     * @param relativeSize size of counters.
     * @return a stack at given coordinates.
     */
    protected StackMarker getStack(List<StackMarker> stacks, float[] xy, int x, int y, float relativeSize) {
        StackMarker stack = null;

        float x0 = xy[0] - relativeSize * (stacks.size()) / 2;

        if (x >= x0 && x < x0 + stacks.size() * relativeSize && y >= xy[1] - relativeSize / 2 && y < xy[1] + relativeSize / 2) {
            int index = (int) ((x - x0) / relativeSize);
            stack = stacks.get(index);
        }

        return stack;
    }

    /** @return the real stack owner (the MultiProvinceMarker if it is the case). */
    private IMapMarker getRealStackOwner() {
        IMapMarker province = this;
        if (parent != null) {
            province = parent;
        }

        return province;
    }

    /**
     * Returns <code>true</code> if the stack should be drawn on the fortress, <code>false</code> otherwise.
     *
     * @param stack to test.
     * @return <code>true</code> if the stack should be drawn on the fortress, <code>false</code> otherwise.
     */
    private boolean isFortressStack(StackMarker stack) {
        if (stack != null && stack.getCounters() != null) {
            for (CounterMarker counter : stack.getCounters()) {
                switch (counter.getType()) {
                    case FORTRESS_1:
                    case FORTRESS_2:
                    case FORTRESS_3:
                    case FORTRESS_4:
                    case FORTRESS_5:
                        return StringUtils.equals(counter.getCountry(), getOwner());
                }
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the stack should be drawn on the port, <code>false</code> otherwise.
     *
     * @param stack to test.
     * @return <code>true</code> if the stack should be drawn on the port, <code>false</code> otherwise.
     */
    private boolean isPortStack(StackMarker stack) {
        if (stack != null && stack.getCounters() != null) {
            for (CounterMarker counter : stack.getCounters()) {
                switch (counter.getType()) {
                    case FORTRESS_1:
                    case FORTRESS_2:
                    case FORTRESS_3:
                    case FORTRESS_4:
                    case FORTRESS_5:
                        return !StringUtils.equals(counter.getCountry(), getOwner());
                    case FLEET_MINUS:
                    case FLEET_PLUS:
                    case NAVAL_DETACHMENT:
                    case NAVAL_DETACHMENT_EXPLORATION:
                    case NAVAL_GALLEY:
                    case NAVAL_TRANSPORT:
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the country owning this province.
     *
     * @return the country owning this province.
     */
    private String getOwner() {
        for (StackMarker stack : stacks) {
            for (CounterMarker marker : stack.getCounters()) {
                if (marker.getType() == CounterFaceTypeEnum.OWN) {
                    return marker.getCountry();
                }
            }
        }

        return (String) getProperties().get(PROP_EU_OWNER);
    }

    /**
     * @return the location of the fortress.
     */
    protected Location getFortressLocation() {
        Location location = null;
        Object x = getProperty(PROP_EU_X_FORTRESS);
        Object y = getProperty(PROP_EU_Y_FORTRESS);
        if (x instanceof Double && y instanceof Double) {
            // Thanks for the x/y inversion in location...
            location = new Location((Double) y, (Double) x);
        }

        return location;
    }

    /**
     * @return the location of the port.
     */
    protected Location getPortLocation() {
        Location location = null;
        Object x = getProperty(PROP_EU_X_PORT);
        Object y = getProperty(PROP_EU_Y_PORT);
        if (x instanceof Double && y instanceof Double) {
            // Thanks for the x/y inversion in location...
            location = new Location((Double) y, (Double) x);
        }

        return location;
    }

    /** {@inheritDoc} */
    @Override
    public void setProperties(HashMap<String, Object> props) {
        HashMap<String, Object> properties = null;
        if (props != null) {
            properties = new HashMap<>();
            for (String key : props.keySet()) {
                Object value = props.get(key);

                if (StringUtils.equals(PROP_TERRAIN, key)) {
                    TerrainEnum terrain = null;
                    try {
                        terrain = TerrainEnum.valueOf((String) value);
                    } catch (Exception e) {
                        // null value if not parsable
                    }
                    value = terrain;
                } else if (StringUtils.equals(PROP_EU_X_FORTRESS, key)
                        || StringUtils.equals(PROP_EU_Y_FORTRESS, key)
                        || StringUtils.equals(PROP_EU_X_PORT, key)
                        || StringUtils.equals(PROP_EU_Y_PORT, key)) {
                    Double number = null;
                    try {
                        number = Double.parseDouble((String) value);
                    } catch (Exception e) {
                        // null value if not parsable
                    }
                    value = number;
                }
                properties.put(key, value);
            }
        }
        super.setProperties(properties);
    }
}
