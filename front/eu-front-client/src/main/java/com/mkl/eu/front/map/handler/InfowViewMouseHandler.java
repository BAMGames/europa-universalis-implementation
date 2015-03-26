package com.mkl.eu.front.map.handler;

import com.mkl.eu.front.main.InfoView;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.MapEventBroadcaster;
import processing.core.PApplet;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class InfowViewMouseHandler extends MapEventBroadcaster {
    /** Small map showing the overview, i.e. the world. */
    private InfoView infoView;

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     One or more maps.
     */
    public InfowViewMouseHandler(PApplet p, InfoView infoView, UnfoldingMap... maps) {
        this(p, infoView, Arrays.asList(maps));
    }

    /**
     * Creates a MouseHandler for the given maps.
     *
     * @param p        The PApplet.
     * @param infoView informative panel.
     * @param maps     A list of maps.
     */
    public InfowViewMouseHandler(PApplet p, InfoView infoView, List<UnfoldingMap> maps) {
        super(maps);

        this.infoView = infoView;

        p.registerMethod("mouseEvent", this);
    }

    /**
     * Mouse event of type CLICK.
     *
     * @param event mouse event.
     */
    public void mouseClicked(MouseEvent event) {
        if (infoView.isHit(event.getX(), event.getY())) {
            infoView.trigger(event.getX(), event.getY());
        }
    }

    // --------------------------------------------------------------
    // Shamelessly copied code from Processing PApplet. No other way to hook into
    // register Processing mouse event and still have the same functionality with pmouseX, etc.
    // --------------------------------------------------------------

    /**
     * Method called by the PApplet.
     *
     * @param event mouse event.
     */
    public void mouseEvent(MouseEvent event) {
        int action = event.getAction();

        switch (action) {
            case MouseEvent.CLICK:
                mouseClicked(event);
                break;
            default:
                break;
        }
    }
}
