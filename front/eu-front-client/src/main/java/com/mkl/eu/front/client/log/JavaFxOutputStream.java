package com.mkl.eu.front.client.log;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream for a TextArea.
 *
 * @author MKL.
 */
public class JavaFxOutputStream extends OutputStream {
    /** TextArea where the bytes will be written. */
    private TextArea text = new TextArea();

    /**
     * Constructor.
     *
     * @param text the text to set.
     */
    public JavaFxOutputStream(TextArea text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override
    public void write(int b) throws IOException {
        // Need to invoke inside a Platform.runLater
        // to not do a NPE when the log is created
        // by a non-javaFX component.
        Platform.runLater(() -> text.appendText(String.valueOf((char) b)));
    }
}
