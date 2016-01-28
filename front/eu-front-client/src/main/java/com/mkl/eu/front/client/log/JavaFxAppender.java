package com.mkl.eu.front.client.log;

import ch.qos.logback.core.OutputStreamAppender;
import javafx.scene.control.TextArea;

/**
 * Log appender that will write the logs in a static TextArea.
 *
 * @author MKL.
 */
public class JavaFxAppender<E> extends OutputStreamAppender<E> {
    /** Static TextArea where the logs will be written. */
    private static TextArea text = new TextArea();

    /** {@inheritDoc} */
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
