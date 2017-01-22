package processing.javafx;

import processing.core.PSurface;

/**
 * Having the possibility to define a custom surface.
 *
 * @author MKL.
 */
public class PGraphicsFX2DFixed extends PGraphicsFX2D {
    /** {@inheritDoc} */
    @Override
    public PSurface createSurface() {
        return new PSurfaceFXFixed(this);
    }
}
