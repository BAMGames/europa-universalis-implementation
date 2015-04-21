package com.mkl.eu.front.client.map;

import com.mkl.eu.front.client.map.component.InfoView;
import com.mkl.eu.front.client.map.component.ViewportRect;
import com.mkl.eu.front.client.map.handler.keyboard.MapKeyboardHandler;
import com.mkl.eu.front.client.map.handler.mouse.InfowViewMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MapMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MultipleMapMouseHandler;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import com.mkl.eu.front.client.map.marker.MyMarkerManager;
import com.mkl.eu.front.client.map.provider.EUProvider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import processing.core.PApplet;

import java.util.Map;

/**
 * Everything under the interactive Map.
 *
 * @author MKL
 */
public class InteractiveMap extends PApplet {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveMap.class);
    /** Interactive map. */
    private UnfoldingMap mapDetail;

    /** Small map showing the overview, i.e. the world. */
    private UnfoldingMap mapOverviewStatic;

    /** Interactive finder box atop the overview map. */
    private ViewportRect viewportRect;
    /** Information panel of the selected province. */
    private InfoView info;

    /**
     * Main method.
     *
     * @param args no args.
     */
    public static void main(String args[]) {
        PApplet.main(new String[]{"com.mkl.eu.front.client.map.InteractiveMap"});
        ApplicationContext context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        InteractiveMap mine = (InteractiveMap) context.getBean("mine");
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

    /** Set up the map and the markers. */
    public void setup() {
        size(1000, 600, OPENGL);
        if (frame != null) {
            frame.setResizable(true);
        }

        mapDetail = new UnfoldingMap(this, "detail", 0, 0, 800, 600, true, false, new EUProvider(this), null);
        mapDetail.setTweening(true);
        mapDetail.zoomToLevel(7);
        mapDetail.setZoomRange(5, 10);
        mapDetail.panTo(1300, -300);


        // Static overview map
        mapOverviewStatic = new UnfoldingMap(this, "overviewStatic", 805, 5, 185, 235, true, false, new EUProvider(this), null);
        mapOverviewStatic.zoomToLevel(4);
        mapOverviewStatic.panTo(new Location(12, 11));

        viewportRect = new ViewportRect(this);

        MarkerUtils markerUtils = new MarkerUtils(this);

        MyMarkerManager markerManager = new MyMarkerManager(markerUtils);

        info = new InfoView(this, markerManager, 805, 245, 185, 350);

        EventDispatcher eventDispatcher = new EventDispatcher();
        KeyboardHandler keyboardHandler = new MapKeyboardHandler(this, mapDetail);
        MapMouseHandler mouseHandler = new MapMouseHandler(this, markerManager, mapDetail);
        new MultipleMapMouseHandler(this, mapOverviewStatic, viewportRect, mapDetail);
        new InfowViewMouseHandler(this, info, mapDetail);

        eventDispatcher.addBroadcaster(keyboardHandler);
        eventDispatcher.addBroadcaster(mouseHandler);

        eventDispatcher.register(mapDetail, PanMapEvent.TYPE_PAN, mapDetail.getId());
        eventDispatcher.register(mapDetail, ZoomMapEvent.TYPE_ZOOM, mapDetail.getId());

        mapDetail.addMarkerManager(markerManager);

        // Load country polygons and adds them as markers
        Map<String, Marker> countryMarkers = markerUtils.createMarkers();
        mapDetail.addMarkers(countryMarkers.values().toArray(new Marker[countryMarkers.values().size()]));
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
}
