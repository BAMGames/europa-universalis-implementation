package com.mkl.eu.front.main;

import com.mkl.eu.front.map.handler.MapKeyboardHandler;
import com.mkl.eu.front.map.handler.MapMouseHandler;
import com.mkl.eu.front.map.marker.MyMarkerManager;
import com.mkl.eu.front.map.marker.ProvinceMarker;
import com.mkl.eu.front.provider.EUProvider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.MultiFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.interactions.KeyboardHandler;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

/**
 * Test PApplet to test the interactive Map.
 *
 * @author MKL
 */
public class Mine extends PApplet {
    /** Color mode. */
    private static boolean withColor = false;
    /** Interactive map. */
    private UnfoldingMap mapDetail;

    static public void main(String args[]) {
        PApplet.main(new String[]{"com.mkl.eu.front.main.Mine"});
    }

    /** Switch the color mode. */
    public static void switchColor() {
        withColor = !withColor;
    }

    /** @return the withColor. */
    public static boolean isWithColor() {
        return withColor;
    }

    /** Set up the map and the markers. */
    public void setup() {
        size(800, 600, OPENGL);

        mapDetail = new UnfoldingMap(this, "detail", new EUProvider(this));
        mapDetail.setTweening(true);
        mapDetail.zoomToLevel(7);
        mapDetail.setZoomRange(5, 10);
        mapDetail.panTo(1000, -300);
        EventDispatcher eventDispatcher = new EventDispatcher();
        KeyboardHandler keyboardHandler = new MapKeyboardHandler(this, mapDetail);
        MapMouseHandler mouseHandler = new MapMouseHandler(this, mapDetail);

        eventDispatcher.addBroadcaster(keyboardHandler);
        eventDispatcher.addBroadcaster(mouseHandler);

        eventDispatcher.register(mapDetail, PanMapEvent.TYPE_PAN, mapDetail.getId());
        eventDispatcher.register(mapDetail, ZoomMapEvent.TYPE_ZOOM, mapDetail.getId());

        mapDetail.addMarkerManager(new MyMarkerManager());

        // Load country polygons and adds them as markers
        List<Feature> countries = GeoJSONReader.loadData(this, "data/map/v2/countries.geo.json");
//        for (Iterator<Feature> country = countries.iterator(); country.hasNext(); ) {
//            Feature c = country.next();
//            if (!StringUtils.equals("Highlands", c.getId())) {
//                country.remove();
//            }
//        }
        List<Marker> countryMarkers = createSimpleMarkers(countries);
        mapDetail.addMarkers(countryMarkers);

        for (Marker marker : countryMarkers) {

            // Encode value as brightness (values range: 0-1000)
            float transparency = map(500f, 0, 700, 10, 255);
            marker.setColor(color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()), transparency));
//            marker.setColor(color(0, 0, 0, transparency));
        }
    }

    /** Draw the PApplet. */
    public void draw() {
        background(0);

        mapDetail.draw();
    }

    private List<Marker> createSimpleMarkers(List<Feature> countries) {
        List<Marker> markers = new ArrayList<>();

        for (Feature feature : countries) {
            if (feature instanceof ShapeFeature) {
                Marker province = new ProvinceMarker(((ShapeFeature) feature).getLocations(), feature.getProperties(), feature.getId());
                markers.add(province);
            } else if (feature instanceof MultiFeature) {
                MultiMarker multiMarker = new MultiMarker();
                multiMarker.setProperties(feature.getProperties());

                for (Feature feat : ((MultiFeature) feature).getFeatures()) {
                    Marker province = new ProvinceMarker(((ShapeFeature) feat).getLocations(), feat.getProperties(), feature.getId());
                    multiMarker.addMarkers(province);
                }

                markers.add(multiMarker);
            }
        }

        return markers;
    }
}
