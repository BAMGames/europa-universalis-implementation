package com.mkl.eu.front.client.eco;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
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
import java.util.function.Function;

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
    /** Table config for sheet B. */
    private static final List<TableConfig<EconomicalSheet>> config;
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

    static {
        config = new ArrayList<>();
        int index = -1;
        config.add(new TableConfig<>("eco.sheetB.#", "eco.sheetB.turnNumber", ++index, sheet -> toString(sheet.getTurn())));
        config.add(new TableConfig<>("1", "eco.sheetB.provinceIncome", ++index, sheet -> toString(sheet.getProvincesIncome())));
        config.add(new TableConfig<>("2", "eco.sheetB.vassalIncome", ++index, sheet -> toString(sheet.getVassalIncome())));
        config.add(new TableConfig<>("3", "eco.sheetB.pillages", ++index, sheet -> toString(sheet.getPillages())));
        config.add(new TableConfig<>("4", "eco.sheetB.eventLandIncome", ++index, sheet -> toString(sheet.getEventLandIncome())));
        config.add(new TableConfig<>("5", "eco.sheetB.landIncome", ++index, sheet -> toString(sheet.getLandIncome())));
        config.add(new TableConfig<>("6", "eco.sheetB.mnuIncome", ++index, sheet -> toString(sheet.getMnuIncome())));
        config.add(new TableConfig<>("7", "eco.sheetB.goldIncome", ++index, sheet -> toString(sheet.getGoldIncome())));
        config.add(new TableConfig<>("8", "eco.sheetB.industrialIncome", ++index, sheet -> toString(sheet.getIndustrialIncome())));
        config.add(new TableConfig<>("9", "eco.sheetB.domTradeIncome", ++index, sheet -> toString(sheet.getDomTradeIncome())));
        config.add(new TableConfig<>("10", "eco.sheetB.forTradeIncome", ++index, sheet -> toString(sheet.getForTradeIncome())));
        config.add(new TableConfig<>("11", "eco.sheetB.fleetLevelIncome", ++index, sheet -> toString(sheet.getFleetLevelIncome())));
        config.add(new TableConfig<>("12", "eco.sheetB.fleetMonopIncome", ++index, sheet -> toString(sheet.getFleetMonopIncome())));
        config.add(new TableConfig<>("13", "eco.sheetB.monopolies", ++index, sheet -> ""));
        config.add(new TableConfig<>("14", "eco.sheetB.tradeCenterIncome", ++index, sheet -> toString(sheet.getTradeCenterIncome())));
        config.add(new TableConfig<>("15", "eco.sheetB.tradeCenterLoss", ++index, sheet -> toString(sheet.getTradeCenterLoss())));
        config.add(new TableConfig<>("16", "eco.sheetB.tradeIncome", ++index, sheet -> toString(sheet.getTradeIncome())));
        config.add(new TableConfig<>("17", "eco.sheetB.colIncome", ++index, sheet -> toString(sheet.getColIncome())));
        config.add(new TableConfig<>("18", "eco.sheetB.tpIncome", ++index, sheet -> toString(sheet.getTpIncome())));
        config.add(new TableConfig<>("19", "eco.sheetB.exoResIncome", ++index, sheet -> toString(sheet.getExoResIncome())));
        config.add(new TableConfig<>("20", "eco.sheetB.monopolies", ++index, sheet -> ""));
        config.add(new TableConfig<>("21", "eco.sheetB.rotwIncome", ++index, sheet -> toString(sheet.getRotwIncome())));
        config.add(new TableConfig<>("22", "eco.sheetB.specialIncome", ++index, sheet -> toString(sheet.getSpecialIncome())));
        config.add(new TableConfig<>("23", "eco.sheetB.income", ++index, sheet -> toString(sheet.getIncome())));
        config.add(new TableConfig<>("24", "eco.sheetB.eventIncome", ++index, sheet -> toString(sheet.getEventIncome())));
        config.add(new TableConfig<>("25", "eco.sheetB.grossIncome", ++index, sheet -> toString(sheet.getGrossIncome())));
    }

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
        stage.setTitle(message.getMessage("eco.title", null, globalConfiguration.getLocale()));
        stage.initModality(Modality.WINDOW_MODAL);

        BorderPane border = new BorderPane();

        TabPane tabPane = new TabPane();
        PlayableCountry country = CommonUtil.findFirst(countries, playableCountry -> playableCountry.getId().equals(gameConfig.getIdCountry()));
        tabPane.getTabs().add(createSheetA(country));
        tabPane.getTabs().add(createSheetB(country));

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
                LOGGER.error("Error when updating economical sheets.", e);

                processExceptionEvent(new ExceptionEvent(e));
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
                populateTable(tableB, newValue, config);
            }
        });
        choiceB.getSelectionModel().select(country);

        return tab;
    }

    private void populateTable(TableView<List<String>> table, PlayableCountry country, List<TableConfig<EconomicalSheet>> config) {
        table.getColumns().clear();

        if (country == null) {
            return;
        }

        List<List<String>> datas = new ArrayList<>();
        for (TableConfig<EconomicalSheet> configItem : config) {
            datas.add(new ArrayList<>());
            datas.get(configItem.getIndex()).add(message.getMessage(configItem.getMessageColumn1(), null, globalConfiguration.getLocale()));
            datas.get(configItem.getIndex()).add(message.getMessage(configItem.getMessageColumn2(), null, globalConfiguration.getLocale()));
        }

        for (EconomicalSheet sheet : country.getEconomicalSheets()) {
            for (TableConfig<EconomicalSheet> configItem : config) {
                datas.get(configItem.getIndex()).add(configItem.getFunction().apply(sheet));
            }
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
            column = new TableColumn<>(country.getEconomicalSheets().get(i).getTurn().toString());
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
    private static String toString(Integer i) {
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
     * Requests that this {@code Window} get the input focus.
     */
    public void requestFocus() {
        this.stage.requestFocus();
    }

    /**
     * Update the window given the diff.
     *
     * @param diff that will update the window.
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
     * Process an economical sheet diff event.
     *
     * @param diff involving an economical sheet.
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
     * Process the invalidate sheet diff event.
     *
     * @param diff involving an invalidate sheet.
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
            populateTable(tableB, choiceB.getSelectionModel().getSelectedItem(), config);
        }
    }

    /**
     * Config used to display the economical sheet in a table view.
     *
     * @param <V> Should be EconomicalSheet but generic in case of.
     */
    private static class TableConfig<V> {
        /** Message code for the first colum. */
        private String messageColumn1;
        /** Message code for the second colum. */
        private String messageColumn2;
        /** Index of this config. */
        private int index;
        /** Function to apply to the economical sheet to retrieve the info. */
        private Function<V, String> function;

        /**
         * Constructor.
         *
         * @param messageColumn1 the messageColumn1 to set.
         * @param messageColumn2 the messageColumn2 to set.
         * @param index          the index to set.
         * @param function       the function to set.
         */
        public TableConfig(String messageColumn1, String messageColumn2, int index, Function<V, String> function) {
            this.messageColumn1 = messageColumn1;
            this.messageColumn2 = messageColumn2;
            this.index = index;
            this.function = function;
        }

        /** @return the function. */
        public Function<V, String> getFunction() {
            return function;
        }

        /** @return the messageColumn1. */
        public String getMessageColumn1() {
            return messageColumn1;
        }

        /** @return the messageColumn2. */
        public String getMessageColumn2() {
            return messageColumn2;
        }

        /** @return the index. */
        public int getIndex() {
            return index;
        }
    }
}
