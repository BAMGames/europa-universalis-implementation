package com.mkl.eu.front.map.handler;

import com.mkl.eu.front.map.marker.BorderMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.MyMarkerManager;
import com.mkl.eu.front.map.marker.StackMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Forking of {@link de.fhpotsdam.unfolding.interactions.MouseHandler} because it was not designed to be derivated.
 * </p>
 * <p>
 * Performs the following:
 * <ul>
 *     <li>Zoom on wheel (like MouseHandler)</li>
 *     <li>Zoom and Pan on double click (like MouseHandler)</li>
 *     <li>Pan on Drag (like MouseHandler)</li>
 *     <li>Province selection on click</li>
 *     <li>Stack highlight on hover</li>
 *     <li>Stack moving on drag&drop</li>
 * </ul>
 * </p>
 *
 * @author MKL
 */
public class MapMouseHandler extends MapEventBroadcaster {

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------
    /** mouseX. */
    private int mouseX;
    /** mouseY. */
    private int mouseY;
    /** pmouseX. */
    private int pmouseX;
    /** pmouseY. */
    private int pmouseY;
    /** emouseX. */
    private int emouseX;
    /** emouseY. */
    private int emouseY;
    /** firstMouse. */
    private boolean firstMouse;
    /** mouseButton. */
    private int mouseButton;
    /** mouseEvent. */
    private MouseEvent mouseEvent;

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps One or more maps.
     */
    public MapMouseHandler(PApplet p, UnfoldingMap... maps) {
        this(p, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p    The PApplet.
     * @param maps A list of maps.
     */
    public MapMouseHandler(PApplet p, List<UnfoldingMap> maps) {
        super(maps);

        p.registerMethod("mouseEvent", this);
    }

    /**
     * Method called when mouse is clicked. Will select an eventual province.
     * If double click, performs a zoom in.
     */
    public void mouseClicked() {
        for (UnfoldingMap map : maps) {
            if (map.isHit(mouseX, mouseY)) {
                Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(mouseX, mouseY);
                if (marker != null && map.getDefaultMarkerManager() instanceof MyMarkerManager) {
                    ((MyMarkerManager) map.getDefaultMarkerManager()).select(marker);
                }
                if (mouseButton == PConstants.LEFT && mouseEvent.getCount() == 2) {

                    // Pan + Zoom (order is important)
                    PanMapEvent panMapEvent = new PanMapEvent(this, map.getId());
                    Location location = map.getLocation(mouseX, mouseY);
                    panMapEvent.setToLocation(location);
                    eventDispatcher.fireMapEvent(panMapEvent);

                    ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL, 1);
                    zoomMapEvent.setTransformationCenterLocation(location);
                    eventDispatcher.fireMapEvent(zoomMapEvent);
                }
            }
        }
    }

    /**
     * Method called when mouse wheel is used. Will zoom out or in according to the wheel direction.
     * @param delta wheel direction.
     */
    public void mouseWheel(float delta) {
        for (UnfoldingMap map : maps) {
            if (map.isHit(mouseX, mouseY)) {
                // log.debug("mouse: fire zoomBy for " + map.getId());

                ZoomMapEvent zoomMapEvent = new ZoomMapEvent(this, map.getId(), ZoomMapEvent.ZOOM_BY_LEVEL);

                // Use location as zoom center, so listening maps can zoom correctly
                Location location = map.getLocation(mouseX, mouseY);
                zoomMapEvent.setTransformationCenterLocation(location);

                // Zoom in or out
                if (delta < 0) {
                    zoomMapEvent.setZoomLevelDelta(1);
                } else if (delta > 0) {
                    zoomMapEvent.setZoomLevelDelta(-1);
                }

                eventDispatcher.fireMapEvent(zoomMapEvent);
            }
        }
    }

    /**
     * Method called when mouse is dragged. Will move an eventual dragged stack.
     * If not, will move the map.
     */
    public void mouseDragged() {
        for (UnfoldingMap map : maps) {
            if (map.isHit(mouseX, mouseY)) {
                if (mouseButton == PConstants.LEFT) {
                    Location newLocation = map.getLocation(mouseX, mouseY);
                    if (map.getDefaultMarkerManager() instanceof MyMarkerManager &&
                            ((MyMarkerManager) map.getDefaultMarkerManager()).getDragged() != null) {
                        ((MyMarkerManager) map.getDefaultMarkerManager()).setDragLocation(newLocation);
                    } else {

                        // log.debug("mouse: fire panTo for " + map.getId());

                        // Pan between two locations, so other listening maps can pan correctly

                        Location oldLocation = map.getLocation(pmouseX, pmouseY);

                        PanMapEvent panMapEvent = new PanMapEvent(this, map.getId(), PanMapEvent.PAN_BY);
                        panMapEvent.setFromLocation(oldLocation);
                        panMapEvent.setToLocation(newLocation);
                        eventDispatcher.fireMapEvent(panMapEvent);
                    }
                }
            }
        }
    }

    /**
     * Method called when mouse is moved. Will highlight a hovering stack.
     */
    public void mouseMoved() {
        for (UnfoldingMap map : maps) {
            if (map.isHit(mouseX, mouseY)) {
                Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(mouseX, mouseY);
                if (marker != null && marker instanceof IMapMarker) {
                    ((IMapMarker) marker).hover(map, mouseX, mouseY);
                }
            }
        }
    }

    /**
     * Method called when the mouse is released.
     * Will empty the dragged stack and move it if possible.
     */
    public void mouseReleased() {
        for (UnfoldingMap map : maps) {
            if (map.getDefaultMarkerManager() instanceof MyMarkerManager) {
                if (((MyMarkerManager) map.getDefaultMarkerManager()).getDragged() != null) {
                    Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(mouseX, mouseY);
                    if (marker instanceof IMapMarker
                            && isNeighbour(((MyMarkerManager) map.getDefaultMarkerManager()).getDragged().getProvince(), (IMapMarker) marker)) {
                        ((IMapMarker) marker).addStack(((MyMarkerManager) map.getDefaultMarkerManager()).getDragged());
                    }
                    ((MyMarkerManager) map.getDefaultMarkerManager()).setDragged(null);
                    ((MyMarkerManager) map.getDefaultMarkerManager()).setDragLocation(null);
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the provinces are neighbours.
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

    /**
     * Methode called when the mouse is pressed to select a stack.
     */
    public void mousePressed() {
        for (UnfoldingMap map : maps) {
            if (map.isHit(mouseX, mouseY)) {
                Marker marker = map.getDefaultMarkerManager().getFirstHitMarker(mouseX, mouseY);
                if (marker != null && map.getDefaultMarkerManager() instanceof MyMarkerManager
                        && marker instanceof IMapMarker) {
                    StackMarker stack = ((IMapMarker) marker).getStack(map, mouseX, mouseY);
                    ((MyMarkerManager) map.getDefaultMarkerManager()).setDragged(stack);
                }
            }
        }
    }

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------

    /**
     * Method called by the pApplet when a mouse action is performed.
     * @param event mouseEvent.
     */
    public void mouseEvent(MouseEvent event) {
        int action = event.getAction();
        mouseEvent = event;

        if ((action == MouseEvent.DRAG) || (action == MouseEvent.MOVE)) {
            pmouseX = emouseX;
            pmouseY = emouseY;
            mouseX = event.getX();
            mouseY = event.getY();
        }

        mouseButton = event.getButton();

        if (firstMouse) {
            pmouseX = mouseX;
            pmouseY = mouseY;
            firstMouse = false;
        }

        mousePerform(event, action);

        if ((action == MouseEvent.DRAG) || (action == MouseEvent.MOVE)) {
            emouseX = mouseX;
            emouseY = mouseY;
        }
    }

    /**
     * Perform the matching method according to the action.
     * @param event mouseEvent to spread to the matching method.
     * @param action of the mouse
     */
    private void mousePerform(MouseEvent event, int action) {
        switch (action) {
            case MouseEvent.CLICK:
                mouseClicked();
                break;
            case MouseEvent.DRAG:
                mouseDragged();
                break;
            case MouseEvent.MOVE:
                mouseMoved();
                break;
            case MouseEvent.WHEEL:
                mouseWheel(event.getCount());
                break;
            case MouseEvent.RELEASE:
                mouseReleased();
                break;
            case MouseEvent.PRESS:
                mousePressed();
                break;
            default:
                break;
        }
    }
}
