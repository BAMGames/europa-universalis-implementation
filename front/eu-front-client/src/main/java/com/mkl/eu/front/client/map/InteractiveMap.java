package com.mkl.eu.front.client.map;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.map.component.InfoView;
import com.mkl.eu.front.client.map.component.ViewportRect;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.keyboard.MapKeyboardHandler;
import com.mkl.eu.front.client.map.handler.mouse.InfoViewMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MapMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MultipleMapMouseHandler;
import com.mkl.eu.front.client.map.marker.*;
import com.mkl.eu.front.client.map.provider.EUProvider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.*;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import processing.core.PApplet;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Everything under the interactive Map.
 *
 * @author MKL
 */
public class InteractiveMap extends PApplet implements MapEventListener, IDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveMap.class);
    /** Utility for markers. */
    @Autowired
    private MarkerUtils markerUtils;
    /** Marker manager. */
    @Autowired
    private MyMarkerManager markerManager;
    /** Game service. */
    @Autowired
    private IGameService gameService;
    /** Interactive map. */
    private UnfoldingMap mapDetail;

    /** Small map showing the overview, i.e. the world. */
    private UnfoldingMap mapOverviewStatic;

    /** Interactive finder box atop the overview map. */
    private ViewportRect viewportRect;
    /** Information panel of the selected province. */
    @Autowired
    private InfoView info;
    /** Markers of the loaded game. */
    private Map<String, Marker> countryMarkers;
    /** Game. */
    private Game game;
    /** Components that listener to diffs. */
    private List<IDiffListenerContainer> components = new ArrayList<>();

    /**
     * Main method.
     *
     * @param args no args.
     */
    public static void main(String args[]) {
        PApplet.main(new String[]{"com.mkl.eu.front.client.map.InteractiveMap"});
    }

    /**
     * Factory method.
     *
     * @return instance.
     */
    public static InteractiveMap createInstance() {
        InteractiveMap mine = new InteractiveMap();
        mine.init();
        return mine;
    }

    /**
     * Initialization.
     */
    @PostConstruct
    public void initialize() {
        mapDetail = new UnfoldingMap(this, "detail", 0, 0, 800, 600, true, false, new EUProvider(this), null);
        // Too many inaccessible field to enable tween and no loop.
        mapDetail.setTweening(true);
        mapDetail.zoomToLevel(7);
        mapDetail.setZoomRange(5, 10);
        mapDetail.panTo(1300, -300);

        // Static overview map
        mapOverviewStatic = new UnfoldingMap(this, "overviewStatic", 805, 5, 185, 235, true, false, new EUProvider(this), null);
        mapOverviewStatic.zoomToLevel(4);
        mapOverviewStatic.panTo(new Location(12, 11));

        viewportRect = new ViewportRect(this);

        mapDetail.addMarkerManager(markerManager);

        info.init(805, 245, 185, 350);

        EventDispatcher eventDispatcher = new EventDispatcher();
        MapKeyboardHandler keyboardHandler = new MapKeyboardHandler(this, gameService, mapDetail);
        MapMouseHandler mouseHandler = new MapMouseHandler(this, markerManager, mapDetail);
        new MultipleMapMouseHandler(this, mapOverviewStatic, viewportRect, mapDetail);
        InfoViewMouseHandler infoHandler = new InfoViewMouseHandler(this, info, mapDetail);

        eventDispatcher.addBroadcaster(keyboardHandler);
        eventDispatcher.addBroadcaster(mouseHandler);
        eventDispatcher.addBroadcaster(infoHandler);

        eventDispatcher.register(mapDetail, PanMapEvent.TYPE_PAN, mapDetail.getId());
        eventDispatcher.register(mapDetail, ZoomMapEvent.TYPE_ZOOM, mapDetail.getId());

        eventDispatcher.register(markerManager, DragEvent.TYPE_DRAG, markerManager.getId());
        eventDispatcher.register(info, DragEvent.TYPE_DRAG, info.getId());

//        eventDispatcher.register(this, PanMapEvent.TYPE_PAN, getId(), mapDetail.getId());
//        eventDispatcher.register(this, ZoomMapEvent.TYPE_ZOOM, getId(), mapDetail.getId());
//        eventDispatcher.register(this, DragEvent.TYPE_DRAG, getId(), markerManager.getId(), info.getId());
//        eventDispatcher.register(this, HoverEvent.TYPE_HOVER, getId(), mapDetail.getId(), markerManager.getId(), info.getId());

        components.add(markerManager);
        components.add(info);
        components.add(keyboardHandler);
    }

    /**
     * Store the markers of the given game.
     *
     * @param game to load.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /** Set up the map and the markers. */
    public void setup() {
        size(1000, 600, OPENGL);
        if (frame != null) {
            frame.setResizable(true);
        }

        countryMarkers = markerUtils.createMarkers(game);
        mapDetail.addMarkers(countryMarkers.values().toArray(new Marker[countryMarkers.values().size()]));

        // Disable the auto-draw feature. Manual redraw on change.
//        noLoop();
    }

    /** Draw the PApplet. */
    public void draw() {
        background(0);

        mapDetail.draw();
        mapOverviewStatic.draw();
        // Viewport is updated by the actual area of the detail map
        ScreenPosition tl = mapOverviewStatic.getScreenPosition(mapDetail.getTopLeftBorder());
        ScreenPosition br = mapOverviewStatic.getScreenPosition(mapDetail.getBottomRightBorder());
        viewportRect.setDimension(tl, br);
        viewportRect.draw();
        info.draw();
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "map";
    }

    /** {@inheritDoc} */
    @Override
    public void onManipulation(MapEvent event) {
        redraw();
    }

    /**
     * Add a diff listener.
     *
     * @param diffListener to add.
     */
    public void addDiffListener(IDiffListener diffListener) {
        for (IDiffListenerContainer component : components) {
            component.addDiffListener(diffListener);
        }
    }

    /**
     * Update the Map given the diff.
     *
     * @param diff that will update the map.
     */
    public synchronized void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case COUNTER:
                updateCounter(diff);
                break;
            case STACK:
                updateStack(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process a counter diff event.
     *
     * @param diff involving a counter.
     */
    private void updateCounter(Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addCounter(diff);
                break;
            case MOVE:
                moveCounter(diff);
                break;
            case REMOVE:
                removeCounter(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process the add counter diff event.
     *
     * @param diff involving a add counter.
     */
    private void addCounter(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute == null) {
            LOGGER.error("Missing province in counter add event.");
            return;
        }
        Marker prov = countryMarkers.get(attribute.getValue());
        if (!(prov instanceof IMapMarker)) {
            LOGGER.error("province is not a IMapMarker.");
            return;
        }
        IMapMarker province = (IMapMarker) prov;

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
        if (attribute == null) {
            LOGGER.error("Missing type in counter add event.");
            return;
        }

        String type = attribute.getValue();

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTRY);
        if (attribute == null) {
            LOGGER.error("Missing country in counter add event.");
            return;
        }

        String nameCountry = attribute.getValue();

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK);
        if (attribute == null) {
            LOGGER.error("Missing stack id in counter add event.");
            return;
        }

        StackMarker stackMarker = new StackMarker(Long.parseLong(attribute.getValue()), province);
        stackMarker.addCounter(new CounterMarker(diff.getIdObject(), markerUtils.getImageFromCounter(type, nameCountry)));
        province.addStack(stackMarker);
    }

    /**
     * Process the move counter diff event.
     *
     * @param diff involving a move counter.
     */
    private void moveCounter(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute == null) {
            LOGGER.error("Missing province in counter move event.");
            return;
        }
        Marker prov = countryMarkers.get(attribute.getValue());
        if (!(prov instanceof IMapMarker)) {
            LOGGER.error("province is not a IMapMarker.");
            return;
        }
        IMapMarker province = (IMapMarker) prov;

        StackMarker stack = null;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_FROM);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stack = findFirst(province.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        }
        if (stack == null) {
            LOGGER.error("Missing stack from in counter move event.");
            return;
        }

        CounterMarker counter = findFirst(stack.getCounters(), counter1 -> diff.getIdObject().equals(counter1.getId()));
        if (counter == null) {
            LOGGER.error("Missing counter in counter move event.");
            return;
        }

        StackMarker stackTo;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_TO);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stackTo = findFirst(province.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stackTo == null) {
                stackTo = new StackMarker(idStack, province);
                province.addStack(stackTo);
            }
        } else {
            LOGGER.error("Missing stack id in counter add event.");
            return;
        }

        stackTo.addCounter(counter);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_DEL);
        if (attribute != null) {
            destroyStack(province, attribute);
        }
    }

    /**
     * Process the remove counter diff event.
     *
     * @param diff involving a remove counter.
     */
    private void removeCounter(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute == null) {
            LOGGER.error("Missing province in counter move event.");
            return;
        }
        Marker prov = countryMarkers.get(attribute.getValue());
        if (!(prov instanceof IMapMarker)) {
            LOGGER.error("province is not a IMapMarker.");
            return;
        }
        IMapMarker province = (IMapMarker) prov;


        StackMarker stack = null;
        CounterMarker counter = null;
        for (StackMarker stackVo : province.getStacks()) {
            for (CounterMarker counterVo : stackVo.getCounters()) {
                if (diff.getIdObject().equals(counterVo.getId())) {
                    counter = counterVo;
                    stack = stackVo;
                    break;
                }
            }
            if (counter != null) {
                break;
            }
        }

        if (counter == null) {
            LOGGER.error("Missing counter in counter remove event.");
            return;
        }

        stack.removeCounter(counter);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_DEL);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            if (idStack.equals(stack.getId())) {
                province.removeStack(stack);
            } else {
                LOGGER.error("Stack to del is not the counter owner in counter remove event.");
            }
        }
    }

    /**
     * Process a stack diff event.
     *
     * @param diff involving a counter.
     */
    private void updateStack(Diff diff) {
        switch (diff.getType()) {
            case ADD:
                break;
            case MOVE:
                moveStack(diff);
                break;
            case REMOVE:
                break;
            default:
                break;
        }
    }

    /**
     * Process the move stack diff event.
     *
     * @param diff involving a add counter.
     */
    private void moveStack(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_FROM);
        if (attribute == null) {
            LOGGER.error("Missing province from in stack move event.");
            return;
        }

        Marker prov = countryMarkers.get(attribute.getValue());
        if (!(prov instanceof IMapMarker)) {
            LOGGER.error("province is not a IMapMarker.");
            return;
        }
        IMapMarker province = (IMapMarker) prov;

        StackMarker stack;
        Long idStack = diff.getIdObject();
        stack = findFirst(province.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        if (stack == null) {
            LOGGER.error("Missing stack in stack move event.");
            return;
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_TO);
        if (attribute == null) {
            LOGGER.error("Missing province to in stack move event.");
            return;
        }

        Marker provTo = countryMarkers.get(attribute.getValue());
        if (!(provTo instanceof IMapMarker)) {
            LOGGER.error("province is not a IMapMarker.");
            return;
        }
        IMapMarker provinceTo = (IMapMarker) provTo;
        provinceTo.addStack(stack);
    }

    /**
     * Generic destroyStack diff update.
     *
     * @param province  where the stack is.
     * @param attribute of type destroy stack.
     */
    private void destroyStack(IMapMarker province, DiffAttributes attribute) {
        Long idStack = Long.parseLong(attribute.getValue());
        StackMarker stack = findFirst(province.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        if (stack != null) {
            province.removeStack(stack);
        } else {
            LOGGER.error("Missing stack for destroy stack generic event.");
        }
    }
}
