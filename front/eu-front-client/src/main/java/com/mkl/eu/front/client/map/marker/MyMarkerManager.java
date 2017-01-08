package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.component.menu.ContextualMenuItem;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.event.HoverEvent;
import com.mkl.eu.front.client.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.client.map.handler.mouse.IDragAndDropAware;
import com.mkl.eu.front.client.vo.AuthentHolder;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker Manager to enable debugging.
 *
 * @author MKL
 */
@Component
@Scope(value = "prototype")
public class MyMarkerManager extends MarkerManager<Marker> implements IDragAndDropAware<StackMarker, IMapMarker>, IContextualMenuAware<Object>, MapEventListener, IDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyMarkerManager.class);
    /** Board Service. */
    @Autowired
    private IBoardService boardService;
    /** Game Admin Service. */
    @Autowired
    private IGameAdminService gameAdminService;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Selected marker. */
    private Marker selectedMarker;
    /** The stack being dragged. */
    private StackMarker dragged;
    /** The new location of the dragged object. */
    private Location dragLocation;
    /** Stack being hovered. */
    private StackMarker hovered = null;
    /** Location of the hovered stack. */
    private Location hoverLocation;
    /** Marker being contextualized. */
    private Object contextualized;
    /** Location of the contextual menu in the map frame. */
    private Location menuLocation;
    /** Contextual menu. */
    private ContextualMenu menu;
    /** Listeners for diffs event. */
    private List<IDiffListener> diffListeners = new ArrayList<>();
    /** Game configuration. */
    private GameConfiguration gameConfig;

    /**
     * Constructor.
     *
     * @param gameConfig the gameConfig to set.
     */
    public MyMarkerManager(GameConfiguration gameConfig) {
        this.gameConfig = gameConfig;
    }

    /** {@inheritDoc} */
    public void draw() {
        if (!bEnableDrawing)
            return;

        PGraphics pg = map.mapDisplay.getOuterPG();
        List<StackMarker> stacksToIgnore = new ArrayList<>();
        if (dragged != null) {
            stacksToIgnore.add(dragged);
        }
        if (hovered != null && hovered.getCounters().size() > 1) {
            stacksToIgnore.add(hovered);
        }

        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, stacksToIgnore);
            } else {
                marker.draw(map);
            }
        }

        Location randomPoint = new Location(0, 0);
        float[] xyRand = map.mapDisplay.getObjectFromLocation(randomPoint);
        float relativeSize = Math.abs(xyRand[0] - map.mapDisplay.getObjectFromLocation(new Location(randomPoint.getLat()
                + IMapMarker.COUNTER_SIZE, randomPoint.getLon() + IMapMarker.COUNTER_SIZE))[0]);

        if (dragged != null && dragLocation != null) {
            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(dragLocation);
            for (int j = 0; j < dragged.getCounters().size(); j++) {
                CounterMarker counter = dragged.getCounters().get(j);
                float x0 = xy[0];
                pg.image(counter.getImage(), x0 + relativeSize * j / 10
                        , xy[1] + relativeSize * j / 10, relativeSize, relativeSize);
            }
            pg.popStyle();
        }

        if (hovered != null && hoverLocation != null && hovered.getCounters().size() > 1 && hovered != dragged) {
            pg.pushStyle();
            pg.imageMode(PConstants.CORNER);
            float[] xy = map.mapDisplay.getObjectFromLocation(hoverLocation);
            for (int j = 0; j < hovered.getCounters().size(); j++) {
                CounterMarker counter = hovered.getCounters().get(j);

                pg.image(counter.getImage(), xy[0] + relativeSize * j * 2
                        , xy[1] + relativeSize / 2, relativeSize, relativeSize);
                pg.stroke(255, 255, 0);
                drawRectBorder(pg, xy[0] + relativeSize * j * 2
                        , xy[1] + relativeSize / 2, relativeSize, relativeSize, 2.5f);
            }
            pg.popStyle();
        }

        if (contextualized != null && menuLocation != null && menu != null) {
            float[] xy = map.mapDisplay.getObjectFromLocation(menuLocation);
            menu.setLocation(new Location(xy[0], xy[1]));
            menu.draw(pg);
        }
    }

    /**
     * Draw the borders of a rectangle.
     *
     * @param pg    the graphics.
     * @param x     X coordinate of the rectangle.
     * @param y     Y coordinate of the rectangle.
     * @param w     width of the rectangle.
     * @param h     height of the rectangle.
     * @param depth of the line.
     */
    private void drawRectBorder(PGraphics pg, float x, float y, float w, float h, float depth) {
        pg.strokeWeight(2 * depth);
        pg.line(x - depth, y - depth, x + w + depth, y - depth);
        pg.line(x + w + depth, y - depth, x + w + depth, y + h + depth);
        pg.line(x + w + depth, y + h + depth, x - depth, y + h + depth);
        pg.line(x - depth, y + h + depth, x - depth, y - depth);
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
                item = marker;
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
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.province", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(province.getId()));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        ContextualMenu neighbours = ContextualMenuItem.createMenuSubMenu(message.getMessage("map.menu.neighbors", null, globalConfiguration.getLocale()));
        for (final BorderMarker border : province.getNeighbours()) {
            StringBuilder label = new StringBuilder(message.getMessage(border.getProvince().getId(), null, globalConfiguration.getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(message.getMessage("border." + border.getType().getCode(), null, globalConfiguration.getLocale())).append(")");
            }
            neighbours.addMenuItem(ContextualMenuItem.createMenuLabel(label.toString()));
        }
        menu.addMenuItem(neighbours);
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A+", event -> createStack(CounterFaceTypeEnum.ARMY_PLUS, province)));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A-", event -> createStack(CounterFaceTypeEnum.ARMY_MINUS, province)));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add D", event -> createStack(CounterFaceTypeEnum.LAND_DETACHMENT, province)));
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
                Long idGame = gameConfig.getIdGame();
                try {
                    Request<MoveStackRequest> request = new Request<>();
                    authentHolder.fillAuthentInfo(request);
                    gameConfig.fillGameInfo(request);
                    gameConfig.fillChatInfo(request);
                    request.setRequest(new MoveStackRequest(stack.getId(), border.getProvince().getId()));
                    DiffResponse response = boardService.moveStack(request);
                    DiffEvent diff = new DiffEvent(response, idGame);
                    processDiffEvent(diff);
                } catch (Exception e) {
                    LOGGER.error("Error when moving stack.", e);

                    processExceptionEvent(new ExceptionEvent(e));
                }
                resetContextualMenu();
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
    private void createStack(CounterFaceTypeEnum type, IMapMarker province) {
        CounterForCreation counter = new CounterForCreation();
        counter.setCountry("france");
        counter.setType(type);
        Long idGame = gameConfig.getIdGame();
        try {
            DiffResponse response = gameAdminService.createCounter(idGame, gameConfig.getVersionGame(), counter, province.getId());
            DiffEvent event = new DiffEvent(response, idGame);
            processDiffEvent(event);
        } catch (Exception e) {
            LOGGER.error("Error when creating counter.", e);

            processExceptionEvent(new ExceptionEvent(e));
        }
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

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "markerManager" + map.getId();
    }

    /** {@inheritDoc} */
    @Override
    public void onManipulation(MapEvent event) {
        if (StringUtils.equals(DragEvent.TYPE_DRAG, event.getType()) && event instanceof DragEvent) {
            DragEvent dragEvent = (DragEvent) event;
            switch (event.getSubType()) {
                case DragEvent.DRAG_TAKE:
                    StackMarker marker = getDrag(dragEvent.getX(), dragEvent.getY());
                    setDragged(marker);
                    break;
                case DragEvent.DRAG_TO:
                    setDragLocation(new Location(dragEvent.getX(), dragEvent.getY()));
                    break;
                case DragEvent.DRAG_DROP:
                    IMapMarker drop = getDrop(dragEvent.getX(), dragEvent.getY());

                    if (isNeighbour(dragged.getProvince(), drop)) {
                        Long idGame = gameConfig.getIdGame();
                        try {
                            Request<MoveStackRequest> request = new Request<>();
                            authentHolder.fillAuthentInfo(request);
                            gameConfig.fillGameInfo(request);
                            gameConfig.fillChatInfo(request);
                            request.setRequest(new MoveStackRequest(dragged.getId(), drop.getId()));
                            DiffResponse response = boardService.moveStack(request);
                            DiffEvent diff = new DiffEvent(response, idGame);
                            processDiffEvent(diff);
                        } catch (Exception e) {
                            LOGGER.error("Error when moving stack.", e);

                            processExceptionEvent(new ExceptionEvent(e));
                        }
                    }

                    setDragged(null);
                    setDragLocation(null);
                    break;
                default:
                    break;
            }
        } else if (StringUtils.equals(HoverEvent.TYPE_HOVER, event.getType()) && event instanceof HoverEvent) {
            HoverEvent hoverEvent = (HoverEvent) event;
            StackMarker lastHover = hovered;
            hovered = getDrag(hoverEvent.getX(), hoverEvent.getY());
            if (hovered != lastHover) {
                hoverLocation = map.getLocation(hoverEvent.getX(), hoverEvent.getY());
            } else if (hovered == null) {
                hoverLocation = null;
            }
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

    /**
     * Process a ExceptionEvent.
     *
     * @param event to process.
     */
    private void processExceptionEvent(ExceptionEvent event) {
        for (IDiffListener diffListener : diffListeners) {
            diffListener.handleException(event);
        }
    }
}
