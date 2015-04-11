package com.mkl.eu.front.map.handler.mouse;

import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.front.main.InfoView;
import com.mkl.eu.front.map.marker.CounterMarker;
import com.mkl.eu.front.map.marker.IMapMarker;
import com.mkl.eu.front.map.marker.StackMarker;
import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;

import java.util.Arrays;
import java.util.List;

/**
 * Mouse Handler to handle the two maps bindings.
 *
 * @author MKL
 */
public class InfowViewMouseHandler extends AbstractDragDropMenuMouseHandler<CounterMarker, StackMarker, CounterMarker, InfoView> {
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
        super(p, infoView, maps);
    }

    /** {@inheritDoc} */
    @Override
    protected void doAfterRelease(CounterMarker dragged, StackMarker drop) {
        if (drop == null) {
            drop = new StackMarker(new Stack(), (IMapMarker) getComponent().getSelected());
            ((IMapMarker) getComponent().getSelected()).addStack(drop);
        }
        StackMarker oldStack = dragged.getOwner();
        drop.addCounter(dragged);

        if (oldStack.getCounters().isEmpty()) {
            ((IMapMarker) getComponent().getSelected()).removeStack(oldStack);
        }
    }
}
