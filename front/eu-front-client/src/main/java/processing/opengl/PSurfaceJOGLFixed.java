package processing.opengl;

import processing.core.PGraphics;

/**
 * PSurfaceJOGL is not closing properly (there is still a process running).
 * Adding the possibility to remove reference of PSurface from screen and
 * display but it is not enough.
 *
 * @author MKL.
 */
public class PSurfaceJOGLFixed extends PSurfaceJOGL {

    /**
     * Constructor.
     *
     * @param graphics the graphics.
     */
    public PSurfaceJOGLFixed(PGraphics graphics) {
        super(graphics);
    }

    /**
     * Method called when sketch is closing.
     */
    public void destroy() {
        screen.removeReference();
        display.removeReference();
    }
}
