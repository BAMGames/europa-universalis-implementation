package com.mkl.eu.front.client.map.component;

import com.mkl.eu.front.client.event.IServiceCaller;
import com.mkl.eu.front.client.window.InteractiveMap;

/**
 * Interfaces for component wishing to call services without running java fx code.
 *
 * @author MKL.
 */
public interface INotJavaFxServiceCaller extends IServiceCaller {
    InteractiveMap getComponent();
}
