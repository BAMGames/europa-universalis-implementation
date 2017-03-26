package com.mkl.eu.front.client.map.marker;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
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
    /** Name of the country owning of the counter. */
    private String country;
    /** Type of the counter. */
    private CounterFaceTypeEnum type;

    /**
     * Constructor.
     *
     * @param id    the id.
     * @param image the image.
     */
    public CounterMarker(Long id, String country, CounterFaceTypeEnum type, PImage image) {
        this.id = id;
        this.country = country;
        this.type = type;
        this.image = image;
    }

    /** @return the id. */
    public Long getId() {
        return id;
    }

    /** @return the country. */
    public String getCountry() {
        return country;
    }

    /** @return the type. */
    public CounterFaceTypeEnum getType() {
        return type;
    }

    /** @param type the type to set. */
    public void setType(CounterFaceTypeEnum type) {
        this.type = type;
    }

    /** @param image the image to set. */
    public void setImage(PImage image) {
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
