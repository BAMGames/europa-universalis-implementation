package com.mkl.eu.front.client.map;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.map.component.InfoView;
import com.mkl.eu.front.client.map.component.ViewportRect;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.event.HoverEvent;
import com.mkl.eu.front.client.map.handler.keyboard.MapKeyboardHandler;
import com.mkl.eu.front.client.map.handler.mouse.InfoViewMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MapMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MultipleMapMouseHandler;
import com.mkl.eu.front.client.map.marker.*;
import com.mkl.eu.front.client.map.provider.EUProvider;
import com.mkl.eu.front.client.vo.AuthentHolder;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.*;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import processing.core.PApplet;
import processing.opengl.PSurfaceJOGLFixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Everything under the interactive Map.
 *
 * @author MKL
 */
@Component
@Scope(value = "prototype")
public class InteractiveMap extends PApplet implements MapEventListener, ApplicationContextAware {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveMap.class);
    /** Spring application context. */
    private ApplicationContext context;
    /** Game service. */
    @Autowired
    private IGameService gameService;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Interactive map. */
    private UnfoldingMap mapDetail;

    /** Small map showing the overview, i.e. the world. */
    private UnfoldingMap mapOverviewStatic;

    /** Interactive finder box atop the overview map. */
    private ViewportRect viewportRect;
    /** Information panel of the selected province. */
    private InfoView info;
    /** Markers of the loaded game. */
    private Map<String, Marker> countryMarkers;
    /** Game. */
    private Game game;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Components that listener to diffs. */
    private List<IDiffListenerContainer> components = new ArrayList<>();
    /** List of diffs listeners. */
    private List<IDiffListener> diffListeners = new ArrayList<>();
    /** Flag saying that the component has been initialized. */
    private boolean init;

    /**
     * Main method.
     *
     * @param args no args.
     */
    public static void main(String args[]) {
        PApplet.main(new String[]{"com.mkl.eu.front.client.map.InteractiveMap"});
    }

    /**
     * Constructor.
     *
     * @param game           the game to display.
     * @param gameConfig     game configuration.
     * @param countryMarkers country markers.
     */
    public InteractiveMap(Game game, GameConfiguration gameConfig, Map<String, Marker> countryMarkers) {
        this.game = game;
        this.gameConfig = gameConfig;
        this.countryMarkers = countryMarkers;
    }

    /** {@inheritDoc} */
    @Override
    public void settings() {
        super.settings();

        size(1000, 600, P2D);
//        size(1000, 600, "processing.javafx.PGraphicsFX2DFixed");
//        size(1000, 600, "processing.opengl.PGraphicsOpenGLFixed");
    }

    /** Set up the map and the markers. */
    public void setup() {
        init = true;
        countryMarkers.values().stream().filter(marker -> marker instanceof IMapMarker).forEach(marker -> {
            for (StackMarker stack : ((IMapMarker) marker).getStacks()) {
                for (CounterMarker counter : stack.getCounters()) {
                    counter.setImage(MarkerUtils.getImageFromCounter(counter.getCountry(), counter.getType().name(), this));
                }
            }
        });

        if (surface != null) {
            surface.setResizable(true);

            // We remove the listener that closes the sketch when
            // the window is closed and replaces it by a setVisible(false)
            if (surface.getNative() instanceof Window) {
                Window window = (Window) surface.getNative();

                WindowListener[] listeners = window.getWindowListeners();

                for (WindowListener listener : listeners) {
                    if (listener instanceof WindowAdapter) {
                        continue;
                    }

                    window.removeWindowListener(listener);
                }

                window.addWindowListener(new WindowAdapter() {

                    /** {@inheritDoc} */
                    @Override
                    public void windowDestroyNotify(WindowEvent e) {
                        setVisible(false);
                    }

                    /** {@inheritDoc} */
                    @Override
                    public void windowDestroyed(WindowEvent e) {
                        if (surface instanceof PSurfaceJOGLFixed) {
                            ((PSurfaceJOGLFixed) surface).destroy();
                        }
                    }
                });
            }
        }

        MyMarkerManager markerManager = context.getBean(MyMarkerManager.class, gameConfig);

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

        info = context.getBean(InfoView.class, this, markerManager, gameConfig);
        info.init(805, 245, 185, 350);

        EventDispatcher eventDispatcher = new EventDispatcher();
        MapKeyboardHandler keyboardHandler = new MapKeyboardHandler(this, gameService, authentHolder, gameConfig, mapDetail);
        MapMouseHandler mouseHandler = new MapMouseHandler(this, markerManager, mapDetail);
        new MultipleMapMouseHandler(this, mapOverviewStatic, viewportRect, mapDetail);
        InfoViewMouseHandler infoHandler = new InfoViewMouseHandler(this, info, mapDetail);

        eventDispatcher.addBroadcaster(keyboardHandler);
        eventDispatcher.addBroadcaster(mouseHandler);
        eventDispatcher.addBroadcaster(infoHandler);

        eventDispatcher.register(mapDetail, PanMapEvent.TYPE_PAN, mapDetail.getId());
        eventDispatcher.register(mapDetail, ZoomMapEvent.TYPE_ZOOM, mapDetail.getId());

        eventDispatcher.register(markerManager, DragEvent.TYPE_DRAG, markerManager.getId());
        eventDispatcher.register(markerManager, HoverEvent.TYPE_HOVER, markerManager.getId());
        eventDispatcher.register(info, DragEvent.TYPE_DRAG, info.getId());

        components.add(markerManager);
        components.add(info);
        components.add(keyboardHandler);

        for (IDiffListener diffListener : diffListeners) {
            markerManager.addDiffListener(diffListener);
            info.addDiffListener(diffListener);
            keyboardHandler.addDiffListener(diffListener);
        }

        mapDetail.addMarkers(countryMarkers.values().toArray(new Marker[countryMarkers.values().size()]));

        // Disable the auto-draw feature. Manual redraw on change.
//        noLoop();
    }

    /** @return the init. */
    public boolean isInit() {
        return init;
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
    public void exitActual() {
        // super.exitActual() terminates JVM, we don't want that.
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
     * Destroy the window.
     */
    public void destroy() {
        if (surface != null) {
            Object nat = surface.getNative();
            if (nat instanceof Window) {
                ((Window) nat).destroy();
            }

            exit();
        }
    }

    /**
     * Request the focus.
     */
    public void requestFocus() {
        if (surface != null) {
            Object nat = surface.getNative();
            if (nat instanceof Window) {
                ((Window) nat).requestFocus();
            }
        }
    }

    /**
     * @return the visibility of the window.
     */
    public boolean isVisible() {
        boolean visible = false;
        if (surface != null) {
            Object nat = surface.getNative();
            if (nat instanceof Window) {
                visible = ((Window) nat).isVisible();
            }
        }

        return visible;
    }

    /**
     * Sets the visibility of the window.
     *
     * @param visible the visible to set.
     */
    public void setVisible(boolean visible) {
        if (surface != null) {
            Object nat = surface.getNative();
            if (nat instanceof Window) {
                ((Window) nat).setVisible(visible);
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
            for (IDiffListenerContainer component : components) {
                component.addDiffListener(diffListener);
            }
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

        CounterFaceTypeEnum type = CounterFaceTypeEnum.valueOf(attribute.getValue());

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTRY);
        if (attribute == null) {
            LOGGER.error("Missing country in counter add event.");
            return;
        }

        String nameCountry = attribute.getValue();

        final DiffAttributes attributeIdStack = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK);
        if (attributeIdStack == null) {
            LOGGER.error("Missing stack id in counter add event.");
            return;
        }
        // FIXME stack may already exist

        Stack stackVO = CommonUtil.findFirst(game.getStacks().stream(), stack -> stack.getId().equals(Long.parseLong(attributeIdStack.getValue())));
        if (stackVO == null) {
            LOGGER.error("Missing stack in the game.");
            return;
        }
        StackMarker stackMarker = new StackMarker(stackVO, province);
        stackMarker.addCounter(new CounterMarker(diff.getIdObject(), nameCountry, type, MarkerUtils.getImageFromCounter(nameCountry, type.name(), this)));
        province.addStack(stackMarker);
    }

    /**
     * Process the move counter diff event.
     *
     * @param diff involving a move counter.
     */
    private void moveCounter(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_FROM);
        if (attribute == null) {
            LOGGER.error("Missing province_from in counter move event.");
            return;
        }
        Marker provFrom = countryMarkers.get(attribute.getValue());
        if (!(provFrom instanceof IMapMarker)) {
            LOGGER.error("province_from is not a IMapMarker.");
            return;
        }
        IMapMarker provinceFrom = (IMapMarker) provFrom;

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_TO);
        if (attribute == null) {
            LOGGER.error("Missing province_to in counter move event.");
            return;
        }
        Marker provTo = countryMarkers.get(attribute.getValue());
        if (!(provTo instanceof IMapMarker)) {
            LOGGER.error("province_to is not a IMapMarker.");
            return;
        }
        IMapMarker provinceTo = (IMapMarker) provTo;

        StackMarker stack = null;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_FROM);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stack = findFirst(provinceTo.getStacks(), stack1 -> idStack.equals(stack1.getId()));
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
            stackTo = findFirst(provinceTo.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stackTo == null) {
                Stack stackVO = CommonUtil.findFirst(game.getStacks().stream(), stackItem -> stackItem.getId().equals(idStack));
                if (stackVO == null) {
                    LOGGER.error("Missing stack in the game.");
                    return;
                }
                stackTo = new StackMarker(stackVO, provinceTo);
                provinceTo.addStack(stackTo);
            }
        } else {
            LOGGER.error("Missing stack id in counter add event.");
            return;
        }

        stackTo.addCounter(counter);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_DEL);
        if (attribute != null) {
            destroyStack(provinceFrom, attribute);
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

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
