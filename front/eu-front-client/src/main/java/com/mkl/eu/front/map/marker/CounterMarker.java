package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.board.Counter;
import processing.core.PImage;

/**
 * Marker of a counter.
 *
 * @author MKL
 */
public class CounterMarker {
    /** The counter it is created from. */
    private Counter counter;
    /** Image of the counter. */
    private PImage image;
    /** Stack owning the counter. */
    private StackMarker owner;

    /**
     * Constructor.
     * @param counter the counter.
     * @param image the image.
     */
    public CounterMarker(Counter counter, PImage image) {
        this.counter = counter;
        this.image = image;
    }

    /** @return the counter. */
    public Counter getCounter() {
        return counter;
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
