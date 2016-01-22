package com.mkl.eu.front.client.eco;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.eco.AddAdminActionRequest;
import com.mkl.eu.client.service.service.eco.RemoveAdminActionRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.BasicForce;
import com.mkl.eu.client.service.vo.tables.Unit;
import com.mkl.eu.client.service.vo.util.MaintenanceUtil;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.add;
import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Window containing the administrative actions.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class AdminActionsWindow extends AbstractDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminActionsWindow.class);
    /** Counter Face Type for armies. */
    private static final List<CounterFaceTypeEnum> ARMY_TYPES = new ArrayList<>();
    /** Counter Face Type for land armies. */
    private static final List<CounterFaceTypeEnum> ARMY_LAND_TYPES = new ArrayList<>();
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
    /** Game. */
    private Game game;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Stage of the window. */
    private Stage stage;

    /********************************************/
    /**        Nodes about maintenance          */
    /********************************************/
    /** The TitledPane containing all the other nodes. */
    private TitledPane unitMaintenancePane;
    /** The TableView containing the already planned actions. */
    private TableView<AdministrativeAction> unitMaintenanceTable;
    /** The ChoiceBox containing the remaining counters. */
    private ChoiceBox<Counter> unitMaintenanceCountersChoice;

    /**
     * Filling the static List.
     */
    static {
        ARMY_TYPES.add(CounterFaceTypeEnum.ARMY_PLUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.ARMY_MINUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK);
        ARMY_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK);
        ARMY_TYPES.add(CounterFaceTypeEnum.FLEET_PLUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.FLEET_MINUS);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_DETACHMENT_EXPLORATION);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_GALLEY);
        ARMY_TYPES.add(CounterFaceTypeEnum.NAVAL_TRANSPORT);

        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_PLUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.ARMY_MINUS);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_KOZAK);
        ARMY_LAND_TYPES.add(CounterFaceTypeEnum.LAND_DETACHMENT_EXPLORATION_KOZAK);
    }

    /**
     * Constructor.
     *
     * @param game       the game to set.
     * @param gameConfig the gameConfig to set.
     */
    public AdminActionsWindow(Game game, GameConfiguration gameConfig) {
        this.game = game;
        this.gameConfig = gameConfig;
    }

    /**
     * Initialize the window.
     */
    @PostConstruct
    public void init() {
        stage = new Stage();
        stage.setTitle(message.getMessage("admin_action.title", null, globalConfiguration.getLocale()));
        stage.initModality(Modality.WINDOW_MODAL);

        BorderPane border = new BorderPane();

        TabPane tabPane = new TabPane();
        PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> playableCountry.getId().equals(gameConfig.getIdCountry()));
        if (country != null) {
            tabPane.getTabs().add(createAdminForm(country));
        }
        tabPane.getTabs().add(createAdminList(country));

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
    private Tab createAdminForm(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("admin_action.form", null, globalConfiguration.getLocale()));

        Node unitMaintenancePane = getMaintenanceNode(country);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(unitMaintenancePane);

        tab.setContent(vBox);

        return tab;
    }

    private Node getMaintenanceNode(PlayableCountry country) {
        unitMaintenancePane = new TitledPane();

        unitMaintenanceTable = new TableView<>();
        configureAdminActionTable(unitMaintenanceTable, param -> {
            Request<RemoveAdminActionRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new RemoveAdminActionRequest(param.getId()));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = economicService.removeAdminAction(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when creating room.", e);
                // TODO exception handling
            }
        });

        HBox hBox = new HBox();

        unitMaintenanceCountersChoice = new ChoiceBox<>();
        unitMaintenanceCountersChoice.converterProperty().set(new StringConverter<Counter>() {
            /** {@inheritDoc} */
            @Override
            public String toString(Counter object) {
                return object.getOwner().getProvince() + " - " + object.getType();
            }

            /** {@inheritDoc} */
            @Override
            public Counter fromString(String string) {
                return null;
            }
        });

        ChoiceBox<AdminActionTypeEnum> choiceType = new ChoiceBox<>();
        choiceType.converterProperty().set(new StringConverter<AdminActionTypeEnum>() {
            /** {@inheritDoc} */
            @Override
            public String toString(AdminActionTypeEnum object) {
                return message.getMessage("admin_action.type." + object, null, globalConfiguration.getLocale());
            }

            /** {@inheritDoc} */
            @Override
            public AdminActionTypeEnum fromString(String string) {
                return null;
            }
        });

        unitMaintenanceCountersChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                if (newValue == null) {
                    choiceType.setItems(null);
                } else if (ARMY_LAND_TYPES.contains(newValue.getType())) {
                    choiceType.setItems(FXCollections.observableArrayList(AdminActionTypeEnum.DIS, AdminActionTypeEnum.LM));
                } else if (ARMY_TYPES.contains(newValue.getType())) {
                    choiceType.setItems(FXCollections.observableArrayList(AdminActionTypeEnum.DIS));
                }
            }
        });

        Button btn = new Button(message.getMessage("add", null, globalConfiguration.getLocale()));
        btn.setOnAction(event -> {
            Counter counter = unitMaintenanceCountersChoice.getSelectionModel().getSelectedItem();
            AdminActionTypeEnum type = choiceType.getSelectionModel().getSelectedItem();

            Request<AddAdminActionRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new AddAdminActionRequest(country.getId(), type, counter.getId()));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = economicService.addAdminAction(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when creating room.", e);
                // TODO exception handling
            }
        });

        hBox.getChildren().addAll(unitMaintenanceCountersChoice, choiceType, btn);

        VBox vBox = new VBox();

        vBox.getChildren().addAll(unitMaintenanceTable, hBox);

        unitMaintenancePane.setContent(vBox);

        updateMaintenanceNode(country);

        return unitMaintenancePane;
    }

    /**
     * Update the unit maintenance node with the current game.
     *
     * @param country of the current player.
     */
    private void updateMaintenanceNode(PlayableCountry country) {
        {
            List<AdministrativeAction> actions = country.getAdministrativeActions().stream()
                    .filter(admAct -> admAct.getStatus() == AdminActionStatusEnum.PLANNED &&
                            (admAct.getType() == AdminActionTypeEnum.DIS || admAct.getType() == AdminActionTypeEnum.LM))
                    .collect(Collectors.toList());

            List<Counter> counters = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            ARMY_TYPES.contains(counter.getType())))
                    .collect(Collectors.toList());
            List<Counter> conscriptCounters = new ArrayList<>();
            actions.stream().forEach(action -> {
                Counter counter = CommonUtil.findFirst(counters, o -> o.getId().equals(action.getIdObject()));
                if (action.getType() == AdminActionTypeEnum.LM) {
                    conscriptCounters.add(counter);
                }
                counters.remove(counter);
            });

            Map<CounterFaceTypeEnum, Long> forces = counters.stream().collect(Collectors.groupingBy(Counter::getType, Collectors.counting()));
            List<BasicForce> basicForces = globalConfiguration.getTables().getBasicForces().stream()
                    .filter(basicForce -> StringUtils.equals(basicForce.getCountry(), country.getName()) &&
                            basicForce.getPeriod().getBegin() <= game.getTurn() &&
                            basicForce.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.toList());
            // TODO manage wars
            List<Unit> units = globalConfiguration.getTables().getUnits().stream()
                    .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                            (unit.getAction() == UnitActionEnum.MAINT_WAR || unit.getAction() == UnitActionEnum.MAINT) &&
                            !unit.isSpecial() &&
                            (StringUtils.equals(unit.getTech().getName(), country.getLandTech()) || StringUtils.equals(unit.getTech().getName(), country.getNavalTech()))).collect(Collectors.toList());
            Integer unitMaintenanceCost = MaintenanceUtil.computeMaintenance(forces, basicForces, units);

            Map<CounterFaceTypeEnum, Long> conscriptForces = conscriptCounters.stream().collect(Collectors.groupingBy(Counter::getType, Collectors.counting()));
            List<Unit> conscriptUnits = globalConfiguration.getTables().getUnits().stream()
                    .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                            unit.getAction() == UnitActionEnum.MAINT_WAR &&
                            unit.isSpecial() &&
                            StringUtils.equals(unit.getTech().getName(), country.getLandTech())).collect(Collectors.toList());
            Integer unitMaintenanceConscriptCost = MaintenanceUtil.computeMaintenance(conscriptForces, null, conscriptUnits);

            unitMaintenancePane.setText(message.getMessage("admin_action.form.unit_maintenance", new Object[]{add(unitMaintenanceCost, unitMaintenanceConscriptCost)}, globalConfiguration.getLocale()));
            unitMaintenanceTable.setItems(FXCollections.observableArrayList(actions));
            unitMaintenanceCountersChoice.setItems(FXCollections.observableArrayList(counters));
        }

    }

    /**
     * Creates the tab for the income sheet.
     *
     * @param country default country sheet to display.
     * @return the tab for the income sheet.
     */
    private Tab createAdminList(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("admin_action.list", null, globalConfiguration.getLocale()));

        ChoiceBox<PlayableCountry> choiceCountry = new ChoiceBox<>();
        choiceCountry.setItems(FXCollections.observableArrayList(game.getCountries()));
        choiceCountry.converterProperty().set(new StringConverter<PlayableCountry>() {
            /** {@inheritDoc} */
            @Override
            public String toString(PlayableCountry object) {
                return object.getName();
            }

            /** {@inheritDoc} */
            @Override
            public PlayableCountry fromString(String string) {
                PlayableCountry country = null;

                for (PlayableCountry countryTest : game.getCountries()) {
                    if (StringUtils.equals(string, countryTest.getName())) {
                        country = countryTest;
                        break;
                    }
                }

                return country;
            }
        });

        ChoiceBox<Integer> choiceTurn = new ChoiceBox<>();

        HBox hBox = new HBox();
        hBox.getChildren().addAll(choiceCountry, choiceTurn);

        TableView<AdministrativeAction> table = new TableView<>();
        configureAdminActionTable(table, null);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, table);

        tab.setContent(vBox);

        choiceCountry.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                List<Integer> turns = newValue.getAdministrativeActions().stream().map(AdministrativeAction::getTurn).distinct().collect(Collectors.toList());
                choiceTurn.setItems(FXCollections.observableArrayList(turns));
            }
        });
        choiceCountry.getSelectionModel().select(country);

        choiceTurn.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                table.setItems(null);
            } else if (!newValue.equals(oldValue)) {
                List<AdministrativeAction> actions = choiceCountry.getSelectionModel().getSelectedItem().getAdministrativeActions().stream().filter(administrativeAction -> administrativeAction.getTurn().equals(newValue)).collect(Collectors.toList());
                table.setItems(FXCollections.observableArrayList(actions));
            }
        });

        return tab;
    }

    /**
     * Configure the administrative actions table.
     *
     * @param table to configure.
     */
    private void configureAdminActionTable(TableView<AdministrativeAction> table, Consumer<AdministrativeAction> callback) {
        table.setTableMenuButtonVisible(true);
        table.setPrefWidth(750);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AdministrativeAction, String> column = new TableColumn<>(message.getMessage("admin_action.turn", null, globalConfiguration.getLocale()));
        column.setPrefWidth(30);
        column.setCellValueFactory(new PropertyValueFactory<>("turn"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("admin_action.action", null, globalConfiguration.getLocale()));
        column.setPrefWidth(400);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(message.getMessage("admin_action.type." + param.getValue().getType(), null, globalConfiguration.getLocale())));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("admin_action.cost", null, globalConfiguration.getLocale()));
        column.setPrefWidth(30);
        column.setCellValueFactory(new PropertyValueFactory<>("cost"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("admin_action.column", null, globalConfiguration.getLocale()));
        column.setPrefWidth(30);
        column.setCellValueFactory(new PropertyValueFactory<>("column"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("admin_action.bonus", null, globalConfiguration.getLocale()));
        column.setPrefWidth(30);
        column.setCellValueFactory(new PropertyValueFactory<>("bonus"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("admin_action.result", null, globalConfiguration.getLocale()));
        column.setPrefWidth(30);
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(message.getMessage("admin_action.result." + param.getValue().getResult(), null, globalConfiguration.getLocale())));
        table.getColumns().add(column);

        if (callback != null) {
            column = new TableColumn<>(message.getMessage("admin_action.actions", null, globalConfiguration.getLocale()));
            column.setPrefWidth(70);
            column.setCellValueFactory(new PropertyValueFactory<>("NONE"));
            Callback<TableColumn<AdministrativeAction, String>, TableCell<AdministrativeAction, String>> cellFactory = new Callback<TableColumn<AdministrativeAction, String>, TableCell<AdministrativeAction, String>>() {
                @Override
                public TableCell<AdministrativeAction, String> call(TableColumn<AdministrativeAction, String> param) {
                    return new TableCell<AdministrativeAction, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                Button btn = new Button(message.getMessage("delete", null, globalConfiguration.getLocale()));
                                btn.setOnAction(event -> {
                                    AdministrativeAction adminAction = getTableView().getItems().get(getIndex());
                                    callback.accept(adminAction);
                                });
                                setGraphic(btn);
                                setText(null);
                            }
                        }
                    };
                }
            };
            column.setCellFactory(cellFactory);
            table.getColumns().add(column);
        }
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
     * Update the window given the diff.
     *
     * @param diff that will update the window.
     */
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case ADM_ACT:
                updateAdmAct(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process a eco sheet diff event.
     *
     * @param diff involving an administrative action.
     */
    private void updateAdmAct(Diff diff) {
        switch (diff.getType()) {
            case ADD:
            case REMOVE:
                DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
                if (attribute != null) {
                    AdminActionTypeEnum type = AdminActionTypeEnum.valueOf(attribute.getValue());
                    if (type != null) {
                        switch (type) {
                            case DIS:
                            case LM:
                                PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> playableCountry.getId().equals(gameConfig.getIdCountry()));
                                if (country != null) {
                                    updateMaintenanceNode(country);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
