package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * Cell factory for stack in combo box with province display.
 *
 * @author MKL.
 */
public class StackInProvinceCellFactory implements Callback<ListView<Stack>, ListCell<Stack>> {

    /** {@inheritDoc} */
    @Override
    public ListCell<Stack> call(ListView<Stack> param) {
        return new ListCell<Stack>() {
            @Override
            protected void updateItem(Stack item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox();
                    setGraphic(hBox);
                    for (Counter counter : item.getCounters()) {
                        hBox.getChildren().add(UIUtil.getImage(counter));
                    }
                    hBox.getChildren().add(new Label(" - " + GlobalConfiguration.getMessage(item.getProvince())));
                }
            }
        };
    }
}
