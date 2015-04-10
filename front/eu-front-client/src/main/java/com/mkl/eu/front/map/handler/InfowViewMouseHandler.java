package com.mkl.eu.front.map.handler;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.front.main.InfoView;
import com.mkl.eu.front.map.marker.CounterMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.StackMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class InfowViewMouseHandler extends MapEventBroadcaster {
    /** Small map showing the overview, i.e. the world. */
    private InfoView infoView;

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------
    /** mouseX. */
    private int mouseX;
    /** mouseY. */
    private int mouseY;
    /** firstMouse. */
    private boolean firstMouse;
    /** mouseButton. */
    private int mouseButton;

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     One or more maps.
     */
    public InfowViewMouseHandler(PApplet p, InfoView infoView, UnfoldingMap... maps) {
        this(p, infoView, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     A list of maps.
     */
    public InfowViewMouseHandler(PApplet p, InfoView infoView, List<UnfoldingMap> maps) {
        super(maps);

        this.infoView = infoView;

        p.registerMethod("mouseEvent", this);
    }

    /**
     * Methode called when the mouse is pressed to select a stack.
     */
    public void mousePressed() {
        if (infoView.isHit(mouseX, mouseY)) {
            CounterMarker marker = infoView.getCounter(mouseX, mouseY);
            infoView.setDragged(marker);
        }
    }

    /**
     * Method called when mouse is dragged. Will move an eventual dragged stack.
     * If not, will move the map.
     */
    public void mouseDragged() {
        if (infoView.isHit(mouseX, mouseY)) {
            if (mouseButton == PConstants.LEFT) {
                Location newLocation = new Location(mouseX, mouseY);
                if (infoView.getDragged() != null) {
                    infoView.setDragLocation(newLocation);
                }
            }
        }
    }

    /**
     * Method called when the mouse is released.
     * Will empty the dragged stack and move it if possible.
     */
    public void mouseReleased() {
        if (infoView.getDragged() != null) {
            StackMarker stack = infoView.getStack(mouseX, mouseY);
            if (stack == null) {
                stack = new StackMarker(new Stack(), (IMapMarker) infoView.getSelected());
                ((IMapMarker) infoView.getSelected()).addStack(stack);
            }
            StackMarker oldStack = infoView.getDragged().getOwner();
            stack.addCounter(infoView.getDragged());

            if (oldStack.getCounters().isEmpty()) {
                ((IMapMarker) infoView.getSelected()).removeStack(oldStack);
            }

            infoView.setDragged(null);
            infoView.setDragLocation(null);
        }
    }

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------

    /**
     * Method called by the pApplet when a mouse action is performed.
     *
     * @param event mouseEvent.
     */
    public void mouseEvent(MouseEvent event) {
        int action = event.getAction();

        if ((action == MouseEvent.DRAG) || (action == MouseEvent.MOVE)) {
            mouseX = event.getX();
            mouseY = event.getY();
        }

        mouseButton = event.getButton();

        if (firstMouse) {
            firstMouse = false;
        }

        mousePerform(action);
    }

    /**
     * Perform the matching method according to the action.
     *
     * @param action of the mouse
     */
    private void mousePerform(int action) {
        switch (action) {
            case MouseEvent.DRAG:
                mouseDragged();
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
