package com.mkl.eu.front.client.eco;

import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Window containing the economics.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class EcoWindow extends AbstractDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EcoWindow.class);
    /** Economic service. */
    @Autowired
    private IEconomicService economicService;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Countries in the game. */
    private List<PlayableCountry> countries;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Stage of the window. */
    private Stage stage;
    /** Combobox for the countries for sheet B. */
    private ChoiceBox<PlayableCountry> choiceB;
    /** TableView for the sheets B. */
    private TableView<List<String>> tableB;

    /**
     * Constructor.
     *
     * @param countries  the countries to set.
     * @param gameConfig the gameConfig to set.
     */
    public EcoWindow(List<PlayableCountry> countries, GameConfiguration gameConfig) {
        this.countries = countries;
        this.gameConfig = gameConfig;
    }

    /**
     * Initialize the window.
     */
    @PostConstruct
    public void init() {
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);

        BorderPane border = new BorderPane();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createSheetA(null));
        tabPane.getTabs().add(createSheetB(null));

        border.setCenter(tabPane);

        Scene scene = new Scene(border, 800, 600);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> hide());
    }

    /**
     * Creates the tab for the royal treasure sheet.
     *
     * @param country default country sheet to display.
     * @return the tab for the royal treasure sheet.
     */
    private Tab createSheetA(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("eco.sheetA", null, globalConfiguration.getLocale()));

        return tab;
    }

    /**
     * Creates the tab for the income sheet.
     *
     * @param country default country sheet to display.
     * @return the tab for the income sheet.
     */
    private Tab createSheetB(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("eco.sheetB", null, globalConfiguration.getLocale()));

        choiceB = new ChoiceBox<>();
        choiceB.setItems(FXCollections.observableArrayList(countries));
        choiceB.converterProperty().set(new StringConverter<PlayableCountry>() {
            /** {@inheritDoc} */
            @Override
            public String toString(PlayableCountry object) {
                return object.getName();
            }

            /** {@inheritDoc} */
            @Override
            public PlayableCountry fromString(String string) {
                PlayableCountry country = null;

                for (PlayableCountry countryTest : countries) {
                    if (StringUtils.equals(string, countryTest.getName())) {
                        country = countryTest;
                        break;
                    }
                }

                return country;
            }
        });

        Button temp = new Button("ATEJ");
        temp.setOnAction(event -> {
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = economicService.computeEconomicalSheets(idGame);
                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when speaking in room.", e);
                // TODO exception handling
            }
        });
        Button update = new Button(message.getMessage("eco.update", null, globalConfiguration.getLocale()));

        HBox hBox = new HBox();
        hBox.getChildren().addAll(choiceB, temp, update);

        tableB = new TableView<>();
        tableB.setTableMenuButtonVisible(true);
        tableB.setPrefWidth(750);
        tableB.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, tableB);

        tab.setContent(vBox);

        choiceB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                populateTable(tableB, newValue);
            }
        });
        choiceB.getSelectionModel().select(country);

        return tab;
    }

    private void populateTable(TableView<List<String>> table, PlayableCountry country) {
        table.getColumns().clear();

        if (country == null) {
            return;
        }

        List<List<String>> datas = new ArrayList<>();
        datas.add(new ArrayList<>());
        datas.get(0).add(message.getMessage("eco.sheetB.#", null, globalConfiguration.getLocale()));
        datas.get(0).add(message.getMessage("eco.sheetB.turnNumber", null, globalConfiguration.getLocale()));
        datas.add(new ArrayList<>());
        datas.get(1).add("1");
        datas.get(1).add(message.getMessage("eco.sheetB.provinceIncome", null, globalConfiguration.getLocale()));
        datas.add(new ArrayList<>());
        datas.get(2).add("2");
        datas.get(2).add(message.getMessage("eco.sheetB.vassalIncome", null, globalConfiguration.getLocale()));
        datas.add(new ArrayList<>());
        datas.get(3).add("3");
        datas.get(3).add(message.getMessage("eco.sheetB.pillages", null, globalConfiguration.getLocale()));
        datas.add(new ArrayList<>());
        datas.get(4).add("4");
        datas.get(4).add(message.getMessage("eco.sheetB.eventLandIncome", null, globalConfiguration.getLocale()));
        datas.add(new ArrayList<>());
        datas.get(5).add("5");
        datas.get(5).add(message.getMessage("eco.sheetB.landIncome", null, globalConfiguration.getLocale()));

        for (EconomicalSheet sheet : country.getEconomicalSheets()) {
            datas.get(0).add(toString(sheet.getTurn()));
            datas.get(1).add(toString(sheet.getProvincesIncome()));
            datas.get(2).add(toString(sheet.getVassalIncome()));
            datas.get(3).add(toString(sheet.getPillages()));
            datas.get(4).add(toString(sheet.getEventLandIncome()));
            datas.get(5).add(toString(sheet.getLandIncome()));
        }

        TableColumn<List<String>, String> column = new TableColumn<>("");
        column.setPrefWidth(30);
        column.setSortable(false);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        table.getColumns().add(column);

        column = new TableColumn<>("");
        column.setPrefWidth(200);
        column.setSortable(false);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        table.getColumns().add(column);

        for (int i = 0; i < country.getEconomicalSheets().size(); i++) {
            column = new TableColumn<>("");
            column.setPrefWidth(50);
            column.setSortable(false);
            final int index = i + 2;
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(index)));
            table.getColumns().add(column);
        }

        table.setItems(FXCollections.observableArrayList(datas));
    }

    /**
     * Returns the String value of an integer.
     *
     * @param i to format in String.
     * @return the String value of an integer.
     */
    private String toString(Integer i) {
        return i == null ? "" : Integer.toString(i);
    }

    /**
     * Show this popup.
     */
    public void show() {
        this.stage.show();
    }

    /**
     * Hide this popup.
     */
    public void hide() {
        this.stage.hide();
    }

    /**
     * @return Whether or not this popup is showing.
     */
    public boolean isShowing() {
        return this.stage.isShowing();
    }

    /**
     * Update the Map given the diff.
     *
     * @param diff that will update the map.
     */
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case ECO_SHEET:
                updateEcoSheet(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process a eco sheet diff event.
     *
     * @param diff involving a room.
     */
    private void updateEcoSheet(Diff diff) {
        switch (diff.getType()) {
            case INVALIDATE:
                invalidateSheet(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process the invalide sheet diff event.
     *
     * @param diff involving a add room.
     */
    private void invalidateSheet(Diff diff) {
        Long idCountry = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null && !StringUtils.isEmpty(attribute.getValue())) {
            idCountry = Long.parseLong(attribute.getValue());
        }

        Long idSelectedCountry = null;
        if (choiceB.getSelectionModel().getSelectedItem() != null) {
            idSelectedCountry = choiceB.getSelectionModel().getSelectedItem().getId();
        }
        if (idCountry == null || idCountry.equals(idSelectedCountry)) {
            populateTable(tableB, choiceB.getSelectionModel().getSelectedItem());
        }
    }
}
