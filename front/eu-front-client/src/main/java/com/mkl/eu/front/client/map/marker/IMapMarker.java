package com.mkl.eu.front.client.map.marker;

import de.fhpotsdam.unfolding.UnfoldingMap;

import java.util.List;

/**
 * Interface of all the map markers.
 *
 * @author MKL
 */
public interface IMapMarker {
    /** Size of a counter in the Map frame. */
    float COUNTER_SIZE = 0.1725f;
    /** Name of the marker property for the terrain. */
    String PROP_TERRAIN = "terrain";
    /** Name of the marker property for the rotw flag. */
    String PROP_ROTW = "rotw";

    // Properties for European provinces
    /** Name of the marker property for the income. */
    String PROP_EU_INCOME = "income";
    /** Name of the marker property for the owner. */
    String PROP_EU_OWNER = "owner";
    /** Name of the marker property for the x coordinate fortress flag. */
    String PROP_EU_X_FORTRESS = "xFortress";
    /** Name of the marker property for the y coordinate fortress flag. */
    String PROP_EU_Y_FORTRESS = "yFortress";
    /** Name of the marker property for the fortress level. */
    String PROP_EU_FORTRESS = "fortress";
    /** Name of the marker property for the capital flag. */
    String PROP_EU_CAPITAL = "capital";
    /** Name of the marker property for the x coordinate port flag. */
    String PROP_EU_X_PORT = "xPort";
    /** Name of the marker property for the y coordinate port flag. */
    String PROP_EU_Y_PORT = "yPort";
    /** Name of the marker property for the port flag. */
    String PROP_EU_PORT = "port";
    /** Name of the marker property for the arsenal flag. */
    String PROP_EU_ARSENAL = "arsenal";
    /** Name of the marker property for the praesidiable flag. */
    String PROP_EU_PREASIDIABLE = "praesidiable";
    /** Name of the marker property for the metadata. */
    String PROP_EU_METADATA = "metadata";

    /** Name of the marker property for the region. */
    String PROP_ROTW_REGION = "region";
    /** Name of the marker property for the fortress level. */
    String PROP_ROTW_FORTRESS = "fortress";
    /** Name of the marker property for the metadata. */
    String PROP_ROTW_METADATA = "metadata";

    // Properties for sea zones
    /** Name of the marker property for the difficulty. */
    String PROP_SEA_DIFFICULTY = "difficulty";
    /** Name of the marker property for the penalty. */
    String PROP_SEA_PENALTY = "penalty";

    // Properties for trade zones
    /** Name of the marker property for the type. */
    String PROP_TZ_TYPE = "type";
    /** Name of the marker property for the country. */
    String PROP_TZ_COUNTRY = "country";
    /** Name of the marker property for the monopoly. */
    String PROP_TZ_MONOPOLY = "monopoly";
    /** Name of the marker property for the presence. */
    String PROP_TZ_PRESENCE = "presence";

    /**
     * Sets the highlightColor.
     *
     * @param highlightColor the highlightColor to set.
     */
    void setHighlightColor(int highlightColor);

    /** @return the neighbours. */
    List<BorderMarker> getNeighbours();

    /**
     * Add a neighbour.
     *
     * @param neighbour the neighbour to add.
     */
    void addNeighbours(BorderMarker neighbour);

    /** @return the id; */
    String getId();

    /** @return the stacks. */
    List<StackMarker> getStacks();

    /**
     * Sets the stacks to the marker.
     *
     * @param stacks the stacks to set.
     */
    void setStacks(List<StackMarker> stacks);

    /**
     * Add a stack to the marker.
     *
     * @param stack the stack to add.
     */
    void addStack(StackMarker stack);

    /**
     * Remove a stack to the marker.
     *
     * @param stack the stack to remove.
     */
    void removeStack(StackMarker stack);

    /**
     * Returns the stack at the x/y coordinates, <code>null</code> if none.
     *
     * @param map the map.
     * @param x   X coordinate.
     * @param y   Y coordinate.
     * @return the stack at the x/y coordinates, <code>null</code> if none.
     */
    StackMarker getStack(UnfoldingMap map, int x, int y);

    /**
     * Draws this marker.
     *
     * @param map           The map to draw on.
     * @param stacksToIgnore stacks not to draw.
     */
    void draw(UnfoldingMap map, List<StackMarker> stacksToIgnore);

    /** @return In case of Provinces in various pieces, returns the MultiProvinceMarker parent of all. */
    IMapMarker getParent();

    /** @param parent the parent to set.. */
    void setParent(IMapMarker parent);

    /**
     * Returns the country owning this province.
     *
     * @return the country owning this province.
     */
    String getOwner();

    /**
     * Returns the country controlling this province.
     *
     * @return the country controlling this province.
     */
    String getController();

    /**
     * @return whether the province is a port or no.
     */
    boolean isPort();

    /**
     * @return the level of the natural fortress in the province.
     */
    int getFortressLevel();

    /**
     * @return whether the province is a tradezone or no.
     */
    boolean isTradeZone();
}
