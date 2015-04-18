package com.mkl.eu.front.map.handler.mouse;

import com.mkl.eu.front.map.marker.BorderMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.MyMarkerManager;
import com.mkl.eu.front.map.marker.StackMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Forking of {@link de.fhpotsdam.unfolding.interactions.MouseHandler} because it was not designed to be derivated.
 * </p>
 * <p>
 * Performs the following:
 * <ul>
 * <li>Zoom on wheel (like MouseHandler)</li>
 * <li>Zoom and Pan on double click (like MouseHandler)</li>
 * <li>Pan on Drag (like MouseHandler)</li>
 * <li>Province selection on click</li>
 * <li>Stack highlight on hover</li>
 * <li>Stack moving on drag&drop</li>
 * <li>Province contextual menu on right click</li>
 * </ul>
 * </p>
 *
 * @author MKL
 */
public class MapMouseHandler extends AbstractDragDropMenuMouseHandler<StackMarker, IMapMarker, Object, MyMarkerManager> {

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p             The PApplet.
     * @param markerManager The marker manager.
     * @param maps          One or more maps.
     */
    public MapMouseHandler(PApplet p, MyMarkerManager markerManager, UnfoldingMap... maps) {
        this(p, markerManager, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p             The PApplet.
     * @param markerManager The marker manager.
     * @param maps          A list of maps.
     */
    public MapMouseHandler(PApplet p, MyMarkerManager markerManager, List<UnfoldingMap> maps) {
        super(p, markerManager, maps);
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseClicked() {
        if (!super.mouseClicked()) {
            for (UnfoldingMap map : maps) {
                if (map.isHit(getMouseX(), getMouseY())) {
                    if (getMouseButton() == PConstants.LEFT) {
                        if (map.getDefaultMarkerManager() instanceof MyMarkerManager) {
                            Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(getMouseX(), getMouseY());
                            if (marker != null) {
                                ((MyMarkerManager) map.getDefaultMarkerManager()).select(marker);
                            }
                        }
                    }
                    if (getMouseButton() == PConstants.LEFT && getMouseEvent().getCount() == 2) {

                        // Pan + Zoom (order is important)
                        PanMapEvent panMapEvent = new PanMapEvent(this, map.getId());
                        Location location = map.getLocation(getMouseX(), getMouseY());
                        panMapEvent.setToLocation(location);
                        eventDispatcher.fireMapEvent(panMapEvent);

                        ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL, 1);
                        zoomMapEvent.setTransformationCenterLocation(location);
                        eventDispatcher.fireMapEvent(zoomMapEvent);
                    }
                }
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseWheel(float delta) {
        boolean stop = super.mouseWheel(delta);
        if (!stop) {
            for (UnfoldingMap map : maps) {
                if (map.isHit(getMouseX(), getMouseY())) {
                    // log.debug("mouse: fire zoomBy for " + map.getId());

                    ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL);

                    // Use location as zoom center, so listening maps can zoom correctly
                    Location location = map.getLocation(getMouseX(), getMouseY());
                    zoomMapEvent.setTransformationCenterLocation(location);

                    // Zoom in or out
                    if (delta < 0) {
                        zoomMapEvent.setZoomLevelDelta(1);
                    } else if (delta > 0) {
                        zoomMapEvent.setZoomLevelDelta(-1);
                    }

                    eventDispatcher.fireMapEvent(zoomMapEvent);

                    stop = true;
                }
            }
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseDragged() {
        boolean stop = super.mouseDragged();
        if (!stop) {
            for (UnfoldingMap map : maps) {
                if (map.isHit(getMouseX(), getMouseY())) {
                    if (getMouseButton() == PConstants.LEFT) {
                        // log.debug("mouse: fire panTo for " + map.getId());

                        // Pan between two locations, so other listening maps can pan correctly

                        Location oldLocation = map.getLocation(getPmouseX(), getPmouseY());

                        PanMapEvent panMapEvent = new PanMapEvent(this, map.getId(), PanMapEvent.PAN_BY);
                        panMapEvent.setFromLocation(oldLocation);
                        panMapEvent.setToLocation(map.getLocation(getMouseX(), getMouseY()));
                        eventDispatcher.fireMapEvent(panMapEvent);
                        stop = true;
                    }
                }
            }
        }
        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseMoved() {
        boolean stop = super.mouseMoved();
        if (!stop) {
            stop = getComponent().hover(getMouseX(), getMouseY());
            if (!stop) {
                for (UnfoldingMap map : maps) {
                    if (map.isHit(getMouseX(), getMouseY())) {
                        Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(getMouseX(), getMouseY());
                        if (marker != null && marker instanceof IMapMarker) {
                            ((IMapMarker) marker).hover(map, getMouseX(), getMouseY());
                            stop = true;
                        }
                    }
                }
            }
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    protected void doAfterRelease(StackMarker dragged, IMapMarker drop) {
        if (isNeighbour(dragged.getProvince(), drop)) {
            drop.addStack(dragged);
        }
    }

    /**
     * Returns <code>true</code> if the provinces are neighbours.
     *
     * @param provinceA the first province.
     * @param provinceB the second province.
     * @return <code>true</code> if the provinces are neighbours.
     */
    private boolean isNeighbour(IMapMarker provinceA, IMapMarker provinceB) {
        boolean isNeighbour = false;

        if (provinceA != null && provinceB != null) {
            if (provinceA.getNeighbours() != null) {
                for (BorderMarker neighbour : provinceA.getNeighbours()) {
                    if (neighbour.getProvince() == provinceB) {
                        isNeighbour = true;
                        break;
                    }
                }
            }
        }

        return isNeighbour;
    }
}
