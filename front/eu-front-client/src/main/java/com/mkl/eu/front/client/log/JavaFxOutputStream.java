package com.mkl.eu.front.client.log;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Description of the class.
 *
 * @author MKL.
 */
public class JavaFxOutputStream extends OutputStream {
    private TextArea text = new TextArea();

    public JavaFxOutputStream(TextArea text) {
        this.text = text;
    }

    @Override
    public void write(int b) throws IOException {
        text.appendText(String.valueOf((char) b));
    }
}
