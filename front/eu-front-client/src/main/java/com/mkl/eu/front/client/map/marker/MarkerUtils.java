package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.vo.Border;
import com.thoughtworks.xstream.XStream;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class description.
 *
 * @author MKL
 */
@Component
@Scope(value = "prototype")
public class MarkerUtils {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerUtils.class);
    /** PApplet. */
    private PApplet pApplet;
    /** Cache of the images for the counters. Used to not load several times the same image. first key: country, second key: type. */
    private static Map<String, Map<String, PImage>> countersImage = new HashMap<>();

    /**
     * Constructor.
     *
     * @param pApplet the pApplet to set.
     */
    public MarkerUtils(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    /**
     * Create the Markers from various sources.
     *
     * @param game holding the stacks to add.
     * @return the markers to add to the maps.
     */
    public Map<String, Marker> createMarkers(Game game) {
        // TODO configure
        List<Feature> countries = GeoJSONReader.loadData(pApplet, "data/map/v2/countries.geo.json");

        XStream xstream = new XStream();
        xstream.processAnnotations(Border.class);

        // TODO configure
        //noinspection unchecked
        List<Border> borders = (List<Border>) xstream.fromXML(new File("data/map/v2/borders.xml"));
        MarkerFactory markerFactory = new MarkerFactory();
        markerFactory.setPolygonClass(ProvinceMarker.class);
        markerFactory.setMultiPolygonClass(MultiProvinceMarker.class);
        Map<String, Marker> countryMarkers = markerFactory.createMapMarkers(countries);

        for (Marker marker : countryMarkers.values()) {

            // Encode value as brightness (values range: 0-1000)
            float transparency = PApplet.map(500f, 0, 700, 10, 255);
            int red = (int) (255 * Math.random());
            int green = (int) (255 * Math.random());
            int blue = (int) (255 * Math.random());
            marker.setColor(pApplet.color(red, green, blue, transparency));
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;
                int highlight = 25;
                mapMarker.setHighlightColor(pApplet.color(Math.min(255, red + highlight), Math.min(255, green + highlight), Math.min(255, blue + highlight), transparency));
                for (Border border : borders) {
                    if (StringUtils.equals(border.getFirst(), marker.getId()) || StringUtils.equals(border.getSecond(), marker.getId())) {
                        String nameToSeek = border.getFirst();
                        if (StringUtils.equals(nameToSeek, marker.getId())) {
                            nameToSeek = border.getSecond();
                        }

                        Marker provinceToSeek = countryMarkers.get(nameToSeek);

                        if (provinceToSeek instanceof IMapMarker) {
                            mapMarker.addNeighbours(new BorderMarker((IMapMarker) provinceToSeek, border.getType()));
                        } else {
                            LOGGER.error("Can't find province {}.", nameToSeek);
                        }
                    }
                }

                game.getStacks().stream().filter(stack -> StringUtils.equals(stack.getProvince(), marker.getId())).forEach(stack -> {
                    StackMarker stackMarker = new StackMarker(stack.getId(), mapMarker);
                    for (Counter counter : stack.getCounters()) {
                        stackMarker.addCounter(new CounterMarker(counter.getId(), counter.getCountry(), counter.getType(), getImageFromCounter(counter)));
                    }
                    mapMarker.addStack(stackMarker);
                });
            }
        }
        return countryMarkers;
    }

    /**
     * Retrieves the image of the counter.
     *
     * @param counter whose we want the image.
     * @return the image of the counter.
     */
    public PImage getImageFromCounter(Counter counter) {
        return getImageFromCounter(counter.getCountry(), counter.getType().name());
    }

    /**
     * Retrieves the image of the counter.
     *
     * @param type        of the counter.
     * @param nameCountry name of the country of the counter.
     * @return the image of the counter.
     */
    public PImage getImageFromCounter(String nameCountry, String type) {
        PImage image = getImageFromCache(nameCountry, type);
        if (image == null) {
            // TODO configure
            StringBuilder path = new StringBuilder("data/counters/v2/counter_8/");
            if (nameCountry != null) {
                path.append(nameCountry)
                        .append("/").append(nameCountry).append("_");
            }
            path.append(type).append(".png");

            image = pApplet.loadImage(path.toString());
            putImageInCache(nameCountry, type, image);
        }

        return image;
    }

    /**
     * Retrieve an image of counter from the cache.
     *
     * @param country of the counter.
     * @param type    of the counter.
     * @return the image of the counter from the cache.
     */
    private PImage getImageFromCache(String country, String type) {
        PImage image = null;

        Map<String, PImage> countryImages = countersImage.get(country);
        if (countryImages != null) {
            image = countryImages.get(type);
        }

        return image;
    }

    /**
     * Put an image of counter in the cache.
     *
     * @param country of the counter.
     * @param type    of the counter.
     * @param image   to store.
     */
    private synchronized void putImageInCache(String country, String type, PImage image) {
        Map<String, PImage> countryImages = countersImage.get(country);
        if (countryImages == null) {
            countryImages = new HashMap<>();
            countersImage.put(country, countryImages);
        }

        countryImages.put(type, image);
    }
}
