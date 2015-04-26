package com.mkl.eu.front.client.map;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.EuropeanProvince;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.front.client.map.component.InfoView;
import com.mkl.eu.front.client.map.component.ViewportRect;
import com.mkl.eu.front.client.map.handler.event.DragEvent;
import com.mkl.eu.front.client.map.handler.keyboard.MapKeyboardHandler;
import com.mkl.eu.front.client.map.handler.mouse.InfowViewMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MapMouseHandler;
import com.mkl.eu.front.client.map.handler.mouse.MultipleMapMouseHandler;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import com.mkl.eu.front.client.map.marker.MyMarkerManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Everything under the interactive Map.
 *
 * @author MKL
 */
public class InteractiveMap extends PApplet implements MapEventListener {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractiveMap.class);
    /** Game service. */
    @Autowired
    private IGameService gameService;
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
        InfowViewMouseHandler infoHandler = new InfowViewMouseHandler(this, info, mapDetail);

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

        // Load country polygons and adds them as markers
//        Game game = mockGame();
        Game game = gameService.loadGame(1L);
        Map<String, Marker> countryMarkers = markerUtils.createMarkers(game);
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
     * @return a mocked game.
     */
    private Game mockGame() {
        Game game = new Game();

        List<Stack> stacks = new ArrayList<>();
        Stack stack1 = new Stack();
        stack1.setProvince(new EuropeanProvince());
        stack1.getProvince().setName("Prypeć");
        Counter counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        stack1.getCounters().add(counter1);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince(new EuropeanProvince());
        stack1.getProvince().setName("Prypeć");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        Counter counter2 = new Counter();
        counter2.setCountry(new Country());
        counter2.getCountry().setName("FRA");
        counter2.setType(CounterTypeEnum.ARMY_MINUS);
        stack1.getCounters().add(counter2);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince(new EuropeanProvince());
        stack1.getProvince().setName("Prypeć");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        counter2 = new Counter();
        counter2.setCountry(new Country());
        counter2.getCountry().setName("FRA");
        counter2.setType(CounterTypeEnum.ARMY_MINUS);
        stack1.getCounters().add(counter2);
        Counter counter3 = new Counter();
        counter3.setCountry(new Country());
        counter3.getCountry().setName("FRA");
        counter3.setType(CounterTypeEnum.LAND_DETACHMENT);
        stack1.getCounters().add(counter3);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince(new EuropeanProvince());
        stack1.getProvince().setName("Languedoc");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        stacks.add(stack1);

        game.setStacks(stacks);

        return game;
    }
}
