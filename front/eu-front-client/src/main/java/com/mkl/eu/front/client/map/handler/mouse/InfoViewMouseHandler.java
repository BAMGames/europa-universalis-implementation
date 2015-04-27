package com.mkl.eu.front.client.map.handler.mouse;

import com.mkl.eu.front.client.map.component.InfoView;
import com.mkl.eu.front.client.map.marker.CounterMarker;
import com.mkl.eu.front.client.map.marker.StackMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class InfoViewMouseHandler extends AbstractDragDropMenuMouseHandler<CounterMarker, StackMarker, Object, InfoView> {
    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     One or more maps.
     */
    public InfoViewMouseHandler(PApplet p, InfoView infoView, UnfoldingMap... maps) {
        this(p, infoView, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     A list of maps.
     */
    public InfoViewMouseHandler(PApplet p, InfoView infoView, List<UnfoldingMap> maps) {
        super(p, infoView, maps);
    }
}
