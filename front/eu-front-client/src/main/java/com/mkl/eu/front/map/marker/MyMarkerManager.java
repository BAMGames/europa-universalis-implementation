package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.front.component.menu.ContextualMenu;
import com.mkl.eu.front.component.menu.ContextualMenuItem;
import com.mkl.eu.front.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.map.handler.mouse.IDragAndDropAware;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.List;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
public class MyMarkerManager extends MarkerManager<Marker> implements IDragAndDropAware<StackMarker, IMapMarker>, IContextualMenuAware<Object> {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyMarkerManager.class);
    /** Utility to draw counters. */
    private MarkerUtils markerUtils;
    /** Selected marker. */
    private Marker selectedMarker;
    /** The stack being dragged. */
    private StackMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
    /** Marker being contextualized. */
    private Object contextualized;
    /** Location of the contextual menu in the map frame. */
    private Location menuLocation;
    /** Contextual menu. */
    private ContextualMenu menu;


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

        if (contextualized != null && menuLocation != null && menu != null) {
            float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
            menu.setLocation(new Location(xy[0], xy[1]));
            menu.draw(pg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean hit(int x, int y) {
        if (menuLocation == null || contextualized == null || menu == null) {
            return false;
        }


        float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
        menu.setLocation(new Location(xy[0], xy[1]));
        return menu.hit(x, y);
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
    public Object getContextualizedItem(int x, int y) {
        Object item = null;
        Marker marker = getFirstHitMarker(x, y);
        if (marker instanceof IMapMarker) {
            item = ((IMapMarker) marker).getStack(map, x, y);
            if (item == null) {
                item = (IMapMarker) marker;
            }
        }
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public void contextualMenu(Object item, Location menuLocation) {
        if (item == contextualized) {
            resetContextualMenu();
        } else {
            this.contextualized = item;
            this.menuLocation = map.mapDisplay.getLocation(menuLocation.getLat(), menuLocation.getLon());
            this.menu = createMenu();
            float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
            menu.setLocation(new Location(xy[0], xy[1]));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetContextualMenu() {
        this.contextualized = null;
        this.menuLocation = null;
        this.menu = null;
    }

    /**
     * Create a Contextual Menu given the contextualized object.
     *
     * @return a Contextual Menu given the contextualized object.
     */
    private ContextualMenu createMenu() {
        ContextualMenu menu = null;
        if (contextualized instanceof IMapMarker) {
            menu = createMenuProvince((IMapMarker) contextualized);
        } else if (contextualized instanceof StackMarker) {
            menu = createMenuStack((StackMarker) contextualized);
        }

        return menu;
    }

    /**
     * Create a Contextual Menu for a Province.
     *
     * @param province where the contextual menu is.
     * @return a Contextual Menu for a Province.
     */
    private ContextualMenu createMenuProvince(final IMapMarker province) {
        ContextualMenu menu = new ContextualMenu("Province");
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(province.getId()));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A+", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createStack(CounterTypeEnum.ARMY_PLUS, province);
            }
        }));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A-", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createStack(CounterTypeEnum.ARMY_MINUS, province);
            }
        }));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add D", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createStack(CounterTypeEnum.LAND_DETACHMENT, province);
            }
        }));
        ContextualMenu subMenu1 = ContextualMenuItem.createMenuSubMenu("Test");
        ContextualMenu subMenu2 = ContextualMenuItem.createMenuSubMenu("Sous menu !");
        subMenu2.addMenuItem(ContextualMenuItem.createMenuItem("action", null));
        subMenu2.addMenuItem(ContextualMenuItem.createMenuLabel("text"));
        subMenu2.addMenuItem(ContextualMenuItem.createMenuItem("reaction", null));
        subMenu1.addMenuItem(subMenu2);
        subMenu1.addMenuItem(ContextualMenuItem.createMenuItem("Amen", null));
        subMenu1.addMenuItem(ContextualMenuItem.createMenuLabel("Upide"));
        ContextualMenu subMenu3 = ContextualMenuItem.createMenuSubMenu("Un autre");
        subMenu3.addMenuItem(ContextualMenuItem.createMenuLabel("OK"));
        subMenu3.addMenuItem(ContextualMenuItem.createMenuItem("Ou pas", null));
        subMenu1.addMenuItem(subMenu3);
        subMenu1.addMenuItem(ContextualMenuItem.createMenuItem("Icule", null));
        menu.addMenuItem(subMenu1);

        return menu;
    }

    /**
     * Create a Contextual Menu for a Stack.
     *
     * @param stack where the contextual menu is.
     * @return a Contextual Menu for a Stack.
     */
    private ContextualMenu createMenuStack(final StackMarker stack) {
        ContextualMenu menu = new ContextualMenu("Stack");
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu("Move");
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(border.getProvince().getId());
            if (border.getType() != null) {
                label.append(" (").append(border.getType()).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    border.getProvince().addStack(stack);
                    resetContextualMenu();
                }
            }));
        }
        menu.addMenuItem(move);

        return menu;
    }

    /**
     * Creates a French stack of one counter on the province.
     *
     * @param type     of the counter to create.
     * @param province where the stack should be created.
     * @return the stack created.
     */
    private StackMarker createStack(CounterTypeEnum type, IMapMarker province) {
        Stack stack = new Stack();
        Counter counter = new Counter();
        counter.setCountry(new Country());
        counter.getCountry().setName("FRA");
        counter.setOwner(stack);
        counter.setType(type);
        stack.getCounters().add(counter);
        StackMarker stackMarker = new StackMarker(stack, province);
        stackMarker.addCounter(new CounterMarker(counter, markerUtils.getImageFromCounter(counter)));
        province.addStack(stackMarker);

        return stackMarker;
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

    /**
     * Hover the given location.
     *
     * @param x coordinate.
     * @param y coordinate.
     * @return <code>true</code> if something was hovered, <ocde>false</ocde> otherwise.
     */
    public boolean hover(int x, int y) {
        boolean hover = false;

        if (menu != null) {
            hover = menu.hover(x, y);
        }

        return hover;
    }

    /** {@inheritDoc} */
    @Override
    public Marker getFirstHitMarker(float checkX, float checkY) {
        List<Marker> markers = getHitMarkers(checkX, checkY);
        Marker firstMarker = null;

        if (markers != null) {
            for (Marker marker : markers) {
                if (firstMarker == null || TerrainEnum.SEA == firstMarker.getProperties().get(IMapMarker.PROP_TERRAIN)) {
                    firstMarker = marker;
                }
            }
        }

        return firstMarker;
    }
}
