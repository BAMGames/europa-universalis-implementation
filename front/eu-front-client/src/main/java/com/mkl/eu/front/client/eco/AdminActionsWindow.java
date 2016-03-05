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
import com.mkl.eu.client.service.vo.tables.Limit;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.client.service.vo.tables.Unit;
import com.mkl.eu.client.service.vo.util.MaintenanceUtil;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    /** Counter Face Type for land armies. */
    private static final List<CounterFaceTypeEnum> FORT_TYPES = new ArrayList<>();
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
    /** Markers of the loaded game. */
    private List<IMapMarker> markers;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Stage of the window. */
    private Stage stage;

    /********************************************/
    /**        Nodes about maintenance          */
    /********************************************/
    /** The TitledPane containing all the other nodes. */
    private TitledPane maintenancePane;
    /** The TableView containing the already planned actions. */
    private TableView<AdministrativeAction> maintenanceTable;
    /** The ChoiceBox containing the remaining counters. */
    private ChoiceBox<Counter> maintenanceCountersChoice;

    /********************************************/
    /**        Nodes about purchase          */
    /********************************************/
    /** The TitledPane containing all the other nodes. */
    private TitledPane purchasePane;
    /** The TableView containing the already planned actions. */
    private TableView<AdministrativeAction> purchaseTable;
    /** The ChoiceBox containing the remaining counters. */
    private ChoiceBox<IMapMarker> purchaseProvincesChoice;
    /** The ChoiceBox containing the type of counters that can be added. */
    private ChoiceBox<CounterFaceTypeEnum> purchaseTypeChoice;

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

        FORT_TYPES.add(CounterFaceTypeEnum.FORT);
        FORT_TYPES.add(CounterFaceTypeEnum.FORTRESS_1);
        FORT_TYPES.add(CounterFaceTypeEnum.FORTRESS_2);
        FORT_TYPES.add(CounterFaceTypeEnum.FORTRESS_3);
        FORT_TYPES.add(CounterFaceTypeEnum.FORTRESS_4);
        FORT_TYPES.add(CounterFaceTypeEnum.FORTRESS_5);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_2);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_0_ST_PETER);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_1_ST_PETER);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_2_ST_PETER);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_3);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_3_GIBRALTAR);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_3_SEBASTOPOL);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_3_ST_PETER);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_4);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_4_ST_PETER);
        FORT_TYPES.add(CounterFaceTypeEnum.ARSENAL_5_ST_PETER);
    }

    /**
     * Constructor.
     *
     * @param game       the game to set.
     * @param gameConfig the gameConfig to set.
     */
    public AdminActionsWindow(Game game, List<IMapMarker> markers, GameConfiguration gameConfig) {
        this.game = game;
        this.markers = markers;
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
     * Creates the tab for the form of the current administrative actions.
     *
     * @param country currently logged (<ocde>null</ocde> if not in this game).
     * @return the tab for the form of the current administrative actions.
     */
    private Tab createAdminForm(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("admin_action.form", null, globalConfiguration.getLocale()));
        tab.setClosable(false);

        Node unitMaintenancePane = createMaintenanceNode(country);
        Node unitPurchasePane = createPurchaseNode(country);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(unitMaintenancePane, unitPurchasePane);

        tab.setContent(vBox);

        return tab;
    }

    /**
     * Create the node for the unit maintenance.
     *
     * @param country of the current player.
     * @return the node for the unit maintenance.
     */
    private Node createMaintenanceNode(PlayableCountry country) {
        maintenancePane = new TitledPane();

        maintenanceTable = new TableView<>();
        configureAdminActionTable(maintenanceTable, this::removeAdminAction);

        HBox hBox = new HBox();

        maintenanceCountersChoice = new ChoiceBox<>();
        maintenanceCountersChoice.converterProperty().set(new StringConverter<Counter>() {
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

        ChoiceBox<CounterFaceTypeEnum> toCounterChoice = new ChoiceBox<>();
        toCounterChoice.setVisible(false);
        toCounterChoice.converterProperty().set(new StringConverter<CounterFaceTypeEnum>() {
            /** {@inheritDoc} */
            @Override
            public String toString(CounterFaceTypeEnum object) {
                return message.getMessage(object + "", null, globalConfiguration.getLocale());
            }

            /** {@inheritDoc} */
            @Override
            public CounterFaceTypeEnum fromString(String string) {
                return null;
            }
        });

        choiceType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                if (newValue == AdminActionTypeEnum.LF) {
                    toCounterChoice.setVisible(true);
                    Counter start = maintenanceCountersChoice.getSelectionModel().getSelectedItem();
                    IMapMarker province = CommonUtil.findFirst(markers, marker -> StringUtils.equals(marker.getId(), start.getOwner().getProvince()));
                    List<CounterFaceTypeEnum> fortressTypes = getLowerFortresses(start.getType(), province.getFortressLevel());
                    toCounterChoice.setItems(FXCollections.observableArrayList(fortressTypes));
                } else {
                    toCounterChoice.setItems(FXCollections.observableArrayList());
                    toCounterChoice.setVisible(false);
                }
            }
        });

        maintenanceCountersChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                if (newValue == null) {
                    choiceType.setItems(FXCollections.observableArrayList());
                } else if (ARMY_LAND_TYPES.contains(newValue.getType())) {
                    choiceType.setItems(FXCollections.observableArrayList(AdminActionTypeEnum.DIS, AdminActionTypeEnum.LM));
                } else if (ARMY_TYPES.contains(newValue.getType())) {
                    choiceType.setItems(FXCollections.observableArrayList(AdminActionTypeEnum.DIS));
                } else if (FORT_TYPES.contains(newValue.getType())) {
                    choiceType.setItems(FXCollections.observableArrayList(AdminActionTypeEnum.DIS, AdminActionTypeEnum.LF));
                }
            }
        });

        Button btn = new Button(message.getMessage("add", null, globalConfiguration.getLocale()));
        btn.setOnAction(event -> {
            Counter counter = maintenanceCountersChoice.getSelectionModel().getSelectedItem();
            AdminActionTypeEnum type = choiceType.getSelectionModel().getSelectedItem();
            CounterFaceTypeEnum toCounter = toCounterChoice.getSelectionModel().getSelectedItem();

            Request<AddAdminActionRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new AddAdminActionRequest(country.getId(), type, counter.getId(), toCounter));
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

        hBox.getChildren().addAll(maintenanceCountersChoice, choiceType, toCounterChoice, btn);

        VBox vBox = new VBox();

        vBox.getChildren().addAll(maintenanceTable, hBox);

        maintenancePane.setContent(vBox);

        updateMaintenanceNode(country);

        return maintenancePane;
    }

    /**
     * @param actual the actual fortress type.
     * @param level of the natural fortress of the province.
     * @return the potential lower fortresses that can be maintained given the actual fortress type and the natural fortress level of the province.
     */
    private List<CounterFaceTypeEnum> getLowerFortresses(CounterFaceTypeEnum actual, int level) {
        List<CounterFaceTypeEnum> fortresses = new ArrayList<>();

        if (actual != null) {
            switch (actual) {
                case FORTRESS_5:
                    if (level < 4) {
                        fortresses.add(CounterFaceTypeEnum.FORTRESS_4);
                    }
                case FORTRESS_4:
                    if (level < 3) {
                        fortresses.add(CounterFaceTypeEnum.FORTRESS_3);
                    }
                case FORTRESS_3:
                    if (level < 2) {
                        fortresses.add(CounterFaceTypeEnum.FORTRESS_2);
                    }
                case FORTRESS_2:
                    if (level < 1) {
                        fortresses.add(CounterFaceTypeEnum.FORTRESS_1);
                    }
                case FORTRESS_1:
                    break;
                case ARSENAL_5_ST_PETER:
                    if (level < 4) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_4_ST_PETER);
                    }
                case ARSENAL_4_ST_PETER:
                    if (level < 3) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_3_ST_PETER);
                    }
                case ARSENAL_3_ST_PETER:
                    if (level < 2) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_2_ST_PETER);
                    }
                case ARSENAL_2_ST_PETER:
                    if (level < 1) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_1_ST_PETER);
                    }
                case ARSENAL_1_ST_PETER:
                case ARSENAL_0_ST_PETER:
                    break;
                case ARSENAL_3_SEBASTOPOL:
                    if (level < 2) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_2_SEBASTOPOL);
                    }
                case ARSENAL_2_SEBASTOPOL:
                    break;
                case ARSENAL_3_GIBRALTAR:
                    if (level < 2) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_2_GIBRALTAR);
                    }
                case ARSENAL_2_GIBRALTAR:
                    break;
                case ARSENAL_4:
                    if (level < 3) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_3);
                    }
                case ARSENAL_3:
                    if (level < 2) {
                        fortresses.add(CounterFaceTypeEnum.ARSENAL_2);
                    }
                case ARSENAL_2:
                    break;
            }
        }

        return fortresses;
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
                            (admAct.getType() == AdminActionTypeEnum.DIS || admAct.getType() == AdminActionTypeEnum.LM) || admAct.getType() == AdminActionTypeEnum.LF)
                    .collect(Collectors.toList());

            List<Counter> counters = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            ARMY_TYPES.contains(counter.getType())))
                    .collect(Collectors.toList());
            List<Counter> conscriptCounters = new ArrayList<>();
            List<Counter> fortresses = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            FORT_TYPES.contains(counter.getType())))
                    .collect(Collectors.toList());
            Map<Pair<Integer, Boolean>, Integer> orderedFortresses = fortresses.stream().collect(Collectors.groupingBy(
                    this::getFortressKeyFromCounter,
                    Collectors.summingInt(value -> 1)));

            actions.stream().forEach(action -> {
                Counter counter = CommonUtil.findFirst(counters, o -> o.getId().equals(action.getIdObject()));
                if (counter != null) {
                    if (action.getType() == AdminActionTypeEnum.LM) {
                        conscriptCounters.add(counter);
                    }
                    counters.remove(counter);
                }
                counter = CommonUtil.findFirst(fortresses, o -> o.getId().equals(action.getIdObject()));
                if (counter != null) {
                    if (action.getType() == AdminActionTypeEnum.LF) {
                        CommonUtil.addOne(orderedFortresses,
                                new ImmutablePair<>(MaintenanceUtil.getFortressLevelFromType(action.getCounterFaceType()), counter.getOwner().getProvince().startsWith("r")));
                    }
                    CommonUtil.subtractOne(orderedFortresses, getFortressKeyFromCounter(counter));
                    fortresses.remove(counter);
                }
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
            Integer unitMaintenanceCost = MaintenanceUtil.computeUnitMaintenance(forces, basicForces, units);

            Map<CounterFaceTypeEnum, Long> conscriptForces = conscriptCounters.stream().collect(Collectors.groupingBy(Counter::getType, Collectors.counting()));
            List<Unit> conscriptUnits = globalConfiguration.getTables().getUnits().stream()
                    .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                            unit.getAction() == UnitActionEnum.MAINT_WAR &&
                            unit.isSpecial() &&
                            StringUtils.equals(unit.getTech().getName(), country.getLandTech())).collect(Collectors.toList());
            Integer unitMaintenanceConscriptCost = MaintenanceUtil.computeUnitMaintenance(conscriptForces, null, conscriptUnits);

            Tech ownerLandTech = CommonUtil.findFirst(globalConfiguration.getTables().getTechs(), tech -> StringUtils.equals(tech.getName(), country.getLandTech()));

            Integer fortressesMaintenance = MaintenanceUtil.computeFortressesMaintenance(
                    orderedFortresses,
                    globalConfiguration.getTables().getTechs(),
                    ownerLandTech,
                    game.getTurn());

            List<Counter> missions = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                    .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                            counter.getType() == CounterFaceTypeEnum.MISSION))
                    .collect(Collectors.toList());

            Integer missionMaintenance = missions.size();

            maintenancePane.setText(message.getMessage("admin_action.form.unit_maintenance", new Object[]{add(unitMaintenanceCost, unitMaintenanceConscriptCost), fortressesMaintenance, missionMaintenance}, globalConfiguration.getLocale()));
            maintenanceTable.setItems(FXCollections.observableArrayList(actions));
            ObservableList<Counter> counterList = FXCollections.observableArrayList(counters);
            counterList.addAll(fortresses);
            maintenanceCountersChoice.setItems(counterList);
        }

    }

    /**
     * @param counter whose we want the key.
     * @return the key used for computing fortress maintenance. It is a Pair consisting of level and location (<code>true</code> for ROTW).
     */
    private Pair<Integer, Boolean> getFortressKeyFromCounter(Counter counter) {
        return new ImmutablePair<>(MaintenanceUtil.getFortressLevelFromType(counter.getType()), counter.getOwner().getProvince().startsWith("r"));
    }

    /**
     * Create the node for the unit purchase.
     *
     * @param country of the current player.
     * @return the node for the unit purchase.
     */
    private Node createPurchaseNode(PlayableCountry country) {
        purchasePane = new TitledPane();

        purchaseTable = new TableView<>();
        configureAdminActionTable(purchaseTable, this::removeAdminAction);

        HBox hBox = new HBox();

        purchaseProvincesChoice = new ChoiceBox<>();
        purchaseProvincesChoice.converterProperty().set(new StringConverter<IMapMarker>() {
            /** {@inheritDoc} */
            @Override
            public String toString(IMapMarker object) {
                return message.getMessage(object.getId(), null, globalConfiguration.getLocale());
            }

            /** {@inheritDoc} */
            @Override
            public IMapMarker fromString(String string) {
                return null;
            }
        });

        purchaseTypeChoice = new ChoiceBox<>();
        purchaseTypeChoice.converterProperty().set(new StringConverter<CounterFaceTypeEnum>() {
            /** {@inheritDoc} */
            @Override
            public String toString(CounterFaceTypeEnum object) {
                return object.name();
            }

            /** {@inheritDoc} */
            @Override
            public CounterFaceTypeEnum fromString(String string) {
                return null;
            }
        });

        Button btn = new Button(message.getMessage("add", null, globalConfiguration.getLocale()));
        btn.setOnAction(event -> {
            IMapMarker province = purchaseProvincesChoice.getSelectionModel().getSelectedItem();
            CounterFaceTypeEnum type = purchaseTypeChoice.getSelectionModel().getSelectedItem();

            Request<AddAdminActionRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new AddAdminActionRequest(country.getId(), AdminActionTypeEnum.PU, province.getId(), type));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = economicService.addAdminAction(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when creating administrative action.", e);

                UIUtil.showException(e, globalConfiguration, message);
            }
        });

        hBox.getChildren().addAll(purchaseProvincesChoice, purchaseTypeChoice, btn);

        VBox vBox = new VBox();

        vBox.getChildren().addAll(purchaseTable, hBox);

        purchasePane.setContent(vBox);

        purchaseProvincesChoice.setItems(FXCollections.observableArrayList(markers.stream()
                .filter(marker -> StringUtils.equals(country.getName(), marker.getOwner()) &&
                        StringUtils.equals(country.getName(), marker.getController())).collect(Collectors.toList())));

        List<Unit> forces = globalConfiguration.getTables().getUnits().stream()
                .filter(unit -> StringUtils.equals(unit.getCountry(), country.getName()) &&
                        unit.getAction() == UnitActionEnum.PURCHASE &&
                        (StringUtils.equals(unit.getTech().getName(), country.getLandTech()) || StringUtils.equals(unit.getTech().getName(), country.getNavalTech()))).collect(Collectors.toList());


        purchaseProvincesChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                if (newValue == null) {
                    purchaseTypeChoice.setItems(null);
                } else {
                    List<CounterFaceTypeEnum> faces = forces.stream().filter(force -> newValue.isPort() || force.getTech().isLand()).flatMap(force -> getFacesFromPurchaseForce(force.getType(), country.getName()).stream()).collect(Collectors.toList());
                    purchaseTypeChoice.setItems(FXCollections.observableArrayList(faces));
                }
            }
        });

        updatePurchaseNode(country);

        return purchasePane;
    }

    /**
     * Update the unit purchase node with the current game.
     *
     * @param country of the current player.
     */
    private void updatePurchaseNode(PlayableCountry country) {
        List<AdministrativeAction> actions = country.getAdministrativeActions().stream()
                .filter(admAct -> admAct.getStatus() == AdminActionStatusEnum.PLANNED &&
                        admAct.getType() == AdminActionTypeEnum.PU)
                .collect(Collectors.toList());

        Map<Boolean, Integer> currentPurchase = actions.stream().collect(Collectors.groupingBy(action -> isLand(action.getCounterFaceType()), Collectors.summingInt(action -> MaintenanceUtil.getSizeFromType(action.getCounterFaceType()))));
        Map<LimitTypeEnum, Integer> maxPurchase = globalConfiguration.getTables().getLimits().stream().filter(
                limit -> StringUtils.equals(limit.getCountry(), country.getName()) &&
                        limit.getPeriod().getBegin() <= game.getTurn() &&
                        limit.getPeriod().getEnd() >= game.getTurn()).collect(Collectors.groupingBy(Limit::getType, Collectors.summingInt(Limit::getNumber)));

        purchasePane.setText(message.getMessage("admin_action.form.unit_purchase", new Object[]{currentPurchase.get(true), maxPurchase.get(LimitTypeEnum.PURCHASE_LAND_TROOPS), currentPurchase.get(false), maxPurchase.get(LimitTypeEnum.PURCHASE_NAVAL_TROOPS)}, globalConfiguration.getLocale()));
        purchaseTable.setItems(FXCollections.observableArrayList(actions));
    }

    /**
     * Returns whether the type of the counter face is land or not.
     *
     * @param face the face.
     * @return if it is land.
     */
    private Boolean isLand(CounterFaceTypeEnum face) {
        Boolean land = null;

        if (face != null) {
            switch (face) {
                case ARMY_PLUS:
                case ARMY_MINUS:
                case LAND_DETACHMENT:
                case LAND_DETACHMENT_TIMAR:
                case LAND_DETACHMENT_KOZAK:
                    land = true;
                    break;
                case FLEET_PLUS:
                case FLEET_MINUS:
                case NAVAL_DETACHMENT:
                case NAVAL_TRANSPORT:
                case NAVAL_GALLEY:
                    land = false;
                    break;
                default:
                    break;
            }
        }

        return land;
    }

    /**
     * Returns the different possibilities for a type of force for a counter type.
     *
     * @param force   type of force in a unit of action PURCHASE.
     * @param country for special rules (turkey, france, hollande, england,...)
     * @return the List of type faces.
     */
    private List<CounterFaceTypeEnum> getFacesFromPurchaseForce(ForceTypeEnum force, String country) {
        List<CounterFaceTypeEnum> faces = new ArrayList<>();

        if (force != null) {
            switch (force) {
                case ARMY_MINUS:
                    faces.add(CounterFaceTypeEnum.ARMY_MINUS);
                    if (StringUtils.equals(PlayableCountry.TURKEY, country)) {
                        faces.add(CounterFaceTypeEnum.ARMY_TIMAR_MINUS);
                    }
                    break;
                case LD:
                    faces.add(CounterFaceTypeEnum.LAND_DETACHMENT);
                    if (StringUtils.equals(PlayableCountry.TURKEY, country)) {
                        faces.add(CounterFaceTypeEnum.LAND_DETACHMENT_TIMAR);
                    }
                    break;
                case FLEET_GALLEY_MINUS:
                case FLEET_MINUS:
                    faces.add(CounterFaceTypeEnum.FLEET_MINUS);
                    break;
                case NWD:
                    faces.add(CounterFaceTypeEnum.NAVAL_DETACHMENT);
                    break;
                case NGD:
                    faces.add(CounterFaceTypeEnum.NAVAL_GALLEY);
                    break;
                case NTD:
                    faces.add(CounterFaceTypeEnum.NAVAL_TRANSPORT);
                    break;
                default:
                    break;
            }
        }

        return faces;
    }

    /**
     * Method called for removing a PLANNED administrative action.
     *
     * @param param the administrative action to remove.
     */
    private void removeAdminAction(AdministrativeAction param) {
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
    }

    /**
     * Creates the tab for the administrative actions already done.
     *
     * @param country of the current player.
     * @return the tab for the administrative actions already done
     */
    private Tab createAdminList(PlayableCountry country) {
        Tab tab = new Tab(message.getMessage("admin_action.list", null, globalConfiguration.getLocale()));
        tab.setClosable(false);

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
                    PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> playableCountry.getId().equals(gameConfig.getIdCountry()));
                    if (type != null && country != null) {
                        switch (type) {
                            case DIS:
                            case LM:
                                updateMaintenanceNode(country);
                                break;
                            case PU:
                                updatePurchaseNode(country);
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
