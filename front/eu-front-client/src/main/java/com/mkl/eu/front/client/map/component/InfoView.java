package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.board.MoveCounterRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.DiffResponseEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.mouse.IContextualMenuAware;
import com.mkl.eu.front.client.map.handler.mouse.IDragAndDropAware;
import com.mkl.eu.front.client.map.marker.CounterMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.MyMarkerManager;
import com.mkl.eu.front.client.map.marker.StackMarker;
import com.mkl.eu.front.client.vo.AuthentHolder;
import com.mkl.eu.front.client.window.InteractiveMap;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Information panel.
 *
 * @author MKL
 */
@Component
@Scope(value = "prototype")
public class InfoView extends AbstractDiffResponseListenerContainer implements IDragAndDropAware<CounterMarker, StackMarker>, IContextualMenuAware<Object>, MapEventListener, IMenuContainer {
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
    private PApplet pApplet;
    /** Marker manager to obtain the selected province. */
    private MyMarkerManager markerManager;
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

    /**
     * Constructor.
     *
     * @param pApplet       the pApplet to draw.
     * @param markerManager the markerManager to set.
     * @param game          the game.
     * @param gameConfig    the gameConfig to set.
     */
    public InfoView(PApplet pApplet, MyMarkerManager markerManager, Game game, GameConfiguration gameConfig) {
        super(gameConfig);
        this.pApplet = pApplet;
        this.markerManager = markerManager;
        this.game = game;
    }

    /**
     * Constructor.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param w Width.
     * @param h Height.
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

                pg.text(GlobalConfiguration.getMessage("map.infoview.stacks"), newX, newY);
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
        boolean inInfoView = isHit(x, y);
        if (inInfoView && markerManager.getSelectedMarker() instanceof IMapMarker
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
        if (item == null && isHit(x, y)) {
            item = markerManager.getSelectedMarker();
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
            menu = MenuHelper.createMenuCounter((CounterMarker) contextualized, this);
        } else if (contextualized instanceof StackMarker) {
            menu = MenuHelper.createMenuStack((StackMarker) contextualized, this);
        } else {
            Marker marker = markerManager.getSelectedMarker();
            if (marker instanceof IMapMarker) {
                menu = MenuHelper.createMenuProvince((IMapMarker) marker, this);
            }
        }

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
                        Long idStack = drop != null ? drop.getId() : null;
                        callService(boardService::moveCounter, () -> new MoveCounterRequest(dragged.getId(), idStack), "Error when moving stack.");
                    }

                    setDragged(null);
                    setDragLocation(null);
                    break;
                default:
                    break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processDiffEvent(DiffResponseEvent event) {
        resetContextualMenu();
        super.processDiffEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void processExceptionEvent(ExceptionEvent event) {
        resetContextualMenu();
        super.processExceptionEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public GlobalConfiguration getGlobalConfiguration() {
        return GlobalConfiguration.getInstance();
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
        return markerManager.getComponent();
    }
}