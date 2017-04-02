package com.mkl.eu.front.client.eco;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.eco.TradeFleet;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.ref.IReferentielConstants;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    /** Trade fleets in the game. */
    private List<TradeFleet> tradeFleets;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Stage of the window. */
    private Stage stage;
    /** Flag saying that a trading fleet has changed and that trade fleet tab should be updated. */
    private boolean tradeFleetModified;
    /** ChoiceBox for the countries for sheet B. */
    private ChoiceBox<PlayableCountry> choiceB;
    /** TableView for the sheets B. */
    private TableView<List<String>> tableB;
    /** TableView for trade fleets in mediterranean trade center. */
    private TableView<TradeFleetSheet> tableTFMed;
    /** TableView for trade fleets in atlantic trade center. */
    private TableView<TradeFleetSheet> tableTFAtl;
    /** TableView for trade fleets in indian trade center. */
    private TableView<TradeFleetSheet> tableTFInd;

    static {
        config = new ArrayList<>();
        int index = -1;
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
        config.add(new TableConfig<>("26", "eco.sheetB.interestExpense", ++index, sheet -> toString(sheet.getInterestExpense())));
        config.add(new TableConfig<>("27", "eco.sheetB.mandatoryRefundExpense", ++index, sheet -> toString(sheet.getMandRefundExpense())));
        config.add(new TableConfig<>("28", "eco.sheetB.rtCollapse", ++index, sheet -> toString(sheet.getRtCollapse())));
        config.add(new TableConfig<>("29", "eco.sheetB.optionalRefundExpense", ++index, sheet -> toString(sheet.getOptRefundExpense())));
        config.add(new TableConfig<>("30", "eco.sheetB.unitMaintenance", ++index, sheet -> toString(sheet.getUnitMaintExpense())));
        config.add(new TableConfig<>("31", "eco.sheetB.fortMaintenance", ++index, sheet -> toString(sheet.getFortMaintExpense())));
        config.add(new TableConfig<>("32", "eco.sheetB.missionMaintenance", ++index, sheet -> toString(sheet.getMissMaintExpense())));
        config.add(new TableConfig<>("33", "eco.sheetB.unitPurchase", ++index, sheet -> toString(sheet.getUnitPurchExpense())));
        config.add(new TableConfig<>("34", "eco.sheetB.fortPurchase", ++index, sheet -> toString(sheet.getFortPurchExpense())));
        config.add(new TableConfig<>("35", "eco.sheetB.adminActions", ++index, sheet -> toString(sheet.getAdminActExpense())));
        config.add(new TableConfig<>("36", "eco.sheetB.adminReactions", ++index, sheet -> toString(sheet.getAdminReactExpense())));
        config.add(new TableConfig<>("37", "eco.sheetB.otherExpenses", ++index, sheet -> toString(sheet.getOtherExpense())));
        config.add(new TableConfig<>("38", "eco.sheetB.adminTotalExpense", ++index, sheet -> toString(sheet.getAdmTotalExpense())));
        config.add(new TableConfig<>("39", "eco.sheetB.excTaxesModifier", ++index, sheet -> toString(sheet.getExcTaxesMod())));
        config.add(new TableConfig<>("40", "eco.sheetB.passiveCampaigns", ++index, sheet -> toString(sheet.getPassCampExpense())));
        config.add(new TableConfig<>("41", "eco.sheetB.activeCampaigns", ++index, sheet -> toString(sheet.getActCampExpense())));
        config.add(new TableConfig<>("42", "eco.sheetB.majorCampaigns", ++index, sheet -> toString(sheet.getMajCampExpense())));
        config.add(new TableConfig<>("43", "eco.sheetB.multipleCampaigns", ++index, sheet -> toString(sheet.getMultCampExpense())));
        config.add(new TableConfig<>("44", "eco.sheetB.excRecruits", ++index, sheet -> toString(sheet.getExcRecruitExpense())));
        config.add(new TableConfig<>("45", "eco.sheetB.navalRefit", ++index, sheet -> toString(sheet.getNavalRefitExpense())));
        config.add(new TableConfig<>("46", "eco.sheetB.praesidiosBuild", ++index, sheet -> toString(sheet.getPraesidioExpense())));
        config.add(new TableConfig<>("47", "eco.sheetB.militaryTotal", ++index, sheet -> toString(sheet.getMilitaryExpense())));
        config.add(new TableConfig<>("48", "eco.sheetB.total", ++index, sheet -> toString(sheet.getExpenses())));
    }

    /**
     * Constructor.
     *
     * @param countries   the countries to set.
     * @param tradeFleets the tradeFleets to set.
     * @param gameConfig  the gameConfig to set.
     */
    public EcoWindow(List<PlayableCountry> countries, List<TradeFleet> tradeFleets, GameConfiguration gameConfig) {
        this.countries = countries;
        this.tradeFleets = tradeFleets;
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
        tabPane.getTabs().add(createTradeFleets());

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
        tableB.setPrefHeight(520);

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

        int currentTurn = country.getEconomicalSheets().stream()
                .map(EconomicalSheet::getTurn)
                .max(Comparator.naturalOrder())
                .orElse(0);
        for (EconomicalSheet sheet : country.getEconomicalSheets()) {
            if (sheet.getTurn() <= currentTurn - 10) {
                continue;
            }
            for (TableConfig<EconomicalSheet> configItem : config) {
                datas.get(configItem.getIndex()).add(configItem.getFunction().apply(sheet));
            }
        }
        for (int i = country.getEconomicalSheets().size(); i < 10; i++) {
            for (TableConfig<EconomicalSheet> configItem : config) {
                datas.get(configItem.getIndex()).add(null);
            }
        }

        TableColumn<List<String>, String> column = new TableColumn<>(message.getMessage("eco.sheetB.#", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        column.setSortable(false);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.sheetB.turnNumber", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.33));
        column.setSortable(false);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        table.getColumns().add(column);

        int begin = Math.max(currentTurn - 10, 0);
        for (int i = begin; i < country.getEconomicalSheets().size(); i++) {
            column = new TableColumn<>(country.getEconomicalSheets().get(i).getTurn().toString());
            column.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
            column.setSortable(false);
            final int index = i + 2 - begin;
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(index)));
            table.getColumns().add(column);
        }
        for (int i = country.getEconomicalSheets().size(); i < 10; i++) {
            column = new TableColumn<>(null);
            column.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
            column.setSortable(false);
            table.getColumns().add(column);
        }

        table.setItems(FXCollections.observableArrayList(datas));
    }

    /**
     * Creates the tab for the trade fleets.
     *
     * @return the tab for the trade fleets.
     */
    private Tab createTradeFleets() {
        Tab tab = new Tab(message.getMessage("eco.tfs", null, globalConfiguration.getLocale()));



        Text titleMed = new Text(message.getMessage("eco.tfs.mediterranean", null, globalConfiguration.getLocale()));
        tableTFMed = new TableView<>();
        configureTableTradeFleet(tableTFMed);

        Text titleAtl = new Text(message.getMessage("eco.tfs.atlantic", null, globalConfiguration.getLocale()));
        tableTFAtl = new TableView<>();
        configureTableTradeFleet(tableTFAtl);

        Text titleInd = new Text(message.getMessage("eco.tfs.indian", null, globalConfiguration.getLocale()));
        tableTFInd = new TableView<>();
        configureTableTradeFleet(tableTFInd);

        updateTradeFleetsTab();

        VBox vBox = new VBox();
        vBox.getChildren().addAll(titleMed, tableTFMed, titleAtl, tableTFAtl, titleInd, tableTFInd);

        tab.setContent(vBox);

        return tab;
    }

    /**
     * Update content of the trade fleets tab.
     */
    private void updateTradeFleetsTab() {
        List<String> tradeZones = tradeFleets.stream()
                .map(TradeFleet::getProvince)
                .distinct()
                .collect(Collectors.toList());

        List<TradeFleetSheet> sheets = new ArrayList<>();
        for (String tradeZone : tradeZones) {
            TradeFleetSheet sheet = new TradeFleetSheet();

            sheet.setTradeZone(tradeZone);
            sheet.setLevelEngland(getLevel(tradeZone, PlayableCountry.ENGLAND));
            sheet.setLevelHolland(getLevel(tradeZone, PlayableCountry.HOLLAND));
            sheet.setLevelFrance(getLevel(tradeZone, PlayableCountry.FRANCE));
            sheet.setLevelVenice(getLevel(tradeZone, PlayableCountry.VENICE));
            sheet.setLevelTurkey(getLevel(tradeZone, PlayableCountry.TURKEY));
            sheet.setLevelSpain(getLevel(tradeZone, PlayableCountry.SPAIN));
            sheet.setLevelPortugal(getLevel(tradeZone, PlayableCountry.PORTUGAL));
            sheet.setLevelSweden(getLevel(tradeZone, PlayableCountry.SWEDEN));
            sheet.setLevelPoland(getLevel(tradeZone, PlayableCountry.POLAND));
            sheet.setLevelPrusse(getLevel(tradeZone, PlayableCountry.PRUSSE));
            sheet.setLevelHabsbourg(getLevel(tradeZone, PlayableCountry.HABSBOURG));
            sheet.setLevelRussia(getLevel(tradeZone, PlayableCountry.RUSSIA));
            sheet.setOtherLevels(tradeFleets.stream()
                    .filter(tf -> StringUtils.equals(tradeZone, tf.getProvince())
                            && Arrays.binarySearch(PlayableCountry.TRADE_FLEET_MAJORS, tf.getCountry()) < 0)
                    .collect(Collectors.toMap(TradeFleet::getCountry, TradeFleet::getLevel)));

            sheets.add(sheet);
        }
        tableTFMed.setItems(FXCollections.observableList(sheets.stream()
                .filter(tf -> Arrays.binarySearch(IReferentielConstants.TRADE_ZONES_MEDITERRANEAN, tf.getTradeZone()) >= 0)
                .sorted(Comparator.comparing(TradeFleetSheet::getTradeZone))
                .collect(Collectors.toList())));
        tableTFAtl.setItems(FXCollections.observableList(sheets.stream()
                .filter(tf -> Arrays.binarySearch(IReferentielConstants.TRADE_ZONES_ATLANTIC, tf.getTradeZone()) >= 0)
                .sorted(Comparator.comparing(TradeFleetSheet::getTradeZone))
                .collect(Collectors.toList())));
        tableTFInd.setItems(FXCollections.observableList(sheets.stream()
                .filter(tf -> Arrays.binarySearch(IReferentielConstants.TRADE_ZONES_INDIEN, tf.getTradeZone()) >= 0)
                .sorted(Comparator.comparing(TradeFleetSheet::getTradeZone))
                .collect(Collectors.toList())));
    }

    /**
     * @param province trade zone where is the trade fleet.
     * @param country  owner of the trade fleet.
     * @return the trade fleet level.
     */
    private Integer getLevel(String province, String country) {
        return tradeFleets.stream()
                .filter(tf -> StringUtils.equals(province, tf.getProvince()) &&
                        StringUtils.equals(country, tf.getCountry()))
                .map(TradeFleet::getLevel)
                .findFirst()
                .orElse(null);
    }

    /**
     * Configure the trade fleets table.
     *
     * @param table the trade fleets table.
     */
    private void configureTableTradeFleet(TableView<TradeFleetSheet> table) {
        table.setTableMenuButtonVisible(true);
        table.setPrefWidth(750);
        TableColumn<TradeFleetSheet, String> column;

        column = new TableColumn<>(message.getMessage("eco.tfs.country", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.18));
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(message.getMessage(param.getValue().getTradeZone(), null, globalConfiguration.getLocale())));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.england", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelEngland"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.holland", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelHolland"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.france", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelFrance"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.venice", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelVenice"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.turkey", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelTurkey"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.spain", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelSpain"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.portugal", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelPortugal"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.sweden", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelSweden"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.poland", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelPoland"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.prusse", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelPrusse"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.habsbourg", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelHabsbourg"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.russia", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.05));
        column.setCellValueFactory(new PropertyValueFactory<>("levelRussia"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("eco.tfs.others", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        column.setCellValueFactory(param -> {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : param.getValue().getOtherLevels().entrySet()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(message.getMessage(entry.getKey(), null, globalConfiguration.getLocale()))
                        .append(" ")
                        .append(entry.getValue());
            }
            return new ReadOnlyStringWrapper(sb.toString());
        });
        table.getColumns().add(column);
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
            case COUNTER:
                updateCounter(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Method called when all diffs of a DiffEvent have been computed.
     */
    public void updateComplete() {
        if (tradeFleetModified) {
            updateTradeFleetsTab();
        }

        tradeFleetModified = false;
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
     * Process a counter diff event.
     *
     * @param diff involving a counter.
     */
    private void updateCounter(Diff diff) {
        switch (diff.getType()) {
            case MODIFY:
            case ADD:
                modifyCounter(diff);
                break;
            case REMOVE:
                removeCounter(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process the modify counter diff event.
     *
     * @param diff involving a modify counter.
     */
    private void modifyCounter(Diff diff) {
        if (tradeFleetModified) {
            return;
        }
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
        if (attribute != null) {
            CounterFaceTypeEnum type = CounterFaceTypeEnum.valueOf(attribute.getValue());

            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.LEVEL);
            if (attribute != null && CounterUtil.isTradingFleet(type)) {
                tradeFleetModified = true;
            }
        }
    }

    /**
     * Process the remove counter diff event.
     *
     * @param diff involving a remove counter.
     */
    private void removeCounter(Diff diff) {
        if (tradeFleetModified) {
            return;
        }
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute != null) {
            String province = attribute.getValue();
            tradeFleetModified = tradeFleets.stream()
                    .filter(tf -> StringUtils.equals(tf.getProvince(), province))
                    .findAny()
                    .isPresent();
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

    /**
     * Object used in the table view to display all the trade fleets in a single trade zone.
     */
    public class TradeFleetSheet {
        /** Trade zone. */
        private String tradeZone;
        /** Trade fleet level of the country England. */
        private Integer levelEngland;
        /** Trade fleet level of the country Holland. */
        private Integer levelHolland;
        /** Trade fleet level of the country France. */
        private Integer levelFrance;
        /** Trade fleet level of the country Venice. */
        private Integer levelVenice;
        /** Trade fleet level of the country Turkey. */
        private Integer levelTurkey;
        /** Trade fleet level of the country Spain. */
        private Integer levelSpain;
        /** Trade fleet level of the country Portugal. */
        private Integer levelPortugal;
        /** Trade fleet level of the country Sweden. */
        private Integer levelSweden;
        /** Trade fleet level of the country Poland. */
        private Integer levelPoland;
        /** Trade fleet level of the country Prusse. */
        private Integer levelPrusse;
        /** Trade fleet level of the country Habsbourg. */
        private Integer levelHabsbourg;
        /** Trade fleet level of the country Russia. */
        private Integer levelRussia;
        /** Trade fleet levels of other minor countries. */
        private Map<String, Integer> otherLevels = new HashMap<>();

        /** @return the tradeZone. */
        public String getTradeZone() {
            return tradeZone;
        }

        /** @param tradeZone the tradeZone to set. */
        public void setTradeZone(String tradeZone) {
            this.tradeZone = tradeZone;
        }

        /** @return the levelEngland. */
        public Integer getLevelEngland() {
            return levelEngland;
        }

        /** @param levelEngland the levelEngland to set. */
        public void setLevelEngland(Integer levelEngland) {
            this.levelEngland = levelEngland;
        }

        /** @return the levelHolland. */
        public Integer getLevelHolland() {
            return levelHolland;
        }

        /** @param levelHolland the levelHolland to set. */
        public void setLevelHolland(Integer levelHolland) {
            this.levelHolland = levelHolland;
        }

        /** @return the levelFrance. */
        public Integer getLevelFrance() {
            return levelFrance;
        }

        /** @param levelFrance the levelFrance to set. */
        public void setLevelFrance(Integer levelFrance) {
            this.levelFrance = levelFrance;
        }

        /** @return the levelVenice. */
        public Integer getLevelVenice() {
            return levelVenice;
        }

        /** @param levelVenice the levelVenice to set. */
        public void setLevelVenice(Integer levelVenice) {
            this.levelVenice = levelVenice;
        }

        /** @return the levelTurkey. */
        public Integer getLevelTurkey() {
            return levelTurkey;
        }

        /** @param levelTurkey the levelTurkey to set. */
        public void setLevelTurkey(Integer levelTurkey) {
            this.levelTurkey = levelTurkey;
        }

        /** @return the levelSpain. */
        public Integer getLevelSpain() {
            return levelSpain;
        }

        /** @param levelSpain the levelSpain to set. */
        public void setLevelSpain(Integer levelSpain) {
            this.levelSpain = levelSpain;
        }

        /** @return the levelPortugal. */
        public Integer getLevelPortugal() {
            return levelPortugal;
        }

        /** @param levelPortugal the levelPortugal to set. */
        public void setLevelPortugal(Integer levelPortugal) {
            this.levelPortugal = levelPortugal;
        }

        /** @return the levelSweden. */
        public Integer getLevelSweden() {
            return levelSweden;
        }

        /** @param levelSweden the levelSweden to set. */
        public void setLevelSweden(Integer levelSweden) {
            this.levelSweden = levelSweden;
        }

        /** @return the levelPoland. */
        public Integer getLevelPoland() {
            return levelPoland;
        }

        /** @param levelPoland the levelPoland to set. */
        public void setLevelPoland(Integer levelPoland) {
            this.levelPoland = levelPoland;
        }

        /** @return the levelPrusse. */
        public Integer getLevelPrusse() {
            return levelPrusse;
        }

        /** @param levelPrusse the levelPrusse to set. */
        public void setLevelPrusse(Integer levelPrusse) {
            this.levelPrusse = levelPrusse;
        }

        /** @return the levelHabsbourg. */
        public Integer getLevelHabsbourg() {
            return levelHabsbourg;
        }

        /** @param levelHabsbourg the levelHabsbourg to set. */
        public void setLevelHabsbourg(Integer levelHabsbourg) {
            this.levelHabsbourg = levelHabsbourg;
        }

        /** @return the levelRussia. */
        public Integer getLevelRussia() {
            return levelRussia;
        }

        /** @param levelRussia the levelRussia to set. */
        public void setLevelRussia(Integer levelRussia) {
            this.levelRussia = levelRussia;
        }

        /** @return the otherLevels. */
        public Map<String, Integer> getOtherLevels() {
            return otherLevels;
        }

        /** @param otherLevels the otherLevels to set. */
        public void setOtherLevels(Map<String, Integer> otherLevels) {
            this.otherLevels = otherLevels;
        }

        /**
         * Adds a trade fleet level of a minor country.
         *
         * @param country minor country.
         * @param level   of the trade fleet.
         * @return this.
         */
        public void addOtherLevel(String country, Integer level) {
            otherLevels.put(country, level);
        }
    }
}
