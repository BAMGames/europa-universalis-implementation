package com.mkl.eu.front.client.log;

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
        text.appendText(String.valueOf((char) b));
    }
}
