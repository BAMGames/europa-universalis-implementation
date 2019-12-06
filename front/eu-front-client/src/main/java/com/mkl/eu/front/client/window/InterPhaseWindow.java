package com.mkl.eu.front.client.window;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IInterPhaseService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.eco.ExchequerRepartitionRequest;
import com.mkl.eu.client.service.service.eco.ImproveStabilityRequest;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.service.military.LandRedeployRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.util.WarUtil;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.eco.EconomicalSheet;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.tables.Result;
import com.mkl.eu.front.client.common.EnumConverter;
import com.mkl.eu.front.client.common.StackInProvinceCellFactory;
import com.mkl.eu.front.client.common.StackInProvinceConverter;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    /** The validate redeployment button. */
    private Button validateRedeployment;
    /** The invalidate redeployment button. */
    private Button invalidateRedeployment;

    /********************************************/
    /**        Nodes about exchequer            */
    /********************************************/
    /** The exchequer info. */
    private HBox exchequerInfo;
    /** The prestige spent field. */
    private TextField prestigeField;
    /** Exchequer repartition button. */
    private Button exchequerRepartition;
    /** The validate exchequer button. */
    private Button validateExchequer;
    /** The invalidate exchequer button. */
    private Button invalidateExchequer;

    /********************************************/
    /**        Nodes about stability            */
    /********************************************/
    /** The improve stability button. */
    private Button improveStability;
    /** The stabilit info. */
    private Label stabInfo;


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

        updateEcoSheet();
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

        Function<Boolean, EventHandler<ActionEvent>> endRedeployment = validate -> callServiceAsEvent(interPhaseService::validateRedeploy, () -> new ValidateRequest(validate), "Error when validating the redeployment.");

        validateRedeployment = new Button(globalConfiguration.getMessage("interphase.info.validate"));
        validateRedeployment.setOnAction(endRedeployment.apply(true));
        invalidateRedeployment = new Button(globalConfiguration.getMessage("interphase.info.invalidate"));
        invalidateRedeployment.setOnAction(endRedeployment.apply(false));
        hBox = new HBox();
        hBox.getChildren().addAll(validateRedeployment, invalidateRedeployment);
        vBox.getChildren().add(hBox);

        updateRedeploymentPanel();

        return tab;
    }

    /**
     * Updates the redeployment tab.
     */
    private void updateRedeploymentPanel() {
        List<String> allies = WarUtil.getAllies(gameConfig.getCountryName(), game);
        allies.removeIf(ally -> game.getCountries().stream()
                .anyMatch(country -> StringUtils.equals(country.getName(), ally) &&
                        !StringUtils.equals(country.getName(), gameConfig.getCountryName()) &&
                        StringUtils.isNotEmpty(country.getUsername())));
        List<Stack> stacks = game.getStacks().stream()
                .filter(stack -> allies.contains(stack.getCountry()) &&
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
        validateRedeployment.setDisable(true);
        invalidateRedeployment.setDisable(true);

        if (countryOrder != null && countryOrder.isActive()) {
            if (game.getStatus() == GameStatusEnum.REDEPLOYMENT) {
                lootStack.setDisable(countryOrder.isReady());
                lootType.setDisable(countryOrder.isReady());
                redeployStack.setDisable(countryOrder.isReady());
                redeployProvince.setDisable(countryOrder.isReady());
                validateRedeployment.setDisable(countryOrder.isReady());
                invalidateRedeployment.setDisable(!countryOrder.isReady());
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

        exchequerInfo = new HBox();
        vBox.getChildren().add(exchequerInfo);

        prestigeField = new TextField();
        prestigeField.setTextFormatter(new TextFormatter<String>(change -> change.getText().matches("[0-9]*") ? change : null));
        exchequerRepartition = new Button(globalConfiguration.getMessage("interphase.info.exchequer.prestige_spent"));
        exchequerRepartition.setOnAction(callServiceAsEvent(interPhaseService::exchequerRepartition, () -> new ExchequerRepartitionRequest(CommonUtil.toInt(prestigeField.getText())), "Error when spending prestige into income."));

        HBox hBox = new HBox();
        hBox.getChildren().addAll(prestigeField, exchequerRepartition);
        vBox.getChildren().add(hBox);

        Function<Boolean, EventHandler<ActionEvent>> endExchequer = validate -> callServiceAsEvent(interPhaseService::validateExchequer, () -> new ValidateRequest(validate), "Error when validating the exchequer.");

        validateExchequer = new Button(globalConfiguration.getMessage("interphase.exchequer.validate"));
        validateExchequer.setOnAction(endExchequer.apply(true));
        invalidateExchequer = new Button(globalConfiguration.getMessage("interphase.exchequer.invalidate"));
        invalidateExchequer.setOnAction(endExchequer.apply(false));
        hBox = new HBox();
        hBox.getChildren().addAll(validateExchequer, invalidateExchequer);
        vBox.getChildren().add(hBox);

        updateExchequerButtons();

        return tab;
    }

    /**
     * Update all the fields based on the current economical sheet.
     */
    private void updateEcoSheet() {
        exchequerRepartition.setText(globalConfiguration.getMessage("interphase.info.exchequer.prestige_spent"));
        improveStability.setText(globalConfiguration.getMessage("interphase.stab.improve"));
        stabInfo.setText(null);
        EconomicalSheet sheet = game.getCountries().stream()
                .filter(country -> StringUtils.equals(country.getName(), gameConfig.getCountryName()))
                .flatMap(country -> country.getEconomicalSheets().stream())
                .filter(es -> Objects.equals(es.getTurn(), game.getTurn()))
                .findAny()
                .orElse(null);
        if (sheet != null) {
            Label label = new Label(globalConfiguration.getMessage("interphase.exchequer.info",
                    sheet.getRtBefExch(), sheet.getGrossIncome(),
                    sheet.getRegularIncome(), sheet.getPrestigeIncome(), sheet.getMaxNatLoan(),
                    sheet.getExpenses(), sheet.getRemainingExpenses()));
            Node tooltip = createExchequerTooltip(sheet);
            exchequerInfo.getChildren().clear();
            exchequerInfo.getChildren().addAll(label, tooltip);
            if (sheet.getRemainingExpenses() != null && sheet.getPrestigeIncome() != null) {
                exchequerRepartition.setText(globalConfiguration.getMessage("interphase.info.exchequer.prestige_spent",
                        Math.max(Math.min(sheet.getRemainingExpenses(), sheet.getPrestigeIncome()), 0)));
            }
            prestigeField.setText(sheet.getPrestigeSpent() + "");
            improveStability.setText(globalConfiguration.getMessage("interphase.stab.improve", sheet.getStabModifier()));
            if (sheet.getStabModifier() != null) {
                stabInfo.setText(getImproveStabilityInfo("interphase.stab.info", sheet));
            } else {
                sheet = game.getCountries().stream()
                        .filter(country -> StringUtils.equals(country.getName(), gameConfig.getCountryName()))
                        .flatMap(country -> country.getEconomicalSheets().stream())
                        .filter(es -> Objects.equals(es.getTurn(), game.getTurn() - 1))
                        .findAny()
                        .orElse(null);
                stabInfo.setText(getImproveStabilityInfo("interphase.stab.info_prev", sheet));
            }
        }
    }

    /**
     * @param code the code of the message to display.
     * @param sheet the economical sheet.
     * @return the label to display in the improve stability info node.
     */
    private String getImproveStabilityInfo(String code, EconomicalSheet sheet) {
        String text;
        if (sheet == null || sheet.getStabDie() == null || sheet.getStabModifier() == null) {
            text = globalConfiguration.getMessage(code + "_none");
        } else {
            InvestmentEnum invest = GameUtil.reverseInvestment(sheet.getStab());
            int modifier = sheet.getStabModifier();
            if (invest == InvestmentEnum.L) {
                modifier += 5;
            } else if (invest == InvestmentEnum.M) {
                modifier += 2;
            }
            text = globalConfiguration.getMessage(code, sheet.getStabDie(), modifier,
                    GameUtil.improveStability(sheet.getStabDie() + modifier));
        }
        return text;
    }

    /**
     * @param sheet the economical sheet.
     * @return The help tooltip about the exchequer test.
     */
    private Node createExchequerTooltip(EconomicalSheet sheet) {
        ResultEnum exchequerResult = globalConfiguration.getTables().getResults().stream()
                .filter(result -> Objects.equals(result.getColumn(), sheet.getExchequerColumn()) &&
                        Objects.equals(result.getDie(), sheet.getExchequerDie()))
                .map(Result::getResult)
                .findAny()
                .orElse(null);
        try {
            ImageView img = new ImageView(new Image(new FileInputStream(new File("data/img/help.png"))));
            Tooltip tooltip = new Tooltip(globalConfiguration.getMessage("interphase.exchequer.help",
                    sheet.getExchequerDie(), sheet.getExchequerBonus(), sheet.getExchequerColumn(), exchequerResult));
            Tooltip.install(img, tooltip);
            UIUtil.patchTooltipUntilMigrationJava9(tooltip);
            return img;
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot find help icon.");
            return null;
        }
    }

    /**
     * Update the exchequer tab - forms.
     */
    private void updateExchequerButtons() {
        PlayableCountry country = game.getCountries().stream()
                .filter(c -> Objects.equals(c.getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        exchequerRepartition.setDisable(true);
        validateExchequer.setDisable(true);
        invalidateExchequer.setDisable(true);

        if (country != null) {
            if (game.getStatus() == GameStatusEnum.EXCHEQUER) {
                exchequerRepartition.setDisable(country.isReady());
                validateExchequer.setDisable(country.isReady());
                invalidateExchequer.setDisable(!country.isReady());
            }
        }
    }

    /**
     * @return the stability tab.
     */
    private Tab createStabilityTab() {
        Tab tab = new Tab(globalConfiguration.getMessage("interphase.info.stab"));
        tab.setClosable(false);
        VBox vBox = new VBox();

        ChoiceBox<InvestmentEnum> investChoice = new ChoiceBox<>();
        investChoice.converterProperty().set(new EnumConverter<>(globalConfiguration));
        investChoice.setItems(FXCollections.observableArrayList(InvestmentEnum.values()));
        investChoice.getItems().add(0, null);
        improveStability = new Button(globalConfiguration.getMessage("interphase.stab.improve"));
        improveStability.setOnAction(callServiceAsEvent(interPhaseService::improveStability, () -> new ImproveStabilityRequest(investChoice.getValue()), "Error when improving stability."));

        updateStability();

        HBox hBox = new HBox();
        hBox.getChildren().addAll(investChoice, improveStability);
        vBox.getChildren().add(hBox);

        stabInfo = new Label();
        vBox.getChildren().add(stabInfo);

        tab.setContent(vBox);
        return tab;
    }

    /**
     * Update the stability panel buttons.
     */
    private void updateStability() {
        PlayableCountry country = game.getCountries().stream()
                .filter(c -> Objects.equals(c.getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        improveStability.setDisable(true);

        if (country != null) {
            if (game.getStatus() == GameStatusEnum.STABILITY) {
                improveStability.setDisable(country.isReady());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case GAME:
            case STATUS:
            case TURN_ORDER:
                updateRedeploymentPanel();
                updateExchequerButtons();
                updateStability();
                break;
            case REDEPLOY:
                checkNotification(diff);
                break;
            case ECO_SHEET:
                updateEcoSheet();
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
