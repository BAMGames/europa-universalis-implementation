package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.map.InteractiveMap;
import com.mkl.eu.front.client.vo.Border;
import com.thoughtworks.xstream.XStream;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.MarkerFactory;
import de.fhpotsdam.unfolding.marker.Marker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility for markers manipulation.
 *
 * @author MKL
 */
public final class MarkerUtils {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerUtils.class);
    /** Cache of the images for the counters. Used to not load several times the same image. first key: country, second key: type. */
    private static Map<String, Map<String, PImage>> countersImage = new HashMap<>();

    /**
     * Constructor.
     */
    private MarkerUtils() {

    }

    /**
     * Create the Markers from various sources.
     *
     * @param game holding the stacks to add.
     * @return the markers to add to the maps.
     */
    public static Map<String, Marker> createMarkers(Game game) {
        // TODO TG-15 configure externalization
        List<Feature> countries = GeoJSONReader.loadDataFromJSON(null, GeoJSONReaderUtil.readJson("data/map/v2/countries.geo.json"));

        XStream xstream = new XStream();
        xstream.processAnnotations(Border.class);

        // TODO TG-15 configure externalization
        //noinspection unchecked
        List<Border> borders = (List<Border>) xstream.fromXML(new File("data/map/v2/borders.xml"));
        MarkerFactory markerFactory = new MarkerFactory();
        markerFactory.setPolygonClass(ProvinceMarker.class);
        markerFactory.setMultiClass(MultiProvinceMarker.class);
        Map<String, Marker> countryMarkers = markerFactory.createMarkers(countries).stream()
                .collect(Collectors.toMap(Marker::getId, Function.<Marker>identity()));

        for (Marker marker : countryMarkers.values()) {

            // Encode value as brightness (values range: 0-1000)
            float transparency = PApplet.map(500f, 0, 700, 10, 255);
            int red = (int) (255 * Math.random());
            int green = (int) (255 * Math.random());
            int blue = (int) (255 * Math.random());
            marker.setColor(createColor(red, green, blue, transparency));
            if (marker instanceof IMapMarker) {
                IMapMarker mapMarker = (IMapMarker) marker;
                int highlight = 25;
                mapMarker.setHighlightColor(createColor(Math.min(255, red + highlight), Math.min(255, green + highlight), Math.min(255, blue + highlight), transparency));
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
                    StackMarker stackMarker = new StackMarker(stack, mapMarker);
                    for (Counter counter : stack.getCounters()) {
                        // Images will be loaded later
                        stackMarker.addCounter(new CounterMarker(counter.getId(), counter.getCountry(), counter.getType(), null));
                    }
                    mapMarker.addStack(stackMarker);
                });
            }
        }
        return countryMarkers;
    }

    /**
     * Creates a color like a PApplet without a PGGraphics would do.
     * Method used in order to create markers before PApplet is initialized.
     *
     * @param v1    red value.
     * @param v2    green value.
     * @param v3    blue value.
     * @param alpha transparency value.
     * @return color understood by processing API.
     */
    public static int createColor(float v1, float v2, float v3, float alpha) {
        if (alpha > 255.0F) {
            alpha = 255.0F;
        } else if (alpha < 0.0F) {
            alpha = 0.0F;
        }

        if (v1 > 255.0F) {
            v1 = 255.0F;
        } else if (v1 < 0.0F) {
            v1 = 0.0F;
        }

        if (v2 > 255.0F) {
            v2 = 255.0F;
        } else if (v2 < 0.0F) {
            v2 = 0.0F;
        }

        if (v3 > 255.0F) {
            v3 = 255.0F;
        } else if (v3 < 0.0F) {
            v3 = 0.0F;
        }

        return (int) alpha << 24 | (int) v1 << 16 | (int) v2 << 8 | (int) v3;
    }

    /**
     * Retrieves the image of the counter.
     *
     * @param counter the counter.
     * @param pApplet     pApplet rendering the image.
     * @return the image of the counter.
     */
    public static PImage getImageFromCounter(CounterMarker counter, InteractiveMap pApplet) {
        return getImageFromCounter(counter.getCountry(), counter.getType().name(), pApplet);
    }

    /**
     * Retrieves the image of the counter.
     *
     * @param type        of the counter.
     * @param nameCountry name of the country of the counter.
     * @param pApplet     pApplet rendering the image.
     * @return the image of the counter.
     */
    public static PImage getImageFromCounter(String nameCountry, String type, InteractiveMap pApplet) {
        PImage image = getImageFromCache(nameCountry, type);
        if (image == null && pApplet.isInit()) {
            image = pApplet.loadImage(getImagePath(nameCountry, type));
            putImageInCache(nameCountry, type, image);
        }

        return image;
    }

    /**
     * @param counter the counter.
     * @return the image path of the counter.
     */
    public static String getImagePath(Counter counter) {
        return getImagePath(counter.getCountry(), counter.getType().name());
    }

    /**
     * @param country of the counter.
     * @param type    of the counter.
     * @return the image path of the counter.
     */
    public static String getImagePath(String country, String type) {
        // TODO TG-15 configure externalization
        StringBuilder path = new StringBuilder("data/counters/v2/counter_8/");
        if (country != null) {
            path.append(country)
                    .append("/").append(country).append("_");
        }
        path.append(type).append(".png");
        return path.toString();
    }

    /**
     * Retrieve an image of counter from the cache.
     *
     * @param country of the counter.
     * @param type    of the counter.
     * @return the image of the counter from the cache.
     */
    private static PImage getImageFromCache(String country, String type) {
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
    private synchronized static void putImageInCache(String country, String type, PImage image) {
        Map<String, PImage> countryImages = countersImage.get(country);
        if (countryImages == null) {
            countryImages = new HashMap<>();
            countersImage.put(country, countryImages);
        }

        countryImages.put(type, image);
    }
}
