package com.mkl.eu.front.map.marker;

import processing.core.PImage;

/**
 * Marker of a counter.
 *
 * @author MKL
 */
public class CounterMarker {
    /** Image of the counter. */
    private PImage image;
    /** Stack owning the counter. */
    private StackMarker owner;

    /**
     * Constructor.
     * @param image the image.
     */
    public CounterMarker(PImage image) {
        this.image = image;
    }

    /** @return the image. */
    public PImage getImage() {
        return image;
    }

    /** @return the owner. */
    public StackMarker getOwner() {
        return owner;
    }

    /** @param owner the owner to set. */
    public void setOwner(StackMarker owner) {
        this.owner = owner;
    }
}
