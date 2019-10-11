package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Cell factory for counter in combo box.
 *
 * @author MKL.
 */
public class CounterCellFactory implements Callback<ListView<Counter>, ListCell<Counter>> {
    /** {@inheritDoc} */
    @Override
    public ListCell<Counter> call(ListView<Counter> param) {
        return new ListCell<Counter>() {
            @Override
            protected void updateItem(Counter item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    try {
                        FileInputStream fis = new FileInputStream(MarkerUtils.getImagePath(item));
                        ImageView image = new ImageView(new Image(fis, 40, 40, true, false));
                        setGraphic(image);
                    } catch (FileNotFoundException e) {
                        // TODO what to display if no image ?
                    }
                }
            }
        };
    }
}
