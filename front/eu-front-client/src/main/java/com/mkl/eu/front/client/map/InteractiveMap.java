package com.mkl.eu.front.client.map;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.DiffListener;
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
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import processing.core.PApplet;

import java.util.Map;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Everything under the interactive Map.
 *
 * @author MKL
 */
public class InteractiveMap extends PApplet implements MapEventListener, DiffListener {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveMap.class);
    /** Utility for markers. */
    @Autowired
    private MarkerUtils markerUtils;
    /** Marker manager. */
    @Autowired
    private MyMarkerManager markerManager;
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
     * Store the markers of the given game.
     *
     * @param game to load.
     */
    public void setGame(Game game) {
        countryMarkers = markerUtils.createMarkers(game);
    }

    /** Set up the map and the markers. */
    public void setup() {
        size(1000, 600, OPENGL);
        if (frame != null) {
            frame.setResizable(true);
        }

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
        KeyboardHandler keyboardHandler = new MapKeyboardHandler(this, mapDetail);
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
    public void addDiffListener(DiffListener diffListener) {
        markerManager.addDiffListener(diffListener);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void update(DiffEvent event) {
        for (Diff diff : event.getDiffs()) {
            switch (diff.getTypeObject()) {
                case COUNTER:
                    updateCounter(diff);
                    break;
                case STACK:
                    break;
                default:
                    break;
            }
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
                break;
            case REMOVE:
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

        StackMarker stackMarker = new StackMarker(province);
        stackMarker.addCounter(new CounterMarker(markerUtils.getImageFromCounter(type, nameCountry)));
        province.addStack(stackMarker);
    }
}
