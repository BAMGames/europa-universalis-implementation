package com.mkl.eu.front.client.map.marker;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.Feature.FeatureType;
import de.fhpotsdam.unfolding.data.MultiFeature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A factory to create markers from features. The factory creates appropriate markers the each feature type, e.g. a
 * polygon marker for a polygon feature, and handle multi-marker from multi-feature, as well.
 * </p>
 * <p>
 * See the following example on how to use this factory to create your own custom markers. For this, set the marker
 * class for each feature type with the {@link #setPointClass(Class)} {@link #setLineClass(Class)}
 * {@link #setPolygonClass(Class)} {@link #setMultiPolygonClass(Class)} methods.
 * </p>
 * <p/>
 * <pre>
 * MarkerFactory markerFactory = new MarkerFactory();
 * markerFactory.setPolygonClass(MyPolygonMarker.class);
 * List&lt;Marker&gt; markers = markerFactory.createMarkers(features);
 * </pre>
 * <p>
 * By default, this factory creates the simple markers provided by Unfolding, such as {@link SimplePointMarker}.
 * </p>
 * <p>
 * Forking from UnfoldingMaps because MultiPolygon can't be overwriten.
 */
public class MarkerFactory {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkerFactory.class);
    /** Concrete classes for markers. */
    private HashMap<FeatureType, Class<?>> featureMarkerMap;

    /** Creates a new MarkerFactory. */
    public MarkerFactory() {
        featureMarkerMap = new HashMap<>();
        featureMarkerMap.put(FeatureType.POINT, SimplePointMarker.class);
        featureMarkerMap.put(FeatureType.LINES, SimpleLinesMarker.class);
        featureMarkerMap.put(FeatureType.POLYGON, SimplePolygonMarker.class);
        featureMarkerMap.put(FeatureType.MULTI, MultiMarker.class);
    }

    /**
     * Creates markers for each feature. Marker depends on feature type.
     *
     * @param features The list of features.
     * @return A list of markers.
     */
    public List<Marker> createMarkers(List<Feature> features) {
        List<Marker> markers = new ArrayList<>();

        try {
            for (Feature feature : features) {
                Marker marker = createMarker(feature);
                markers.add(marker);
            }

        } catch (Exception e) {
            LOGGER.error("Can't create marker.", e);
            return null;
        }

        return markers;
    }

    /**
     * Creates markers for each feature. Marker depends on feature type.
     *
     * @param features The list of features.
     * @return A map of id -> markers.
     */
    public Map<String, Marker> createMapMarkers(List<Feature> features) {
        Map<String, Marker> markers = new HashMap<>();

        try {
            for (Feature feature : features) {
                Marker marker = createMarker(feature);
                markers.put(marker.getId(), marker);
            }

        } catch (Exception e) {
            LOGGER.error("Can't create marker.", e);
            return null;
        }

        return markers;
    }

    /**
     * Creates a marker for the feature. Marker depends on feature type.
     *
     * @param feature The feature.
     * @return A marker of the appropriate type with ID and properties.
     * @throws Exception exception.
     */
    public Marker createMarker(Feature feature) throws Exception {
        Marker marker = null;

        switch (feature.getType()) {
            case POINT:
                marker = createPointMarker((PointFeature) feature);
                break;
            case LINES:
                marker = createLinesMarker((ShapeFeature) feature);
                break;
            case POLYGON:
                marker = createPolygonMarker((ShapeFeature) feature);
                break;
            case MULTI:
                marker = createMultiMarker((MultiFeature) feature);
                break;
        }

        // Set id
        marker.setId(feature.getId());

        // Copy properties
        marker.setProperties(feature.getProperties());

        return marker;
    }

    /**
     * Sets the marker class for markers to be created for point features.
     *
     * @param pointMarkerClass A marker class.
     */
    public void setPointClass(Class pointMarkerClass) {
        featureMarkerMap.remove(FeatureType.POINT);
        featureMarkerMap.put(FeatureType.POINT, pointMarkerClass);
    }

    /**
     * Sets the marker class for markers to be created for lines features.
     *
     * @param lineMarkerClass A marker class.
     */
    public void setLineClass(Class lineMarkerClass) {
        featureMarkerMap.remove(FeatureType.LINES);
        featureMarkerMap.put(FeatureType.LINES, lineMarkerClass);
    }

    /**
     * Sets the marker class for markers to be created for polygon features.
     *
     * @param polygonMarkerClass A marker class.
     */
    public void setPolygonClass(Class polygonMarkerClass) {
        featureMarkerMap.remove(FeatureType.POLYGON);
        featureMarkerMap.put(FeatureType.POLYGON, polygonMarkerClass);
    }

    /**
     * Sets the marker class for markers to be created for multipolygon features.
     *
     * @param multiPolygonMarkerClass A marker class.
     */
    public void setMultiPolygonClass(Class multiPolygonMarkerClass) {
        featureMarkerMap.remove(FeatureType.MULTI);
        featureMarkerMap.put(FeatureType.MULTI, multiPolygonMarkerClass);
    }

    /**
     * Create a point marker for a PointFeature.
     *
     * @param feature source.
     * @return a marker.
     * @throws Exception exception.
     */
    protected Marker createPointMarker(PointFeature feature) throws Exception {
        Class<?> markerClass = featureMarkerMap.get(feature.getType());
        Marker marker;
        try {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(Location.class, HashMap.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocation(), feature.getProperties());
        } catch (NoSuchMethodException e) {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(Location.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocation());
            marker.setProperties(feature.getProperties());
        }
        return marker;
    }

    /**
     * Create a line marker for a ShapeFeature.
     *
     * @param feature source.
     * @return a marker.
     * @throws Exception exception.
     */
    protected Marker createLinesMarker(ShapeFeature feature) throws Exception {
        Class<?> markerClass = featureMarkerMap.get(feature.getType());
        Marker marker;
        try {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class, HashMap.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocations(), feature.getProperties());
        } catch (NoSuchMethodException e) {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocations());
            marker.setProperties(feature.getProperties());
        }
        return marker;
    }

    /**
     * Create a polygone marker for a ShapeFeature.
     *
     * @param feature source.
     * @return a marker.
     * @throws Exception exception.
     */
    protected Marker createPolygonMarker(ShapeFeature feature) throws Exception {
        Class<?> markerClass = featureMarkerMap.get(feature.getType());
        Marker marker;
        try {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class, HashMap.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocations(), feature.getProperties());
        } catch (NoSuchMethodException e) {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class);
            marker = (Marker) markerConstructor.newInstance(feature.getLocations());
            marker.setProperties(feature.getProperties());
        }

        // Set interior ring locations if existing, and if supported by the markerClass
        if (feature.getInteriorRings() != null) {
            try {
                Method method = markerClass.getMethod("setInteriorRings", List.class);
                method.invoke(marker, feature.getInteriorRings());
            } catch (NoSuchMethodException e) {
                LOGGER.warn("Marker of class {} does not support interiorRings", markerClass);
            }
        }

        return marker;
    }

    /**
     * Create a multipolygon marker for a MultiFeature.
     *
     * @param feature source.
     * @return a marker.
     * @throws Exception exception.
     */
    private Marker createMultiMarker(MultiFeature feature) throws Exception {
        List<Marker> markers = new ArrayList<>();
        for (Feature subFeature : feature.getFeatures()) {
            Marker marker = createMarker(subFeature);
            marker.setId(feature.getId());
            marker.setProperties(feature.getProperties());
            markers.add(marker);
        }

        Class<?> markerClass = featureMarkerMap.get(feature.getType());
        Marker marker;
        try {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class, HashMap.class);
            marker = (Marker) markerConstructor.newInstance(markers, feature.getProperties());
        } catch (NoSuchMethodException e) {
            Constructor markerConstructor = markerClass.getDeclaredConstructor(List.class);
            marker = (Marker) markerConstructor.newInstance(markers);
            marker.setProperties(feature.getProperties());
        }

        return marker;
    }
}
