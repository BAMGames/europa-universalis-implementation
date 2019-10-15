package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.front.client.main.UIUtil;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Cell factory for counter in combo box.
 *
 * @author MKL.
 */
public class CounterFaceCellFactory implements Callback<ListView<CounterFaceTypeEnum>, ListCell<CounterFaceTypeEnum>> {
    /** Country of the counter. */
    private String country;

    /**
     * Constructor.
     *
     * @param country of the counter.
     */
    public CounterFaceCellFactory(String country) {
        this.country = country;
    }

    /** {@inheritDoc} */
    @Override
    public ListCell<CounterFaceTypeEnum> call(ListView<CounterFaceTypeEnum> param) {
        return new ListCell<CounterFaceTypeEnum>() {
            @Override
            protected void updateItem(CounterFaceTypeEnum item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(UIUtil.getImage(country, item));
                }
            }
        };
    }
}
