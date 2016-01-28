package com.mkl.eu.front.client.log;

import ch.qos.logback.core.OutputStreamAppender;
import javafx.scene.control.TextArea;

/**
 * Description of the class.
 *
 * @author MKL.
 */
public class JavaFxAppender<E> extends OutputStreamAppender<E> {
    private static TextArea text = new TextArea();

    @Override
    public void start() {
        setOutputStream(new JavaFxOutputStream(text));
        super.start();
    }

    /** @return the text. */
    public static TextArea getText() {
        return text;
    }
}
