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
    /** Code of the leader counter. */
    private String code;

    /**
     * Constructor.
     *
     * @param id      the id.
     * @param country the country.
     * @param type    the type.
     * @param code    the code.
     */
    public CounterMarker(Long id, String country, CounterFaceTypeEnum type, String code) {
        this.id = id;
        this.country = country;
        this.type = type;
        this.code = code;
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

    /** @return the code. */
    public String getCode() {
        return code;
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
