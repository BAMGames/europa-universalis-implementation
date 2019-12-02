package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.enumeration.TerrainEnum;
import com.mkl.eu.front.client.event.DiffResponseEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.event.IDiffResponseListener;
import com.mkl.eu.front.client.event.IDiffResponseListenerContainer;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.component.IMenuContainer;
import com.mkl.eu.front.client.map.component.MenuHelper;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.event.HoverEvent;
import com.mkl.eu.front.client.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.client.map.handler.mouse.IDragAndDropAware;
import com.mkl.eu.front.client.vo.AuthentHolder;
import com.mkl.eu.front.client.window.InteractiveMap;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MyMarkerManager extends MarkerManager<Marker> implements IDragAndDropAware<StackMarker, IMapMarker>, IContextualMenuAware<Object>, MapEventListener, IDiffResponseListenerContainer, IMenuContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyMarkerManager.class);
    /** Interactive map. */
    private InteractiveMap interactiveMap;
    /** Game. */
    private Game game;
    /** Board Service. */
    @Autowired
    private IBoardService boardService;
    /** Battle Service. */
    @Autowired
    private IBattleService battleService;
    /** Siege Service. */
    @Autowired
    private ISiegeService siegeService;
    /** Interphase Service. */
    @Autowired
    private IInterPhaseService interPhaseService;
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
    private List<IDiffResponseListener> diffListeners = new ArrayList<>();
    /** Game configuration. */
    private GameConfiguration gameConfig;

    /**
     * Constructor.
     *
     * @param map        the interactive map.
     * @param game       the game.
     * @param gameConfig the gameConfig to set.
     */
    public MyMarkerManager(InteractiveMap map, Game game, GameConfiguration gameConfig) {
        this.interactiveMap = map;
        this.game = game;
        this.gameConfig = gameConfig;
    }

    /** {@inheritDoc} */
    public void draw() {
        if (!bEnableDrawing)
            return;

        PGraphics pg = map.mapDisplay.getOuterPG();
        List<StackMarker> stacksToIgnore = new ArrayList<>();
        List<StackMarker> stacksSelected = new ArrayList<>();
        if (dragged != null) {
            stacksToIgnore.add(dragged);
        }
        if (hovered != null && hovered.getCounters().size() > 1) {
            stacksSelected.add(hovered);
        }

        for (Marker marker : markers) {
            if (marker instanceof IMapMarker) {
                ((IMapMarker) marker).draw(map, stacksSelected, stacksToIgnore);
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
            menu = MenuHelper.createMenuProvince((IMapMarker) contextualized, this);
        } else if (contextualized instanceof StackMarker) {
            menu = MenuHelper.createMenuStack((StackMarker) contextualized, this);
        }

        return menu;
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
                        callService(boardService::moveStack, () -> new MoveStackRequest(dragged.getId(), drop.getId()), "Error when moving stack.");
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

    /** {@inheritDoc} */
    @Override
    public void addDiffListener(IDiffResponseListener diffListener) {
        if (!diffListeners.contains(diffListener)) {
            diffListeners.add(diffListener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processDiffEvent(DiffResponseEvent event) {
        resetContextualMenu();
        for (IDiffResponseListener diffListener : diffListeners) {
            diffListener.update(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processExceptionEvent(ExceptionEvent event) {
        resetContextualMenu();
        for (IDiffResponseListener diffListener : diffListeners) {
            diffListener.handleException(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    /** {@inheritDoc} */
    @Override
    public GameConfiguration getGameConfig() {
        return gameConfig;
    }

    /** {@inheritDoc} */
    @Override
    public AuthentHolder getAuthentHolder() {
        return authentHolder;
    }

    /** {@inheritDoc} */
    @Override
    public InteractiveMap getComponent() {
        return interactiveMap;
    }

    /** {@inheritDoc} */
    @Override
    public IBoardService getBoardService() {
        return boardService;
    }

    /** {@inheritDoc} */
    @Override
    public IBattleService getBattleService() {
        return battleService;
    }

    /** {@inheritDoc} */
    @Override
    public ISiegeService getSiegeService() {
        return siegeService;
    }

    /** {@inheritDoc} */
    @Override
    public IInterPhaseService getInterPhaseService() {
        return interPhaseService;
    }

    /** {@inheritDoc} */
    @Override
    public Game getGame() {
        return game;
    }
}
