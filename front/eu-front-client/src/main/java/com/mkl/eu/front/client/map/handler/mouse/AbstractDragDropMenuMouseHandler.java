package com.mkl.eu.front.client.map.handler.mouse;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.List;

/**
 * Abstract Mouse Handler to handle drag&drop and contextual menu.
 *
 * @param <T> type of the drag object.
 * @param <U> type of the drop object.
 * @param <V> type of the component object.
 * @param <S> type of the object being contextualized.
 * @author MKL
 */
public abstract class AbstractDragDropMenuMouseHandler<T, U, S, V extends IDragAndDropAware<T, U> & IContextualMenuAware<S>> extends AbstractMouseHandler {
    /** Component responsible for the behavior of drag&drop and contextual menu. */
    private V component;

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p         The PApplet.
     * @param component informative panel.
     * @param maps      A list of maps.
     */
    public AbstractDragDropMenuMouseHandler(PApplet p, V component, List<UnfoldingMap> maps) {
        super(p, maps);

        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseClicked() {
        boolean stop = super.mouseClicked();
        if (getMouseButton() == PConstants.LEFT) {
            stop = component.hit(getMouseX(), getMouseY());
            if (!stop) {
                component.resetContextualMenu();
            }
        }
        if (getMouseButton() == PConstants.RIGHT) {
            S item = component.getContextualizedItem(getMouseX(), getMouseY());
            if (item != null) {
                component.contextualMenu(item, new Location(getMouseX(), getMouseY()));
                stop = item != null;
            }
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mousePressed() {
        boolean stop = super.mousePressed();
        if (!stop && component.isHit(getMouseX(), getMouseY())) {
            T marker = component.getDrag(getMouseX(), getMouseY());
            component.setDragged(marker);
            stop = true;
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseDragged() {
        boolean stop = false;
        if (component.isHit(getMouseX(), getMouseY())) {
            if (getMouseButton() == PConstants.LEFT) {
                Location newLocation = new Location(getMouseX(), getMouseY());
                if (component.getDragged() != null) {
                    component.setDragLocation(newLocation);
                    stop = true;
                }
            }
        }

        return stop;
    }

    /** {@inheritDoc} */
    @Override
    public boolean mouseReleased() {
        boolean stop = super.mouseReleased();
        if (!stop && component.getDragged() != null) {
            U drop = component.getDrop(getMouseX(), getMouseY());
            doAfterRelease(component.getDragged(), drop);

            component.setDragged(null);
            component.setDragLocation(null);

            stop = true;
        }

        return stop;
    }

    /**
     * Do the process after the release is done.
     *
     * @param dragged the object being dragged.
     * @param drop    the object where the drag is dropped.
     */
    protected abstract void doAfterRelease(T dragged, U drop);

    /** @return the component. */
    public V getComponent() {
        return component;
    }
}
