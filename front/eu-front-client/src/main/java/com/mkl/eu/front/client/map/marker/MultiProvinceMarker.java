package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Province marker that consists of various pieces.
 *
 * @author MKL
 */
public class MultiProvinceMarker extends MultiMarker implements IMapMarker {
    /** Neighbours of the province. */
    private List<BorderMarker> neighbours = new ArrayList<>();
    /** Parent. */
    private IMapMarker parent;

    /**
     * Constructor.
     *
     * @param markers sons of the multi provinc emarker.
     */
    public MultiProvinceMarker(List<Marker> markers) {
        this.markers = markers;
        if (markers != null) {
            for (Marker marker : markers) {
                if (marker instanceof IMapMarker) {
                    ((IMapMarker) marker).setParent(this);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void draw(UnfoldingMap map, List<StackMarker> stacksToIgnore) {
        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, stacksToIgnore);
            } else {
                marker.draw(map);
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
    public void setHighlightColor(int highlightColor) {
        for (Marker subMarker : getMarkers()) {
            if (subMarker instanceof AbstractMarker) {
                ((AbstractMarker) subMarker).setHighlightColor(highlightColor);
            }
        }
    }

    /** @return the first IMapMarker of the markers. */
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
    public List<StackMarker> getStacks() {
        return getFirstMapMarker().getStacks();
    }

    /** {@inheritDoc} */
    @Override
    public void setStacks(List<StackMarker> stacks) {
        getFirstMapMarker().setStacks(stacks);
    }

    /** {@inheritDoc} */
    @Override
    public void addStack(StackMarker stack) {
        getFirstMapMarker().addStack(stack);
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getStack(UnfoldingMap map, int x, int y) {
        return getFirstMapMarker().getStack(map, x, y);
    }

    /** {@inheritDoc} */
    @Override
    public void removeStack(StackMarker stack) {
        getFirstMapMarker().removeStack(stack);
    }

    /** {@inheritDoc} */
    @Override
    public String getOwner() {
        return getFirstMapMarker().getOwner();
    }

    /** {@inheritDoc} */
    @Override
    public String getController() {
        return getFirstMapMarker().getController();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPort() {
        return getFirstMapMarker().isPort();
    }

    /** {@inheritDoc} */
    @Override
    public int getFortressLevel() {
        return getFirstMapMarker().getFortressLevel();
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
                }
                properties.put(key, value);
            }
        }
        super.setProperties(properties);
    }
}
