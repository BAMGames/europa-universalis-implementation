package com.mkl.eu.front.client.common;

import com.mkl.eu.client.service.service.common.RedeployRequest;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Class for the redeploy node.
 * It is a component that begins with a line with either an existing counter or a face and a province.
 * This line can be duplicated or deleted.
 */
public class RedeployLine {
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** The global node of a line. */
    private HBox node = new HBox();
    /** Choice box to select a counter. */
    private ChoiceBox<Counter> counter = new ChoiceBox<>();
    /** Choice box to select the face of a new counter. */
    private ChoiceBox<CounterFaceTypeEnum> face = new ChoiceBox<>();
    /** Choice box to select the province. */
    private ChoiceBox<String> province = new ChoiceBox<>();
    /** A button. It can be a Add button to add a new Line or a Remove button to remove current line. */
    private Button button;

    /**
     * Constructor.
     *
     * @param counterList the list of counters that can be selected.
     * @param faces       the list of faces that can be selected.
     * @param provinces   the list of provinces that can be selected.
     * @param listener    the listener to get the add and remove redeploy line events.
     */
    public RedeployLine(List<Counter> counterList, List<CounterFaceTypeEnum> faces, List<String> provinces, BiConsumer<RedeployLine, Boolean> listener) {
        counter.converterProperty().set(new CounterConverter());
        counter.setItems(FXCollections.observableList(counterList));
        face.setItems(FXCollections.observableList(faces));
        province.setItems(FXCollections.observableList(provinces));
        button = new Button(message.getMessage("add", null, globalConfiguration.getLocale()));

        counter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> face.setDisable(newValue != null));
        button.setOnAction(event -> {
                    RedeployLine newLine = new RedeployLine(counterList, faces, provinces, listener);
                    newLine.button.setText(message.getMessage("delete", null, globalConfiguration.getLocale()));
                    newLine.button.setOnAction(delEvent -> listener.accept(newLine, false));
                    listener.accept(newLine, true);
                }
        );

        node.getChildren().addAll(counter, face, province, button);
        listener.accept(this, true);
    }

    /** @return the node. */
    public HBox getNode() {
        return node;
    }

    /**
     * Transform a list of RedeployLine into a RedeployRequest.
     *
     * @param lines   the list of RedeployLine.
     * @param country the name of the country doing the redeploy.
     * @return a RedeployRequest.
     */
    public static RedeployRequest toRequest(List<RedeployLine> lines, String country) {
        List<RedeployRequest.ProvinceRedeploy> redeploys = new ArrayList<>();
        for (RedeployLine line : lines) {
            RedeployRequest.ProvinceRedeploy redeploy = redeploys.stream()
                    .filter(red -> StringUtils.equals(red.getProvince(), line.province.getValue()))
                    .findAny()
                    .orElseGet(() -> {
                        RedeployRequest.ProvinceRedeploy red = new RedeployRequest.ProvinceRedeploy(line.province.getValue());
                        redeploys.add(red);
                        return red;
                    });
            RedeployRequest.Unit unit = new RedeployRequest.Unit();
            if (line.counter.getSelectionModel().getSelectedItem() != null) {
                unit.setIdCounter(line.counter.getSelectionModel().getSelectedItem().getId());
            }
            if (line.face.getSelectionModel().getSelectedItem() != null) {
                unit.setFace(line.face.getSelectionModel().getSelectedItem());
                unit.setCountry(country);
            }
            redeploy.getUnits().add(unit);
        }
        return new RedeployRequest(redeploys);
    }
}