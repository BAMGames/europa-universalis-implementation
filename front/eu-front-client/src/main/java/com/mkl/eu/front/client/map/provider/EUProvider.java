package com.mkl.eu.front.client.map.provider;

import de.fhpotsdam.unfolding.core.Coordinate;
import de.fhpotsdam.unfolding.geo.MercatorProjection;
import de.fhpotsdam.unfolding.geo.Transformation;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Map Provider for the European and ROTW maps.
 * First check in local cache on disk if map is present, then ask
 * a distant server and caches it on disk.
 *
 * @author MKL
 */
public class EUProvider extends AbstractMapProvider {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EUProvider.class);
    /** The parent applet. */
    protected PApplet applet;

    /**
     * Default constructor.
     * @param applet the parent applet.
     */
    public EUProvider(PApplet applet) {
        super(new MercatorProjection(26, new Transformation(1.068070779e7, 0.0, 3.355443185e7, 0.0, -1.068070890e7,
                3.355443057e7)));

        this.applet = applet;
    }

    /**
     * Retrieves a string which is part of the final url based on the coordinate.
     * @param coordinate of the portion of the map.
     * @return part of the final url.
     */
    private String getZoomString(Coordinate coordinate) {
        int x = (int) coordinate.column;
        int y = (int) coordinate.row;
        int zoom = (int) coordinate.zoom;

        int z = zoom - 4;
        if (z < 0) return "blank/256x256";
        int numTiles = 1 << z;
        int newx = x - (1 << (zoom - 1));
        int newy = -y - 1 + (1 << (zoom - 1));
        if (newx < 0 || newx >= numTiles) return "blank/bkgnd";
        if (newy < 0 || newy >= numTiles) return "blank/bkgnd";
        return "tile_" +
                z + "/" + newx + "_" + (newy);
    }

    /** {@inheritDoc} */
    @Override
    public int tileWidth() {
        return 256;
    }

    /** {@inheritDoc} */
    @Override
    public int tileHeight() {
        return 256;
    }


    /** {@inheritDoc} */
    @Override
    public PImage getTile(Coordinate coordinate) {
        return applet.loadImage(getImagePath(getZoomString(coordinate)));
    }

    /**
     * Returns the path the portion of the map will be stored on disk.
     * @param subPath name of the tile.
     * @return the path the portion of the map will be stored on disk.
     */
    private String getImagePath(String subPath) {
        // TODO TG-15 configure
        return "data/map/v2/carte/" + subPath + ".png";
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTileUrls(Coordinate coordinate) {
        LOGGER.debug("Demande de {} / {} / {}", coordinate.zoom, coordinate.column, coordinate.row);

        String subPath = getZoomString(coordinate);

        // TODO TG-15 configure
        String url = "http://old-lipn.univ-paris13.fr/~dubacq/europa/carte/0.6/" + subPath + ".png";


        String[] urls = new String[]{url};

        PImage image = getTileFromUrl(urls);
        if (image != null) {
            image.save(getImagePath(subPath));
        }

        return urls;
    }

    /**
     * Loads tile from URL(s) by using Processing's loadImage function. If multiple URLs are
     * provided, all tile images are blended into each other.
     * Duplicated from TileLoader since listeners cannot be added.
     *
     * @param urls The URLs (local or remote) to load the tiles from.
     * @return The tile image.
     */
    protected PImage getTileFromUrl(String[] urls) {
        // Load image from URL (local file included)
        // NB: Use 'unknown' as content-type to let loadImage decide
        PImage img = applet.loadImage(urls[0], "unknown");
        //PImage img = p.loadImage(urls[0]); // test for Android

        if (img != null) {
            // If array contains multiple URLs, load all images and blend them together
            for (int i = 1; i < urls.length; i++) {
                PImage img2 = applet.loadImage(urls[i], "unknown");
                if (img2 != null) {
                    img.blend(img2, 0, 0, img.width, img.height, 0, 0, img.width, img.height, PApplet.BLEND);
                }
            }
        }

        return img;
    }
}
