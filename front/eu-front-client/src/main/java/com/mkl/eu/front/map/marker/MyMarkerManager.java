package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.front.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.map.handler.mouse.IDragAndDropAware;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
public class MyMarkerManager extends MarkerManager<Marker> implements IDragAndDropAware<StackMarker, IMapMarker>, IContextualMenuAware<IMapMarker> {
    /** Width of the contextual menu. */
    private static final int MENU_WIDTH = 50;
    /** Height of the contextual menu. */
    private static final int MENU_HEIGHT = 75;
    /** Padding in the contextual menu. */
    private static final int PADDING = 5;
    /** Vertical space used by each item in the contextual menu. */
    private static final int V_SPACE = 20;
    /** Utility to draw counters. */
    private MarkerUtils markerUtils;
    /** Selected marker. */
    private Marker selectedMarker;
    /** The stack being dragged. */
    private StackMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
    /** Marker being contextualized. */
    private IMapMarker contextualized;
    /** Location of the contextual menu. */
    private Location menuLocation;


    /**
     * Constructor.
     *
     * @param markerUtils markerUtils.
     */
    public MyMarkerManager(MarkerUtils markerUtils) {
        this.markerUtils = markerUtils;
    }

    /** {@inheritDoc} */
    public void draw() {
        if (!bEnableDrawing)
            return;

        PGraphics pg = map.mapDisplay.getOuterPG();

        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, dragged);
            } else {
                marker.draw(map);
            }
        }

        if (dragged != null && dragLocation != null) {
            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(dragLocation);
            float size = 0.08f * map.getZoom();
            for (int j = 0; j < dragged.getCounters().size(); j++) {
                CounterMarker counter = dragged.getCounters().get(j);
                float x0 = xy[0];
                pg.image(counter.getImage(), x0 + size * j / 10
                        , xy[1] + size * j / 10, size, size);
            }
            pg.popStyle();
        }

        if (contextualized != null && menuLocation != null) {
            pg.pushStyle();

            pg.fill(255, 255, 255, 255);
            float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
            pg.rect(xy[0], xy[1], MENU_WIDTH, -MENU_HEIGHT);

            pg.fill(0, 0, 0);
            pg.text("New", xy[0] + PADDING, xy[1] - (MENU_HEIGHT - PADDING - V_SPACE));

            pg.popStyle();

        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hit(int x, int y) {
        if (menuLocation == null || contextualized == null) {
            return false;
        }

        boolean hit = false;
        float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);

        if (x >= xy[0] && x <= xy[0] + MENU_WIDTH
                && y <= xy[1] && y >= xy[1] - MENU_HEIGHT) {
            hit = true;

            if (y <= xy[1] - (MENU_HEIGHT - PADDING - V_SPACE) && y >= xy[1] - (MENU_HEIGHT - PADDING)) {
                Stack stack = new Stack();
                Counter counter = new Counter();
                counter.setCountry(new Country());
                counter.getCountry().setName("FRA");
                counter.setOwner(stack);
                counter.setType(CounterTypeEnum.ARMY_PLUS);
                stack.getCounters().add(counter);
                StackMarker stackMarker = new StackMarker(stack, contextualized);
                stackMarker.addCounter(new CounterMarker(counter, markerUtils.getImageFromCounter(counter)));
                contextualized.addStack(stackMarker);
            }
        }

        return hit;
    }

    /**
     * Select a marker and deselect the previous one.
     *
     * @param marker to be selected.
     */
    public void select(Marker marker) {
        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
        }

        selectedMarker = marker;

        selectedMarker.setSelected(true);
    }

    /** {@inheritDoc} */
    @Override
    public IMapMarker getContextualizedItem(int x, int y) {
        IMapMarker item = null;
        Marker marker = getFirstHitMarker(x, y);
        if (marker instanceof IMapMarker) {
            item = (IMapMarker) marker;
        }
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public void contextualMenu(IMapMarker item, Location menuLocation) {
        if (item == contextualized) {
            resetContextualMenu();
        } else {
            this.contextualized = item;
            this.menuLocation = map.mapDisplay.getLocation(menuLocation.getLat(), menuLocation.getLon());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetContextualMenu() {
        this.contextualized = null;
        this.menuLocation = null;
    }

    /** @return the selectedMarker. */
    public Marker getSelectedMarker() {
        return selectedMarker;
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getDragged() {
        return dragged;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHit(int checkX, int checkY) {
        return map.isHit(checkX, checkY);
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getDrag(int x, int y) {
        StackMarker stack = null;
        Marker marker = getFirstHitMarker(x, y);
        if (marker instanceof IMapMarker) {
            stack = ((IMapMarker) marker).getStack(map, x, y);
        }

        return stack;
    }

    /** {@inheritDoc} */
    @Override
    public void setDragged(StackMarker dragged) {
        this.dragged = dragged;
    }

    /** {@inheritDoc} */
    @Override
    public Location getDragLocation() {
        return dragLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setDragLocation(Location dragLocation) {
        if (dragLocation != null) {
            this.dragLocation = map.getLocation(dragLocation.getLat(), dragLocation.getLon());
        } else {
            this.dragLocation = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public IMapMarker getDrop(int x, int y) {
        IMapMarker drop = null;
        Marker marker = getFirstHitMarker(x, y);
        if (marker instanceof IMapMarker) {
            drop = (IMapMarker) marker;
        }

        return drop;
    }
}
