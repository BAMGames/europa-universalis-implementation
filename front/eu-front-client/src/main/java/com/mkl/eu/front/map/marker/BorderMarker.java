package com.mkl.eu.front.map.marker;

import com.mkl.eu.client.service.vo.enumeration.BorderEnum;

/**
 * Not a marker but Border was already used.
 *
 * @author MKL
 */
public class BorderMarker {
    /** Province bound to. */
    private IMapMarker province;
    /** Type of border. */
    private BorderEnum type;

    /**
     * Constructor.
     * @param province the province.
     * @param typeBorder the type of border.
     */
    public BorderMarker(IMapMarker province, String typeBorder) {
        this.province = province;
        this.type = BorderEnum.getByCode(typeBorder);
    }

    /** @return the province. */
    public IMapMarker getProvince() {
        return province;
    }

    /** @return the type. */
    public BorderEnum getType() {
        return type;
    }
}
