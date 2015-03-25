package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.Country;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.front.map.vo.Border;
import com.thoughtworks.xstream.XStream;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class description.
 *
 * @author MKL
 */
public final class MarkerUtils {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerUtils.class);

    /**
     * No instance of this class.
     */
    private MarkerUtils() {

    }

    /**
     * Create the Markers from various sources.
     * @param pApplet to compute some colors.
     * @return the markers to add to the maps.
     */
    public static Map<String, Marker> createMarkers(PApplet pApplet) {
        List<Feature> countries = GeoJSONReader.loadData(pApplet, "data/map/v2/countries.geo.json");

        XStream xstream = new XStream();
        xstream.processAnnotations(Border.class);

        List<Border> borders = (List<Border>)xstream.fromXML(new File("data/map/v2/borders.xml"));

//        for (Iterator<Feature> country = countries.iterator(); country.hasNext(); ) {
//            Feature c = country.next();
//            if (!StringUtils.equals("Highlands", c.getId())) {
//                country.remove();
//            }
//        }
        MarkerFactory markerFactory = new MarkerFactory();
        markerFactory.setPolygonClass(ProvinceMarker.class);
        markerFactory.setMultiPolygonClass(MultiProvinceMarker.class);
        Map<String, Marker> countryMarkers = markerFactory.createMapMarkers(countries);
        List<Counter> counters = new ArrayList<>();
        Counter counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setProvince("Île-de-France");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        counters.add(counter1);
        counters.add(counter1);
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setProvince("Languedoc");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        counters.add(counter1);

        for (Marker marker : countryMarkers.values()) {

            // Encode value as brightness (values range: 0-1000)
            float transparency = PApplet.map(500f, 0, 700, 10, 255);
            int red = (int) (255 * Math.random());
            int green = (int) (255 * Math.random());
            int blue = (int) (255 * Math.random());
            marker.setColor(pApplet.color(red, green, blue, transparency));
            if (marker instanceof IMapMarker) {
                int highlight = 25;
                ((IMapMarker)marker).setHighlightColor(pApplet.color(Math.min(255, red + highlight), Math.min(255, green + highlight), Math.min(255, blue + highlight), transparency));
                for (Border border: borders) {
                    if (StringUtils.equals(border.getFirst(), marker.getId()) || StringUtils.equals(border.getSecond(), marker.getId())) {
                        String nameToSeek = border.getFirst();
                        if (StringUtils.equals(nameToSeek, marker.getId())) {
                            nameToSeek = border.getSecond();
                        }

                        Marker provinceToSeek = countryMarkers.get(nameToSeek);

                        if (provinceToSeek instanceof IMapMarker) {
                            ((IMapMarker) marker).addNeighbours(new BorderMarker((IMapMarker) provinceToSeek, border.getType()));
                        } else {
                            LOGGER.error("Can't find province {}.", nameToSeek);
                        }
                    }
                }

                for (Counter counter: counters) {
                    if (StringUtils.equals(counter.getProvince(), marker.getId())) {
                        ((IMapMarker) marker).addCounter(new CounterMarker(counter, getImageFromCounter(counter, pApplet)));
                    }
                }
            }
        }
        return countryMarkers;
    }

    /**
     * Retrieves the image of the counter.
     * @param counter whose we want the image.
     * @param pApplet to load the image.
     * @return the image of the counter.
     */
    private static PImage getImageFromCounter(Counter counter, PApplet pApplet) {
        StringBuilder path = new StringBuilder("data/map/v2/counters/").append(counter.getCountry().getName())
                .append("/").append(counter.getCountry().getName()).append("_")
                .append(counter.getType().name()).append(".png");

        return pApplet.loadImage(path.toString());
    }
}
