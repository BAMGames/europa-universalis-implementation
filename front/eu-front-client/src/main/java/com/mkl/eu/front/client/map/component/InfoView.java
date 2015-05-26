package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.common.vo.AuthentRequest;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.game.MoveStackRequest;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.MapConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.component.menu.ContextualMenuItem;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.client.map.handler.mouse.IDragAndDropAware;
import com.mkl.eu.front.client.map.marker.*;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Information panel.
 *
 * @author MKL
 */
@Component
public class InfoView implements IDragAndDropAware<CounterMarker, StackMarker>, IContextualMenuAware<Object>, MapEventListener, IDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoView.class);
    /** Game Service. */
    @Autowired
    private IGameService gameService;
    /** Game Admin Service. */
    @Autowired
    private IGameAdminService gameAdminService;
    /** Vertical Padding. */
    private static final float V_PADDING = 20;
    /** Horizontal Padding. */
    private static final float H_PADDING = 10;
    /** Vertical Space taken by a text line. */
    private static final float V_TEXT = 20;
    /** Space between two objects. */
    private static final float SPACE = 10;
    /** Size of a counter. */
    private static final float SIZE = 30;
    /** PApplet for drawing purpose. */
    @Autowired
    private PApplet pApplet;
    /** Marker manager to obtain the selected province. */
    @Autowired
    private MyMarkerManager markerManager;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** The counter being dragged. */
    private CounterMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
    /** X coordinate. */
    private float x;
    /** Y coordinate. */
    private float y;
    /** Width. */
    private float w;
    /** Height. */
    private float h;
    /** Counter being contextualized. */
    private Object contextualized;
    /** Contextual menu. */
    private ContextualMenu menu;
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param x       X coordinate.
     * @param y       Y coordinate.
     * @param w       Width.
     * @param h       Height.
     */
    public void init(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /** Draw the viewport. */
    public void draw() {
        PGraphics pg = pApplet.recorder;
        if (pg == null) {
            pg = pApplet.g;
        }
        pg.pushStyle();
        pg.fill(255, 255, 255);
        pg.rect(x, y, w, h);
        pg.fill(0, 0, 0);

        float newX = x + H_PADDING;
        float newY = y + V_PADDING;

        Marker marker = markerManager.getSelectedMarker();
        if (marker != null) {
            pg.text(marker.getId(), newX, newY);
            newY += V_TEXT;
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;

                pg.text(message.getMessage("map.infoview.stacks", null, globalConfiguration.getLocale()), newX, newY);
                newY += V_TEXT;
                pg.imageMode(PConstants.CORNER);
                for (int i = 0; i < mapMarker.getStacks().size(); i++) {
                    for (int j = 0; j < mapMarker.getStacks().get(i).getCounters().size(); j++) {
                        CounterMarker counter = mapMarker.getStacks().get(i).getCounters().get(j);
                        if (counter != dragged) {
                            pg.image(counter.getImage(), newX + (SIZE + SPACE) * j
                                    , newY + (SIZE + SPACE) * i, SIZE, SIZE);
                        }
                    }
                }
            }
        }

        if (dragged != null && dragLocation != null) {
            pg.image(dragged.getImage(), dragLocation.getLat(), dragLocation.getLon(),
                    SIZE, SIZE);
        }

        if (contextualized != null && menu != null) {
            menu.draw(pg);
        }

        pg.popStyle();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHit(int checkX, int checkY) {
        return checkX > x && checkX < x + w && checkY > y && checkY < y + h;
    }

    /** {@inheritDoc} */
    @Override
    public CounterMarker getDragged() {
        return dragged;
    }

    /** {@inheritDoc} */
    @Override
    public void setDragged(CounterMarker dragged) {
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
        this.dragLocation = dragLocation;
    }

    /** @return the selected marker. */
    public Marker getSelected() {
        return markerManager.getSelectedMarker();
    }

    /** {@inheritDoc} */
    @Override
    public CounterMarker getDrag(int x, int y) {
        CounterMarker counter = null;

        StackMarker stack = getDrop(x, y);

        if (stack != null) {
            float newX = this.x + H_PADDING;
            int counterNumber = (int) ((x - newX) / (SIZE + SPACE));
            float x0 = newX + counterNumber * (SIZE + SPACE);
            if (x >= x0 && x <= x0 + SIZE && counterNumber >= 0 && counterNumber < stack.getCounters().size()) {
                counter = stack.getCounters().get(counterNumber);
            }
        }

        return counter;
    }

    /** {@inheritDoc} */
    @Override
    public StackMarker getDrop(int x, int y) {
        StackMarker stack = null;
        float newY = this.y + V_PADDING + 2 * V_TEXT;

        int stackNumber = (int) ((y - newY) / (SIZE + SPACE));
        if (markerManager.getSelectedMarker() instanceof IMapMarker
                && stackNumber >= 0 && stackNumber < ((IMapMarker) markerManager.getSelectedMarker()).getStacks().size()) {
            stack = ((IMapMarker) markerManager.getSelectedMarker()).getStacks().get(stackNumber);
        }

        return stack;
    }

    /** {@inheritDoc} */
    @Override
    public Object getContextualizedItem(int x, int y) {
        Object item = getDrag(x, y);
        if (item == null) {
            item = getDrop(x, y);
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
            this.menu = createMenu();
            this.menu.setLocation(menuLocation);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetContextualMenu() {
        this.contextualized = null;
        this.menu = null;
    }

    /**
     * Create a Contextual Menu given the contextualized object.
     *
     * @return a Contextual Menu given the contextualized object.
     */
    private ContextualMenu createMenu() {
        ContextualMenu menu = null;
        if (contextualized instanceof CounterMarker) {
            menu = createMenuCounter((CounterMarker) contextualized);
        } else if (contextualized instanceof StackMarker) {
            menu = createMenuStack((StackMarker) contextualized);
        }

        return menu;
    }

    /**
     * Create a Contextual Menu for a Counter.
     *
     * @param counter where the contextual menu is.
     * @return a Contextual Menu for a Counter.
     */
    private ContextualMenu createMenuCounter(final CounterMarker counter) {
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.counter", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(message.getMessage("map.menu.counter", null, globalConfiguration.getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addMenuItem(ContextualMenuItem.createMenuItem(message.getMessage("map.menu.disband", null, globalConfiguration.getLocale()), event -> {
            Long idGame = MapConfiguration.getIdGame();
            try {
                DiffResponse response = gameAdminService.removeCounter(idGame, MapConfiguration.getVersionGame(),
                        counter.getId());
                DiffEvent diff = new DiffEvent(response.getDiffs(), idGame, response.getVersionGame());
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when moving stack.", e);
                // TODO exception handling
            }

            resetContextualMenu();
        }));

        return menu;
    }

    /**
     * Create a Contextual Menu for a Stack.
     *
     * @param stack where the contextual menu is.
     * @return a Contextual Menu for a Stack.
     */
    private ContextualMenu createMenuStack(final StackMarker stack) {
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.stack", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(message.getMessage("map.menu.stack", null, globalConfiguration.getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu(message.getMessage("map.menu.move", null, globalConfiguration.getLocale()));
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(message.getMessage(border.getProvince().getId(), null, globalConfiguration.getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(message.getMessage("border." + border.getType().getCode(), null, globalConfiguration.getLocale())).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), event -> {
                Long idGame = MapConfiguration.getIdGame();
                try {
                    AuthentRequest<MoveStackRequest> request = new AuthentRequest<>();
                    request.setRequest(new MoveStackRequest(idGame, MapConfiguration.getVersionGame(),
                            stack.getId(), border.getProvince().getId()));
                    DiffResponse response = gameService.moveStack(request);
                    DiffEvent diff = new DiffEvent(response.getDiffs(), idGame, response.getVersionGame());
                    processDiffEvent(diff);
                } catch (Exception e) {
                    LOGGER.error("Error when moving stack.", e);
                    // TODO exception handling
                }
                resetContextualMenu();
            }));
        }
        menu.addMenuItem(move);

        return menu;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hit(int x, int y) {
        return !(menu == null || contextualized == null) && menu.hit(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hover(int x, int y) {
        boolean hover = false;

        if (menu != null) {
            hover = menu.hover(x, y);
        }

        return hover;
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "infoView";
    }

    /** {@inheritDoc} */
    @Override
    public void onManipulation(MapEvent event) {
        if (StringUtils.equals(DragEvent.TYPE_DRAG, event.getType()) && event instanceof DragEvent) {
            DragEvent dragEvent = (DragEvent) event;
            switch (event.getSubType()) {
                case DragEvent.DRAG_TAKE:
                    CounterMarker marker = getDrag(dragEvent.getX(), dragEvent.getY());
                    setDragged(marker);
                    break;
                case DragEvent.DRAG_TO:
                    setDragLocation(new Location(dragEvent.getX(), dragEvent.getY()));
                    break;
                case DragEvent.DRAG_DROP:
                    StackMarker drop = getDrop(dragEvent.getX(), dragEvent.getY());


                    if (drop != dragged.getOwner()) {
                        Long idGame = MapConfiguration.getIdGame();
                        try {
                            Long idStack = null;
                            if (drop != null) {
                                idStack = drop.getId();
                            }
                            DiffResponse response = gameService.moveCounter(idGame, MapConfiguration.getVersionGame(),
                                    dragged.getId(), idStack);
                            DiffEvent diff = new DiffEvent(response.getDiffs(), idGame, response.getVersionGame());
                            processDiffEvent(diff);
                        } catch (Exception e) {
                            LOGGER.error("Error when moving stack.", e);
                            // TODO exception handling
                        }
                    }

                    setDragged(null);
                    setDragLocation(null);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    public void addDiffListener(IDiffListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /**
     * Process a DiffEvent.
     *
     * @param event to process.
     */
    private void processDiffEvent(DiffEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }
}