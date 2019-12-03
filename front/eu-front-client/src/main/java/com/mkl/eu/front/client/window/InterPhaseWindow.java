package com.mkl.eu.front.client.window;

import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.service.military.LandRedeployRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.LandLootTypeEnum;
import com.mkl.eu.front.client.common.EnumConverter;
import com.mkl.eu.front.client.common.StackInProvinceCellFactory;
import com.mkl.eu.front.client.common.StackInProvinceConverter;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Window containing the military (battles, sieges,...).
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class InterPhaseWindow extends AbstractDiffResponseListenerContainer implements IDiffListener {
    /** InterPhase service. */
    @Autowired
    private IInterPhaseService interPhaseService;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Game. */
    private Game game;
    /** Markers of the loaded game. */
    private List<IMapMarker> markers;
    /** Global node. */
    private TabPane tabPane;

    /********************************************/
    /**       Nodes about redeployment          */
    /********************************************/
    /** The lootStack combo box. */
    private ComboBox<Stack> lootStack;
    /** The type of loot choice box. */
    private ChoiceBox<LandLootTypeEnum> lootType;
    /** The loot button. */
    private Button lootButton;
    /** The redeployStack combo box. */
    private ComboBox<Stack> redeployStack;
    /** Choice box to select the province to redeploy. */
    private ChoiceBox<String> redeployProvince;
    /** The redeploy button. */
    private Button redeployButton;
    /** The validate interphase button. */
    private Button validateInterPhase;
    /** The invalidate interphase button. */
    private Button invalidateInterPhase;


    /**
     * Constructor.
     *
     * @param game       the game to set.
     * @param gameConfig the gameConfig to set.
     */
    public InterPhaseWindow(Game game, List<IMapMarker> markers, GameConfiguration gameConfig) {
        super(gameConfig);
        this.game = game;
        this.markers = markers;
    }

    /** @return the tabPane. */
    public TabPane getTabPane() {
        return tabPane;
    }

    /**
     * Initialize the window.
     */
    @PostConstruct
    public void init() {
        tabPane = new TabPane();
        tabPane.getTabs().add(createRedeploymentTab());
        tabPane.getTabs().add(createExchequerTab());
        tabPane.getTabs().add(createStabilityTab());
    }

    /**
     * @return the redeployment tab.
     */
    private Tab createRedeploymentTab() {
        Tab tab = new Tab(globalConfiguration.getMessage("interphase.info.title"));
        tab.setClosable(false);
        VBox vBox = new VBox();
        tab.setContent(vBox);

        lootStack = new ComboBox<>();
        lootStack.setCellFactory(new StackInProvinceCellFactory(globalConfiguration));
        lootStack.converterProperty().set(new StackInProvinceConverter(globalConfiguration));
        lootType = new ChoiceBox<>();
        lootType.converterProperty().set(new EnumConverter<>(globalConfiguration));
        lootType.setItems(FXCollections.observableArrayList(LandLootTypeEnum.values()));
        lootStack.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> lootButton.setDisable(newValue == null
                        || lootType.getSelectionModel().getSelectedItem() == null));
        lootType.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> lootButton.setDisable(newValue == null
                        || lootStack.getSelectionModel().getSelectedItem() == null));
        lootButton = new Button(globalConfiguration.getMessage("interphase.info.loot"));
        lootButton.setOnAction(callServiceAsEvent(interPhaseService::landLooting,
                () -> new LandLootingRequest(lootStack.getSelectionModel().getSelectedItem().getId(), lootType.getSelectionModel().getSelectedItem()),
                "Error when looting by land."));
        HBox hBox = new HBox();
        hBox.getChildren().addAll(lootStack, lootType, lootButton);
        vBox.getChildren().add(hBox);

        redeployStack = new ComboBox<>();
        redeployStack.setCellFactory(new StackInProvinceCellFactory(globalConfiguration));
        redeployStack.converterProperty().set(new StackInProvinceConverter(globalConfiguration));
        redeployProvince = new ChoiceBox<>();
        redeployStack.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> redeployButton.setDisable(newValue == null
                        || redeployProvince.getSelectionModel().getSelectedItem() == null));
        redeployProvince.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> redeployButton.setDisable(newValue == null
                        || redeployStack.getSelectionModel().getSelectedItem() == null));
        redeployButton = new Button(globalConfiguration.getMessage("interphase.info.redeploy"));
        redeployButton.setOnAction(callServiceAsEvent(interPhaseService::landRedeploy,
                () -> new LandRedeployRequest(redeployStack.getSelectionModel().getSelectedItem().getId(), redeployProvince.getSelectionModel().getSelectedItem()),
                "Error when redeploying land units."));
        hBox = new HBox();
        hBox.getChildren().addAll(redeployStack, redeployProvince, redeployButton);
        vBox.getChildren().add(hBox);

        Function<Boolean, EventHandler<ActionEvent>> endInterPhase = validate -> callServiceAsEvent(interPhaseService::validateRedeploy, () -> new ValidateRequest(validate), "Error when validating the interphase.");

        validateInterPhase = new Button(globalConfiguration.getMessage("interphase.info.validate"));
        validateInterPhase.setOnAction(endInterPhase.apply(true));
        invalidateInterPhase = new Button(globalConfiguration.getMessage("interphase.info.invalidate"));
        invalidateInterPhase.setOnAction(endInterPhase.apply(false));
        hBox = new HBox();
        hBox.getChildren().addAll(validateInterPhase, invalidateInterPhase);
        vBox.getChildren().add(hBox);

        updateRedeploymentPanel();

        return tab;
    }

    /**
     * Updates the redeployment tab.
     */
    private void updateRedeploymentPanel() {
        List<Stack> stacks = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getCountry(), gameConfig.getCountryName()) &&
                        CounterUtil.isMobile(stack) && CounterUtil.isLandArmy(stack))
                .collect(Collectors.toList());
        lootStack.setItems(FXCollections.observableList(stacks));
        redeployStack.setItems(FXCollections.observableList(stacks));
        List<String> provinces = markers.stream()
                .filter(province -> StringUtils.equals(province.getController(), gameConfig.getCountryName()))
                .map(IMapMarker::getId)
                .collect(Collectors.toList());
        redeployProvince.setItems(FXCollections.observableList(provinces));

        CountryOrder countryOrder = game.getOrders().stream()
                .filter(order -> Objects.equals(order.getCountry().getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        lootStack.setDisable(true);
        lootType.setDisable(true);
        lootButton.setDisable(true);
        redeployStack.setDisable(true);
        redeployProvince.setDisable(true);
        redeployButton.setDisable(true);
        validateInterPhase.setDisable(true);
        invalidateInterPhase.setDisable(true);

        if (countryOrder != null && countryOrder.isActive()) {
            if (game.getStatus() == GameStatusEnum.REDEPLOYMENT) {
                lootStack.setDisable(countryOrder.isReady());
                lootType.setDisable(countryOrder.isReady());
                redeployStack.setDisable(countryOrder.isReady());
                redeployProvince.setDisable(countryOrder.isReady());
                validateInterPhase.setDisable(countryOrder.isReady());
                invalidateInterPhase.setDisable(!countryOrder.isReady());
            }
        }
    }

    /**
     * @return the exchequer tab.
     */
    private Tab createExchequerTab() {
        Tab tab = new Tab(globalConfiguration.getMessage("interphase.info.exchequer"));
        tab.setClosable(false);
        VBox vBox = new VBox();
        tab.setContent(vBox);
        return tab;
    }

    /**
     * @return the stability tab.
     */
    private Tab createStabilityTab() {
        Tab tab = new Tab(globalConfiguration.getMessage("interphase.info.stab"));
        tab.setClosable(false);
        VBox vBox = new VBox();
        tab.setContent(vBox);
        return tab;
    }

    /** {@inheritDoc} */
    @Override
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case GAME:
            case STATUS:
            case TURN_ORDER:
                updateRedeploymentPanel();
                break;
            case REDEPLOY:
                checkNotification(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Apply a possible notification from the client.
     *
     * @param diff the diff.
     */
    private void checkNotification(Diff diff) {
        if (diff.getType() == DiffTypeEnum.NOTIFY) {
            Long idStack = diff.getAttributes().stream()
                    .filter(d -> d.getType() == DiffAttributeTypeEnum.STACK)
                    .map(d -> Long.valueOf(d.getValue()))
                    .findAny()
                    .orElse(null);
            Stack stack = game.getStacks().stream()
                    .filter(s -> Objects.equals(s.getId(), idStack))
                    .findAny()
                    .orElse(null);
            redeployStack.getSelectionModel().select(stack);
            tabPane.getSelectionModel().select(0);
            tabPane.requestFocus();
        }
    }
}
