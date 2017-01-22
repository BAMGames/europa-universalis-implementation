package processing.javafx;

import javafx.application.Platform;
import javafx.stage.Stage;
import processing.core.PApplet;

/**
 * PSurfaceFX fails if there is already a JavaFX Application running.
 * But this one falls in deadlock.
 *
 * @author MKL.
 */
public class PSurfaceFXFixed extends PSurfaceFX {
    /**
     * Constructor.
     *
     * @param graphics the graphics.
     */
    public PSurfaceFXFixed(PGraphicsFX2D graphics) {
        super(graphics);
    }

    /** {@inheritDoc} */
    @Override
    public void initFrame(PApplet sketch) {/*, int backgroundColor,
                         int deviceIndex, boolean fullScreen,
                         boolean spanDisplays) {*/
        this.sketch = sketch;
        PApplicationFX.surface = this;
        //Frame frame = new DummyFrame();
        Platform.runLater(() -> new PApplicationFX().start(new Stage()));

        // FIXME deadlock because current thread is locking JavaFX thread
        // and is also waiting for the next to execute.

        // wait for stage to be initialized on its own thread before continuing
        while (stage == null) {
            try {
                //System.out.println("waiting for launch");
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }

        setProcessingIcon(stage);
    }
}
