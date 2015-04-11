package com.mkl.eu.front.map.handler.mouse;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.List;

/**
 * Abstract Mouse Handler to handle drag&drop and contextual menu.
 *
 * @author MKL
 */
public abstract class AbstractMouseHandler extends MapEventBroadcaster {

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
     * @param maps A list of maps.
     */
    public AbstractMouseHandler(PApplet p, List<UnfoldingMap> maps) {
        super(maps);

        p.registerMethod("mouseEvent", this);
    }

    /**
     * Method called when mouse is clicked.
     *
     * @return <code>true</code> if subclasses should stop the click process, <code>false</code> otherwise.
     */
    public boolean mouseClicked() {
        return false;
    }

    /**
     * Method called when mouse wheel is used.
     *
     * @param delta wheel direction.
     * @return <code>true</code> if subclasses should stop the wheel process, <code>false</code> otherwise.
     */
    public boolean mouseWheel(float delta) {
        return false;
    }

    /**
     * Method called when the mouse is pressed.
     *
     * @return <code>true</code> if subclasses should stop the press process, <code>false</code> otherwise.
     */
    public boolean mousePressed() {
        return false;
    }

    /**
     * Method called when mouse is dragged.
     *
     * @return <code>true</code> if subclasses should stop the drag process, <code>false</code> otherwise.
     */
    public boolean mouseDragged() {
        return false;
    }

    /**
     * Method called when mouse is moved.
     *
     * @return <code>true</code> if subclasses should stop the move process, <code>false</code> otherwise.
     */
    public boolean mouseMoved() {
        return false;
    }

    /**
     * Method called when the mouse is released.
     *
     * @return <code>true</code> if subclasses should stop the release process, <code>false</code> otherwise.
     */
    public boolean mouseReleased() {
        return false;
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
     *
     * @param event  mouseEvent to spread to the matching method.
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

    /** @return the mouseX. */
    public int getMouseX() {
        return mouseX;
    }

    /** @return the mouseY. */
    public int getMouseY() {
        return mouseY;
    }

    /** @return the pmouseX. */
    public int getPmouseX() {
        return pmouseX;
    }

    /** @return the pmouseY. */
    public int getPmouseY() {
        return pmouseY;
    }

    /** @return the emouseX. */
    public int getEmouseX() {
        return emouseX;
    }

    /** @return the emouseY. */
    public int getEmouseY() {
        return emouseY;
    }

    /** @return the firstMouse. */
    public boolean isFirstMouse() {
        return firstMouse;
    }

    /** @return the mouseButton. */
    public int getMouseButton() {
        return mouseButton;
    }

    /** @return the mouseEvent. */
    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }
}
