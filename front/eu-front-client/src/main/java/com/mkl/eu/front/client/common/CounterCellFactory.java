package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.front.client.main.UIUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

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
                    setGraphic(UIUtil.getImage(item));
                }
            }
        };
    }
}
