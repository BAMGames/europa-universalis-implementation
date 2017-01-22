package processing.opengl;

import processing.core.PSurface;

/**
 * Having the possibility to define a custom surface..
 *
 * @author MKL.
 */
public class PGraphicsOpenGLFixed extends PGraphicsOpenGL {
    /** {@inheritDoc} */
    @Override
    public PSurface createSurface() {
        return new PSurfaceJOGLFixed(this);
    }
}
