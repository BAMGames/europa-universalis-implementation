package com.mkl.eu.front.main;

import com.mkl.eu.front.provider.EUProvider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;
import processing.core.PApplet;

import java.util.Iterator;
import java.util.List;

/**
 * Test PApplet to test the interactive Map.
 *
 * @author MKL
 */
public class Mine extends PApplet {
    /** Interactive map. */
    private UnfoldingMap mapDetail;

    static public void main(String args[]) {
        PApplet.main(new String[]{"com.mkl.eu.front.main.Mine"});
    }

    /** Set up the map and the markers. */
    public void setup() {
        size(800, 600, OPENGL);

        mapDetail = new UnfoldingMap(this, "detail", new EUProvider(this));
        mapDetail.setTweening(true);
        mapDetail.zoomToLevel(7);
        mapDetail.setZoomRange(5, 10);
        mapDetail.panTo(1000, -300);
        MapUtils.createDefaultEventDispatcher(this, mapDetail);

        mapDetail.addMarkerManager(new MyMarkerManager());

        // Load country polygons and adds them as markers
        List<Feature> countries = GeoJSONReader.loadData(this, "data/map/v2/countries.geo.json");
        for (Iterator<Feature> country = countries.iterator(); country.hasNext(); ) {
            Feature c = country.next();
            if (!StringUtils.equals("Norvege", c.getId())) {
                country.remove();
            }
        }
        List<Marker> countryMarkers = MapUtils.createSimpleMarkers(countries);
        mapDetail.addMarkers(countryMarkers);

        for (Marker marker : countryMarkers) {

            // Encode value as brightness (values range: 0-1000)
            float transparency = map(0f, 0, 700, 10, 255);
//                    marker.setColor(color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()), transparency));
            marker.setColor(color(0, 0, 0, transparency));
        }
    }

    /**
     * Draw the PApplet.
     */
    public void draw() {
        background(0);

        mapDetail.draw();
    }
}
