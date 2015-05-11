package com.mkl.eu.front.client.map.marker;

import processing.core.PImage;

/**
 * Marker of a counter.
 *
 * @author MKL
 */
public class CounterMarker {
    /** Id of the counter. */
    private Long id;
    /** Image of the counter. */
    private PImage image;
    /** Stack owning the counter. */
    private StackMarker owner;

    /**
     * Constructor.
     *
     * @param id    the id.
     * @param image the image.
     */
    public CounterMarker(Long id, PImage image) {
        this.id = id;
        this.image = image;
    }

    /** @return the id. */
    public Long getId() {
        return id;
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
