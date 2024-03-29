package com.mkl.eu.front.client.window;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.board.MoveLeaderRequest;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.military.*;
import com.mkl.eu.client.service.vo.tables.AssaultResult;
import com.mkl.eu.client.service.vo.tables.CombatResult;
import com.mkl.eu.client.service.vo.tables.Leader;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.front.client.common.*;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.marker.BorderMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Window containing the military (battles, sieges,...).
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class MilitaryWindow extends AbstractDiffResponseListenerContainer implements IDiffListener {
    /** Board service. */
    @Autowired
    private IBoardService boardService;
    /** Battle service. */
    @Autowired
    private IBattleService battleService;
    /** Siege service. */
    @Autowired
    private ISiegeService siegeService;
    /** Game. */
    private Game game;
    /** Markers of the loaded game. */
    private List<IMapMarker> markers;
    /** Global node. */
    private TabPane tabPane;

    /********************************************/
    /**         Nodes about military            */
    /********************************************/
    /** The choose leader for deploy leader action. */
    private ComboBox<Counter> leaderCountersChoice;
    /** The choiceBox for stacks that contains at least an army counter of the country for deploy leader action. */
    private ComboBox<Stack> leaderStackChoice;
    /** The deploy leader button. */
    private Button deployLeaderButton;
    /** The validate military phase button. */
    private Button validateMilitaryPhase;
    /** The invalidate military phase button. */
    private Button invalidateMilitaryPhase;
    /** The selected battle. */
    private ChoiceBox<Battle> choiceBattle;
    /** The choose battle button. */
    private Button chooseBattle;
    /** The selected siege. */
    private ChoiceBox<Siege> choiceSiege;
    /** The choose siege button. */
    private Button chooseSiege;

    /********************************************/
    /**         Nodes about battles             */
    /********************************************/
    /** The selected turn. */
    private ChoiceBox<Integer> choiceBattleTurn;

    /********************************************/
    /**         Nodes about sieges             */
    /********************************************/
    /** The selected turn. */
    private ChoiceBox<Integer> choiceSiegeTurn;


    /**
     * Constructor.
     *
     * @param game       the game to set.
     * @param gameConfig the gameConfig to set.
     */
    public MilitaryWindow(Game game, List<IMapMarker> markers, GameConfiguration gameConfig) {
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
        PlayableCountry country = game.getCountries().stream()
                .filter(playableCountry -> Objects.equals(playableCountry.getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        tabPane.getTabs().add(createInfoTab(country));
        tabPane.getTabs().add(createBattles());
        tabPane.getTabs().add(createSieges());

        updateBattles();
        updateSieges();
        Integer maxTurn = choiceBattleTurn.getItems().stream()
                .filter(Objects::nonNull)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(null);
        choiceBattleTurn.getSelectionModel().select(maxTurn);
        maxTurn = choiceSiegeTurn.getItems().stream()
                .filter(Objects::nonNull)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(null);
        choiceSiegeTurn.getSelectionModel().select(maxTurn);
    }

    /**
     * @param country of the current player.
     * @return the info tab.
     */
    private Tab createInfoTab(PlayableCountry country) {
        Tab tab = new Tab(GlobalConfiguration.getMessage("military.info.title"));
        tab.setClosable(false);
        VBox vBox = new VBox();
        tab.setContent(vBox);

        HBox hBox = new HBox();

        leaderCountersChoice = new ComboBox<>();
        leaderCountersChoice.setCellFactory(new CounterInProvinceCellFactory());
        leaderCountersChoice.converterProperty().set(new CounterInProvinceConverter());

        leaderStackChoice = new ComboBox<>();
        leaderStackChoice.setCellFactory(new StackInProvinceCellFactory());
        leaderStackChoice.converterProperty().set(new StackInProvinceConverter());

        ChoiceBox<IMapMarker> provincesChoice = new ChoiceBox<>();
        provincesChoice.converterProperty().set(new StringConverter<IMapMarker>() {
            /** {@inheritDoc} */
            @Override
            public String toString(IMapMarker object) {
                return object == null ? "" : GlobalConfiguration.getMessage(object.getId());
            }

            /** {@inheritDoc} */
            @Override
            public IMapMarker fromString(String string) {
                return null;
            }
        });
        provincesChoice.setItems(FXCollections.observableArrayList(markers.stream()
                .filter(marker -> StringUtils.equals(country.getName(), marker.getController())).collect(Collectors.toList())));
        provincesChoice.getItems().add(0, null);

        deployLeaderButton = new Button(GlobalConfiguration.getMessage("move"));

        leaderStackChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            provincesChoice.setDisable(newValue != null);
            deployLeaderButton.setDisable(newValue == null && provincesChoice.getSelectionModel().getSelectedItem() == null);
        });
        provincesChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            deployLeaderButton.setDisable(newValue == null && provincesChoice.getSelectionModel().getSelectedItem() == null);
        });

        deployLeaderButton.setOnAction(event -> {
            Counter counter = leaderCountersChoice.getSelectionModel().getSelectedItem();
            Stack stack = leaderStackChoice.getSelectionModel().getSelectedItem();
            IMapMarker province = provincesChoice.getSelectionModel().getSelectedItem();

            MoveLeaderRequest request;
            if (stack != null) {
                request = new MoveLeaderRequest(counter.getId(), stack.getId());
            } else {
                request = new MoveLeaderRequest(counter.getId(), province.getId());
            }
            callService(boardService::moveLeader, () -> request, "Error when moving leader.");
        });

        Button adminDeployLeaders = new Button(GlobalConfiguration.getMessage("military.info.init_leaders"));
        adminDeployLeaders.setOnAction(callServiceAsEvent(boardService::initLeaders, () -> null, "Error when initializing leaders."));

        hBox.getChildren().addAll(leaderCountersChoice, leaderStackChoice, provincesChoice, deployLeaderButton, adminDeployLeaders);
        vBox.getChildren().add(hBox);

        Function<Boolean, EventHandler<ActionEvent>> endMilitaryPhase = validate -> callServiceAsEvent(boardService::validateMilitaryRound, () -> new ValidateRequest(validate), "Error when validating the military round.");

        validateMilitaryPhase = new Button(GlobalConfiguration.getMessage("military.info.validate"));
        validateMilitaryPhase.setOnAction(endMilitaryPhase.apply(true));
        invalidateMilitaryPhase = new Button(GlobalConfiguration.getMessage("military.info.invalidate"));
        invalidateMilitaryPhase.setOnAction(endMilitaryPhase.apply(false));
        hBox = new HBox();
        hBox.getChildren().addAll(validateMilitaryPhase, invalidateMilitaryPhase);
        vBox.getChildren().add(hBox);

        choiceBattle = new ChoiceBox<>();
        chooseBattle = new Button(GlobalConfiguration.getMessage("military.info.choose_battle"));
        choiceBattle.converterProperty().set(new StringConverter<Battle>() {
            /** {@inheritDoc} */
            @Override
            public String toString(Battle object) {
                return GlobalConfiguration.getMessage(object.getProvince());
            }

            /** {@inheritDoc} */
            @Override
            public Battle fromString(String string) {
                return null;
            }
        });
        choiceBattle.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> chooseBattle.setDisable(newValue == null));
        chooseBattle.setOnAction(callServiceAsEvent(battleService::chooseBattle, () -> new ChooseProvinceRequest(choiceBattle.getSelectionModel().getSelectedItem().getProvince()),
                "Error when choosing the battle to proceed.", () -> tabPane.getSelectionModel().select(1), null));
        hBox = new HBox();
        hBox.getChildren().addAll(choiceBattle, chooseBattle);
        vBox.getChildren().add(hBox);

        choiceSiege = new ChoiceBox<>();
        chooseSiege = new Button(GlobalConfiguration.getMessage("military.info.choose_siege"));
        choiceSiege.converterProperty().set(new StringConverter<Siege>() {
            /** {@inheritDoc} */
            @Override
            public String toString(Siege object) {
                return GlobalConfiguration.getMessage(object.getProvince());
            }

            /** {@inheritDoc} */
            @Override
            public Siege fromString(String string) {
                return null;
            }
        });
        choiceSiege.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> chooseSiege.setDisable(newValue == null));
        chooseSiege.setOnAction(callServiceAsEvent(siegeService::chooseSiege, () -> new ChooseProvinceRequest(choiceSiege.getSelectionModel().getSelectedItem().getProvince()),
                "Error when choosing the siege to proceed.", () -> tabPane.getSelectionModel().select(2), null));
        hBox = new HBox();
        hBox.getChildren().addAll(choiceSiege, chooseSiege);
        vBox.getChildren().add(hBox);

        updateInfoPanel(country);

        return tab;
    }

    /**
     * Updates the info tab.
     *
     * @param country of the current player.
     */
    private void updateInfoPanel(PlayableCountry country) {
        List<Counter> counters = game.getStacks().stream().flatMap(stack -> stack.getCounters().stream()
                .filter(counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                        CounterUtil.isLeader(counter.getType())))
                .sorted((c1, c2) -> c1.getCode().compareTo(c2.getCode()))
                .collect(Collectors.toList());

        ObservableList<Counter> counterList = FXCollections.observableArrayList(counters);
        leaderCountersChoice.setItems(counterList);
        leaderCountersChoice.getItems().add(0, null);

        leaderStackChoice.setItems(FXCollections.observableList(game.getStacks().stream()
                .filter(stack -> stack.getCounters().stream().anyMatch(
                        counter -> StringUtils.equals(counter.getCountry(), country.getName()) &&
                                CounterUtil.isArmy(counter.getType())))
                .sorted((o1, o2) -> o1.getProvince().compareTo(o2.getProvince()))
                .collect(Collectors.toList())));
        leaderStackChoice.getItems().add(0, null);

        CountryOrder countryOrder = game.getOrders().stream()
                .filter(order -> Objects.equals(order.getCountry().getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        deployLeaderButton.setDisable(true);
        validateMilitaryPhase.setDisable(true);
        invalidateMilitaryPhase.setDisable(true);
        chooseBattle.setDisable(true);
        chooseSiege.setDisable(true);

        if (game.getStatus() == GameStatusEnum.MILITARY_HIERARCHY) {
            deployLeaderButton.setDisable(false);
        }
        if (countryOrder != null && countryOrder.isActive()) {
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE) {
                validateMilitaryPhase.setDisable(countryOrder.isReady());
                invalidateMilitaryPhase.setDisable(!countryOrder.isReady());
            } else if (game.getStatus() == GameStatusEnum.MILITARY_BATTLES &&
                    game.getBattles().stream().noneMatch(battle -> battle.getStatus() != BattleStatusEnum.NEW && battle.getStatus() != BattleStatusEnum.DONE)) {
                chooseBattle.setDisable(false);
            } else if (game.getStatus() == GameStatusEnum.MILITARY_SIEGES &&
                    game.getSieges().stream().noneMatch(siege -> siege.getStatus() != SiegeStatusEnum.NEW && siege.getStatus() != SiegeStatusEnum.DONE)) {
                chooseSiege.setDisable(false);
            }
        }
    }

    /**
     * Creates the tab for the battles.
     *
     * @return the tab for the battles.
     */
    private Tab createBattles() {
        Tab tab = new Tab(GlobalConfiguration.getMessage("military.battle.title"));
        tab.setClosable(false);

        choiceBattleTurn = new ChoiceBox<>();


        VBox vBox = new VBox();
        vBox.getChildren().addAll(choiceBattleTurn);

        ScrollPane scroll = new ScrollPane();
        scroll.fitToWidthProperty().set(true);
        scroll.setContent(vBox);
        tab.setContent(scroll);

        choiceBattleTurn.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                vBox.getChildren().removeIf(node -> node != choiceBattleTurn);
            } else {
                vBox.getChildren().removeIf(node -> node != choiceBattleTurn);
                Map<WarLight, List<Battle>> battlesByWar = game.getBattles().stream()
                        .filter(battle -> battle.getTurn().equals(newValue))
                        .collect(Collectors.groupingBy(Battle::getWar));
                for (Map.Entry<WarLight, List<Battle>> entry : battlesByWar.entrySet()) {
                    vBox.getChildren().add(addBattlesFromWar(entry.getKey(), entry.getValue()));
                }
            }
        });

        return tab;
    }

    /**
     * @param war     the war.
     * @param battles the battles of the war.
     * @return a node representing all the battles of a war in a given turn.
     */
    private Node addBattlesFromWar(WarLight war, List<Battle> battles) {
        VBox vBox = new VBox();
        Collections.sort(battles, (o1, o2) -> o2.getId().compareTo(o1.getId()));
        for (Battle battle : battles) {
            SplitPane splitBattle = new SplitPane();
            VBox phasingNode = new VBox();
            VBox nonPhasingNode = new VBox();
            splitBattle.getItems().addAll(phasingNode, nonPhasingNode);

            HBox phasingCounters = new HBox();
            HBox nonPhasingCounters = new HBox();
            battle.getCounters().stream()
                    .forEach(counter -> {
                        ImageView image = UIUtil.getImage(counter.getCounter());
                        if (counter.isPhasing()) {
                            phasingCounters.getChildren().add(image);
                        } else {
                            nonPhasingCounters.getChildren().add(image);
                        }
                    });
            phasingCounters.getChildren().add(createBattleTooltip(battle.getPhasing()));
            nonPhasingCounters.getChildren().add(createBattleTooltip(battle.getNonPhasing()));
            try {
                ImageView img = new ImageView(new Image(new FileInputStream(new File("data/img/victory.png"))));
                if (battle.getWinner() == BattleWinnerEnum.NON_PHASING) {
                    nonPhasingCounters.getChildren().add(img);
                } else if (battle.getWinner() == BattleWinnerEnum.PHASING) {
                    phasingCounters.getChildren().add(img);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Cannot find victory icon.");
            }


            phasingNode.getChildren().addAll(phasingCounters, createBattleModifiers(battle.getPhasing()),
                    createBattleLosses(battle.getPhasing(), battle.getNonPhasing()));
            nonPhasingNode.getChildren().addAll(nonPhasingCounters, createBattleModifiers(battle.getNonPhasing()),
                    createBattleLosses(battle.getNonPhasing(), battle.getPhasing()));


            if (battle.getStatus() == BattleStatusEnum.SELECT_FORCES) {
                if (BooleanUtils.isNotTrue(battle.getPhasing().isForces())) {
                    phasingNode.getChildren().add(createBattleSelectForces(battle, true));
                }
                if (BooleanUtils.isNotTrue(battle.getNonPhasing().isForces())) {
                    nonPhasingNode.getChildren().add(createBattleSelectForces(battle, false));
                }
            } else if (battle.getStatus() == BattleStatusEnum.WITHDRAW_BEFORE_BATTLE) {
                nonPhasingNode.getChildren().add(createBattleWithdraw(battle.getProvince()));
            } else if (battle.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_ATT) {
                phasingNode.getChildren().add(createBattleRetreatFirstDay());
            } else if (battle.getStatus() == BattleStatusEnum.RETREAT_AFTER_FIRST_DAY_DEF) {
                nonPhasingNode.getChildren().add(createBattleRetreatFirstDay());
            } else if (battle.getStatus() == BattleStatusEnum.CHOOSE_LOSS) {
                if (BooleanUtils.isNotTrue(battle.getPhasing().isLossesSelected())) {
                    phasingNode.getChildren().add(createBattleChooseLosses(battle, true));
                }
                if (BooleanUtils.isNotTrue(battle.getNonPhasing().isLossesSelected())) {
                    nonPhasingNode.getChildren().add(createBattleChooseLosses(battle, false));
                }
            } else if (battle.getStatus() == BattleStatusEnum.RETREAT) {
                if (BooleanUtils.isNotTrue(battle.getPhasing().isRetreatSelected())) {
                    phasingNode.getChildren().add(createBattleRetreat(battle, true));
                }
                if (BooleanUtils.isNotTrue(battle.getNonPhasing().isRetreatSelected())) {
                    nonPhasingNode.getChildren().add(createBattleRetreat(battle, false));
                }
            }

            TitledPane battleNode = new TitledPane(GlobalConfiguration.getMessage(battle.getProvince()) + " - " +
                    GlobalConfiguration.getMessage(battle.getStatus()), splitBattle);
            vBox.getChildren().add(battleNode);
        }

        return new TitledPane(war.getName(), vBox);
    }

    /**
     * @param side of the battle.
     * @return The help tooltip about the battle.
     */
    private Node createBattleTooltip(BattleSide side) {
        try {
            ImageView img = new ImageView(new Image(new FileInputStream(new File("data/img/help.png"))));
            Tooltip tooltip = new Tooltip(GlobalConfiguration.getMessage("military.battle.info",
                    side.getTech(), side.getFireColumn(), side.getShockColumn(), side.getMoral(), side.getSize(), side.getSizeDiff(), side.getPursuitMod()));
            Tooltip.install(img, tooltip);
            UIUtil.patchTooltipUntilMigrationJava9(tooltip);
            return img;
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot find help icon.");
            return null;
        }
    }

    /**
     * @param side of the battle.
     * @return the node displaying the modifiers of a battle side.
     */
    private Node createBattleModifiers(BattleSide side) {
        HBox hBox = new HBox();
        Label modifiers = new Label(side.getFirstDay().getFireMod() + " / " + side.getFirstDay().getShockMod() + "   "
                + side.getSecondDay().getFireMod() + " / " + side.getSecondDay().getShockMod());
        hBox.getChildren().add(modifiers);
        if (StringUtils.isNotEmpty(side.getLeader())) {
            Label leaderLabel = new Label("   " + GlobalConfiguration.getMessage("military.battle.leader", GlobalConfiguration.getMessage(side.getLeader())));
            hBox.getChildren().add(leaderLabel);
            Leader leader = GlobalConfiguration.getTables().getLeader(side.getLeader(), side.getCountry());
            if (leader != null) {
                try {
                    ImageView leaderTooltip = new ImageView(new Image(new FileInputStream(new File("data/img/help.png"))));
                    Tooltip tooltip = new Tooltip(GlobalConfiguration.getMessage("military.battle.leader_help",
                            leader.getManoeuvre(), leader.getFire(), leader.getShock(), leader.getSiege()));
                    Tooltip.install(leaderTooltip, tooltip);
                    UIUtil.patchTooltipUntilMigrationJava9(tooltip);
                    hBox.getChildren().add(leaderTooltip);
                } catch (FileNotFoundException e) {
                    LOGGER.error("Cannot find help icon.");
                }
            }
            if (side.getLeaderWounds() != null) {
                if (side.getLeaderWounds() == -1) {
                    hBox.getChildren().add(new Label(GlobalConfiguration.getMessage("military.battle.leader.dead")));
                } else {
                    hBox.getChildren().add(new Label(GlobalConfiguration.getMessage("military.battle.leader.wounded", side.getLeaderWounds())));
                }
            }
        }
        return hBox;
    }

    /**
     * @param side    of the battle.
     * @param against side against of the battle.
     * @return the node displaying the damages of a battle side.
     */
    private Label createBattleLosses(BattleSide side, BattleSide against) {
        Label label = new Label(getDamage(against.getLosses()));

        StringBuilder sb = new StringBuilder();
        sb.append(getSequenceDamage("military.battle.first_fire", side.getFireColumn(), side.getFirstDay().getFire(), side.getFirstDay().getFireMod(), side.getTech()));
        sb.append("\n");
        sb.append(getSequenceDamage("military.battle.first_shock", side.getShockColumn(), side.getFirstDay().getShock(), side.getFirstDay().getShockMod(), Tech.LACE_WAR));
        sb.append("\n");
        sb.append(getSequenceDamage("military.battle.second_fire", side.getFireColumn(), side.getSecondDay().getFire(), side.getSecondDay().getFireMod(), side.getTech()));
        sb.append("\n");
        sb.append(getSequenceDamage("military.battle.second_shock", side.getShockColumn(), side.getSecondDay().getShock(), side.getSecondDay().getShockMod(), Tech.LACE_WAR));
        sb.append("\n");
        sb.append(getSequenceDamage("military.battle.pursuit", CombatResult.COLUMN_E, side.getPursuit(), side.getPursuitMod(), Tech.LACE_WAR));
        sb.append("\n");
        sb.append(getRetreatDamage(against.getRetreat()));
        Tooltip tooltip = new Tooltip(sb.toString());
        UIUtil.patchTooltipUntilMigrationJava9(tooltip);
        label.setTooltip(tooltip);

        return label;
    }

    /**
     * @param key      of the sequence of the damage.
     * @param column   the column of the damage.
     * @param die      the unmodified roll die of the damage.
     * @param modifier the bonus on the roll die of the damage.
     * @param tech     the tech of the damage.
     * @return a text displaying all damage info for a given sequence.
     */
    private String getSequenceDamage(String key, String column, Integer die, int modifier, String tech) {
        StringBuilder sb = new StringBuilder();
        sb.append(GlobalConfiguration.getMessage(key));
        sb.append(" : ");
        if (die != null) {
            int min = GlobalConfiguration.getTables().getCombatResults().stream()
                    .filter(result -> StringUtils.equals(column, result.getColumn()))
                    .map(CombatResult::getDice)
                    .min(Comparator.naturalOrder())
                    .orElse(3);
            int max = GlobalConfiguration.getTables().getCombatResults().stream()
                    .filter(result -> StringUtils.equals(column, result.getColumn()))
                    .map(CombatResult::getDice)
                    .max(Comparator.naturalOrder())
                    .orElse(12);
            int modifiedDie = die + modifier < min ? min : die + modifier > max ? max : die + modifier;
            CombatResult result = GlobalConfiguration.getTables().getCombatResults().stream()
                    .filter(cr -> StringUtils.equals(cr.getColumn(), column) && cr.getDice() == modifiedDie)
                    .findAny()
                    .orElse(null);
            String localDamage = getDamage(result.adjustToTech(tech));
            sb.append(GlobalConfiguration.getMessage("military.battle.damage", die, modifier, localDamage));
        } else {
            sb.append(GlobalConfiguration.getMessage("military.battle.no_damage"));
        }
        return sb.toString();
    }

    /**
     * @param die the unmodified die roll of the retreat.
     * @return a text displaying all damage info for the retreat.
     */
    private String getRetreatDamage(Integer die) {
        StringBuilder sb = new StringBuilder();
        sb.append(GlobalConfiguration.getMessage("military.battle.retreat"));
        sb.append(" : ");
        if (die != null) {
            // cap die to [1-8)
            int modifiedDie = Math.max(1, Math.min(8, die));
            String localDamage = getDamage(AbstractWithLoss.create((modifiedDie - 1) / 2));
            sb.append(GlobalConfiguration.getMessage("military.battle.damage", die, 0, localDamage));
        } else {
            sb.append(GlobalConfiguration.getMessage("military.battle.no_damage"));
        }
        return sb.toString();

    }

    /**
     * @param losses the loss to display.
     * @return a text displaying a damage or a loss.
     */
    private String getDamage(AbstractWithLoss losses) {
        StringBuilder sb = new StringBuilder();
        if (losses.getRoundLoss() != null && losses.getRoundLoss() > 0) {
            sb.append(losses.getRoundLoss());
        }
        if (losses.getThirdLoss() != null) {
            for (int i = 0; i < losses.getThirdLoss(); i++) {
                sb.append("°");
            }
        }
        if (losses.getMoraleLoss() != null) {
            for (int i = 0; i < losses.getMoraleLoss(); i++) {
                sb.append("*");
            }
        }
        if (sb.length() == 0) {
            sb.append(" - ");
        }
        return sb.toString();
    }

    /**
     * @param battle  the battle.
     * @param phasing player.
     * @return the node for selecting forces at that start of the battle.
     */
    private Node createBattleSelectForces(Battle battle, boolean phasing) {
        HBox hBox = new HBox();
        List<Long> selectedCounters = new ArrayList<>();

        Node counters = createMultiSelectCounterNode(battle, phasing, "military.battle.counters", selectedCounters);
        Button withdraw = new Button(GlobalConfiguration.getMessage("military.battle.select"));
        withdraw.setOnAction(callServiceAsEvent(battleService::selectForces, () -> new SelectForcesRequest(selectedCounters), "Error when selecting forces at the start of the battle."));

        hBox.getChildren().addAll(counters, withdraw);

        return hBox;
    }

    /**
     * @param battle           the battle.
     * @param phasing          the side.
     * @param selectedCounters the list of selected counters in the checkboxes (sort of callback).
     * @return a multi check box node that contains all the counters of a side of a battle.
     */
    private Node createMultiSelectCounterNode(Battle battle, boolean phasing, String key, List<Long> selectedCounters) {
        War war = game.getWars().stream()
                .filter(w -> Objects.equals(w.getId(), battle.getWar().getId()))
                .findAny()
                .orElse(null);
        boolean offensive = !battle.isPhasingOffensive() ^ phasing;
        List<String> allies = war.getCountries().stream()
                .filter(c -> c.isOffensive() == offensive)
                .map(c -> c.getCountry().getName())
                .collect(Collectors.toList());
        return createMultiSelectCounterNode(allies, battle.getProvince(), key, selectedCounters);
    }

    /**
     * @param allies           the white list of countries.
     * @param province         the province.
     * @param selectedCounters the list of selected counters in the checkboxes (sort of callback).
     * @return a multi check box node that contains all the counters that matches the country and the province.
     */
    private Node createMultiSelectCounterNode(List<String> allies, String province, String key, List<Long> selectedCounters) {
        List<Counter> counterList = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), province))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> allies.contains(counter.getCountry()) && CounterUtil.isMobile(counter.getType()))
                .collect(Collectors.toList());

        MenuButton counters = new MenuButton(GlobalConfiguration.getMessage(key));
        for (Counter counter : counterList) {
            CheckBox check = new CheckBox();
            ImageView image = UIUtil.getImage(counter);
            check.setGraphic(image);
            if (image == null) {
                LOGGER.error("Cannot load counter image for " + counter.getCountry() + " - " + counter.getType());
                check.setText(counter.getCountry() + " - " + counter.getType());
            }
            check.setOnAction(event -> {
                CheckBox source = (CheckBox) event.getSource();
                if (source.isSelected()) {
                    selectedCounters.add(counter.getId());
                } else {
                    selectedCounters.remove(counter.getId());
                }
            });
            CustomMenuItem item = new CustomMenuItem(check);
            item.setHideOnClick(false);
            counters.getItems().add(item);
        }
        return counters;
    }

    /**
     * @param battleProvince where the battle occurs.
     * @return the node for withdrawing before the battle.
     */
    private Node createBattleWithdraw(String battleProvince) {
        HBox hBox = new HBox();

        ChoiceBox<String> provinces = new ChoiceBox<>();
        IMapMarker marker = markers.stream()
                .filter(m -> StringUtils.equals(m.getId(), battleProvince))
                .findAny()
                .orElse(null);
        provinces.setItems(FXCollections.observableList(marker.getNeighbours().stream()
                .map(BorderMarker::getProvince)
                .map(IMapMarker::getId)
                .collect(Collectors.toList())));
        provinces.getItems().add(0, null);
        Button withdraw = new Button(GlobalConfiguration.getMessage("military.battle.withdraw"));
        withdraw.setOnAction(callServiceAsEvent(battleService::withdrawBeforeBattle, () -> {
            String province = provinces.getSelectionModel().getSelectedItem();
            return new WithdrawBeforeBattleRequest(StringUtils.isNotEmpty(province), province);
        }, "Error when withdrawing from the current battle."));

        hBox.getChildren().addAll(provinces, withdraw);

        return hBox;
    }

    /**
     * @return the node for retreating after the first day of a battle.
     */
    private Node createBattleRetreatFirstDay() {
        HBox hBox = new HBox();

        ChoiceBox<Boolean> choices = new ChoiceBox<>();

        choices.setItems(FXCollections.observableArrayList(false, true));
        choices.setConverter(new StringConverter<Boolean>() {
            @Override
            public String toString(Boolean object) {
                if (object != null && object) {
                    return GlobalConfiguration.getMessage("military.battle.retreat");
                }
                return GlobalConfiguration.getMessage("military.battle.stay");
            }

            @Override
            public Boolean fromString(String string) {
                return null;
            }
        });
        Button retreat = new Button(GlobalConfiguration.getMessage("military.battle.retreat_first_day"));
        retreat.setOnAction(callServiceAsEvent(battleService::retreatFirstDay, () -> {
            Boolean choice = choices.getSelectionModel().getSelectedItem();
            return new ValidateRequest(choice);
        }, "Error when withdrawing from the current battle."));

        hBox.getChildren().addAll(choices, retreat);

        return hBox;
    }

    /**
     * @param battle the battle.
     * @return the node for retreating at the end of a battle.
     */
    private Node createBattleRetreat(Battle battle, boolean phasing) {
        HBox hBox = new HBox();

        ChoiceBox<String> provinces = new ChoiceBox<>();
        IMapMarker marker = markers.stream()
                .filter(m -> StringUtils.equals(m.getId(), battle.getProvince()))
                .findAny()
                .orElse(null);
        List<String> neighbors = marker.getNeighbours().stream()
                .map(BorderMarker::getProvince)
                .map(IMapMarker::getId)
                .collect(Collectors.toList());
        neighbors.add(0, "disband");
        provinces.setItems(FXCollections.observableList(neighbors));
        List<Long> selectedCounters = new ArrayList<>();

        Node counters = createMultiSelectCounterNode(battle, phasing, "military.battle.retreat_in_fortress", selectedCounters);


        Button withdraw = new Button(GlobalConfiguration.getMessage("military.battle.retreat"));
        withdraw.setOnAction(callServiceAsEvent(battleService::retreatAfterBattle, () -> {
            String province = provinces.getSelectionModel().getSelectedItem();
            boolean disband = StringUtils.equals("disband", province);
            return new RetreatAfterBattleRequest(selectedCounters, disband ? null : province, disband);
        }, "Error when retreating at the end of the battle."));

        hBox.getChildren().addAll(counters, provinces, withdraw);

        return hBox;
    }

    /**
     * @param battle  the battle.
     * @param phasing player.
     * @return the node for choosing losses at the end of a battle.
     */
    private Node createBattleChooseLosses(Battle battle, boolean phasing) {
        List<Counter> counterList = battle.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing)
                .map(BattleCounter::getCounter)
                .collect(Collectors.toList());
        int maxRound = CommonUtil.add(phasing ? battle.getPhasing().getLosses().getRoundLoss() : battle.getNonPhasing().getLosses().getRoundLoss(), 1);
        return createChooseLosses(counterList, maxRound, battleService::chooseLossesFromBattle);
    }

    /**
     * @param counterList the list of counters elligible for losses.
     * @param maxRound    the max number of round losses.
     * @param service     the service to call to choose losses.
     * @return the node for choosing losses at the end of a battle.
     */
    private Node createChooseLosses(List<Counter> counterList, int maxRound, IService<ChooseLossesRequest> service) {
        List<Integer> rounds = Stream.iterate(0, t -> t + 1).limit(maxRound).collect(Collectors.toList());
        List<Integer> thirds = Stream.iterate(0, t -> t + 1).limit(3).collect(Collectors.toList());
        List<ChooseLossLine> lines = new ArrayList<>();

        VBox vBox = new VBox();
        ChooseLossLine line = new ChooseLossLine(counterList, rounds, thirds, (newLine, add) -> {
            if (add) {
                lines.add(newLine);
                vBox.getChildren().add(newLine.node);
            } else {
                vBox.getChildren().remove(newLine.node);
                lines.remove(newLine);
            }
        });
        Button chooseLoss = new Button(GlobalConfiguration.getMessage("military.battle.choose_losses"));
        chooseLoss.setOnAction(callServiceAsEvent(service, () -> line.toRequest(lines), "Error when choosing losses."));

        lines.get(0).node.getChildren().add(chooseLoss);

        return vBox;
    }

    /**
     * Creates the tab for the sieges.
     *
     * @return the tab for the sieges.
     */
    private Tab createSieges() {
        Tab tab = new Tab(GlobalConfiguration.getMessage("military.siege.title"));
        tab.setClosable(false);

        choiceSiegeTurn = new ChoiceBox<>();


        VBox vBox = new VBox();
        vBox.getChildren().addAll(choiceSiegeTurn);

        ScrollPane scroll = new ScrollPane();
        scroll.fitToWidthProperty().set(true);
        scroll.setContent(vBox);
        tab.setContent(scroll);

        choiceSiegeTurn.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                vBox.getChildren().removeIf(node -> node != choiceSiegeTurn);
            } else {
                vBox.getChildren().removeIf(node -> node != choiceSiegeTurn);
                Map<WarLight, List<Siege>> siegesByWar = game.getSieges().stream()
                        .filter(siege -> siege.getTurn().equals(newValue))
                        .collect(Collectors.groupingBy(Siege::getWar));
                for (Map.Entry<WarLight, List<Siege>> entry : siegesByWar.entrySet()) {
                    vBox.getChildren().add(addSiegesFromWar(entry.getKey(), entry.getValue()));
                }
            }
        });

        return tab;
    }

    /**
     * @param war    the war.
     * @param sieges the sieges of the war.
     * @return a node representing all the battles of a war in a given turn.
     */
    private Node addSiegesFromWar(WarLight war, List<Siege> sieges) {
        VBox vBox = new VBox();
        Collections.sort(sieges, (o1, o2) -> o2.getId().compareTo(o1.getId()));
        for (Siege siege : sieges) {
            SplitPane splitBattle = new SplitPane();
            VBox phasingNode = new VBox();
            VBox nonPhasingNode = new VBox();
            splitBattle.getItems().addAll(phasingNode, nonPhasingNode);

            HBox phasingCounters = new HBox();
            HBox nonPhasingCounters = new HBox();
            if (!StringUtils.isEmpty(siege.getNonPhasing().getCountry()) || siege.getFortressLevel() != 0) {
                ImageView image = UIUtil.getImage(siege.getNonPhasing().getCountry(), CounterUtil.getFortressesFromLevel(siege.getFortressLevel(), false), null);
                nonPhasingCounters.getChildren().add(image);
            }
            siege.getCounters().stream()
                    .forEach(counter -> {
                        ImageView imageCounter = UIUtil.getImage(counter.getCounter());
                        if (counter.isPhasing()) {
                            phasingCounters.getChildren().add(imageCounter);
                        } else {
                            nonPhasingCounters.getChildren().add(imageCounter);
                        }
                    });
            try {
                ImageView img = new ImageView(new Image(new FileInputStream(new File("data/img/victory.png"))));
                if (siege.isFortressFalls()) {
                    phasingCounters.getChildren().add(img);
                } else if (siege.getStatus() == SiegeStatusEnum.DONE) {
                    nonPhasingCounters.getChildren().add(img);
                }
            } catch (FileNotFoundException e) {
                LOGGER.error("Cannot find victory icon.");
            }

            phasingNode.getChildren().addAll(phasingCounters, createSiegeUndermine(siege));
            nonPhasingNode.getChildren().addAll(nonPhasingCounters, new Label());
            if (siege.getNonPhasing().getModifiers().getFire() != null) {
                boolean breach = siege.getUndermineResult() == SiegeUndermineResultEnum.BREACH_TAKEN;
                phasingNode.getChildren().addAll(createSiegeModifiers(siege.getPhasing()),
                        createSiegeLosses(siege.getPhasing(), siege.getNonPhasing(), true, breach));
                nonPhasingNode.getChildren().addAll(createSiegeModifiers(siege.getNonPhasing()),
                        createSiegeLosses(siege.getNonPhasing(), siege.getPhasing(), false, breach));
            }

            if (siege.getStatus() == SiegeStatusEnum.SELECT_FORCES) {
                phasingNode.getChildren().add(createSiegeSelectForces(siege));
            } else if (siege.getStatus() == SiegeStatusEnum.CHOOSE_MODE) {
                phasingNode.getChildren().add(createSiegeChooseMode(siege));
            } else if (siege.getStatus() == SiegeStatusEnum.CHOOSE_BREACH) {
                phasingNode.getChildren().add(createSiegeChooseBreach());
            } else if (siege.getStatus() == SiegeStatusEnum.CHOOSE_MAN) {
                phasingNode.getChildren().add(createSiegeChooseMan(siege));
            } else if (siege.getStatus() == SiegeStatusEnum.CHOOSE_LOSS) {
                if (BooleanUtils.isNotTrue(siege.getPhasing().isLossesSelected())) {
                    phasingNode.getChildren().add(createSiegeChooseLosses(siege, true));
                }
                if (BooleanUtils.isNotTrue(siege.getNonPhasing().isLossesSelected())) {
                    nonPhasingNode.getChildren().add(createSiegeChooseLosses(siege, false));
                }
            } else if (siege.getStatus() == SiegeStatusEnum.REDEPLOY) {
                nonPhasingNode.getChildren().add(createSiegeRedeploy(siege));
            }

            TitledPane battleNode = new TitledPane(GlobalConfiguration.getMessage(siege.getProvince()) + " - " +
                    GlobalConfiguration.getMessage(siege.getStatus()), splitBattle);
            vBox.getChildren().add(battleNode);
        }

        return new TitledPane(war.getName(), vBox);
    }

    /**
     * @param side of the siege.
     * @return The help tooltip about the siege.
     */
    private Node createSiegeTooltip(SiegeSide side) {
        try {
            ImageView img = new ImageView(new Image(new FileInputStream(new File("data/img/help.png"))));
            Tooltip tooltip = new Tooltip(GlobalConfiguration.getMessage("military.siege.info", side.getTech(), side.getMoral(), side.getSize()));
            Tooltip.install(img, tooltip);
            UIUtil.patchTooltipUntilMigrationJava9(tooltip);
            return img;
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot find help icon.");
            return null;
        }
    }

    /**
     * @param siege the siege.
     * @return the node displaying the undermining information.
     */
    private Label createSiegeUndermine(Siege siege) {

        if (siege.getUndermineDie() == 0) {
            return new Label(GlobalConfiguration.getMessage("military.siege.undermine_not_done", siege.getBonus()));
        }

        String result = GlobalConfiguration.getMessage(siege.getUndermineResult());
        if (result == null) {
            result = GlobalConfiguration.getMessage("nothing");
        }
        return new Label(GlobalConfiguration.getMessage("military.siege.undermine_done", siege.getUndermineDie(), siege.getBonus(), result));
    }

    /**
     * @param side of the siege.
     * @return the node displaying the modifiers of a siege side.
     */
    private Node createSiegeModifiers(SiegeSide side) {
        HBox hBox = new HBox();
        Label modifiers = new Label(side.getModifiers().getFireMod() + " / " + side.getModifiers().getShockMod());
        Node help = createSiegeTooltip(side);
        hBox.getChildren().addAll(modifiers, help);
        if (StringUtils.isNotEmpty(side.getLeader())) {
            Label leaderLabel = new Label("   " + GlobalConfiguration.getMessage("military.battle.leader", GlobalConfiguration.getMessage(side.getLeader())));
            hBox.getChildren().add(leaderLabel);
            Leader leader = GlobalConfiguration.getTables().getLeader(side.getLeader(), side.getCountry());
            if (leader != null) {
                try {
                    ImageView leaderTooltip = new ImageView(new Image(new FileInputStream(new File("data/img/help.png"))));
                    Tooltip tooltip = new Tooltip(GlobalConfiguration.getMessage("military.battle.leader_help",
                            leader.getManoeuvre(), leader.getFire(), leader.getShock(), leader.getSiege()));
                    Tooltip.install(leaderTooltip, tooltip);
                    UIUtil.patchTooltipUntilMigrationJava9(tooltip);
                    hBox.getChildren().add(leaderTooltip);
                } catch (FileNotFoundException e) {
                    LOGGER.error("Cannot find help icon.");
                }
            }
            if (side.getLeaderWounds() != null) {
                if (side.getLeaderWounds() == -1) {
                    hBox.getChildren().add(new Label(GlobalConfiguration.getMessage("military.battle.leader.dead")));
                } else {
                    hBox.getChildren().add(new Label(GlobalConfiguration.getMessage("military.battle.leader.wounded", side.getLeaderWounds())));
                }
            }
        }
        return hBox;
    }

    /**
     * @param side    of the siege.
     * @param against side against of the siege.
     * @return the node displaying the damages of a siege side (only relevant for an assault).
     */
    private Label createSiegeLosses(SiegeSide side, SiegeSide against, boolean besieger, boolean breach) {
        Label label = new Label(getDamage(against.getLosses()));

        StringBuilder sb = new StringBuilder();
        sb.append(getSequenceDamage("military.siege.fire", side.getModifiers().getFire(), side.getModifiers().getFireMod(), true, besieger, breach && !besieger));
        sb.append("\n");
        sb.append(getSequenceDamage("military.siege.shock", side.getModifiers().getShock(), side.getModifiers().getShockMod(), false, besieger, breach && !besieger));
        Tooltip tooltip = new Tooltip(sb.toString());
        UIUtil.patchTooltipUntilMigrationJava9(tooltip);
        label.setTooltip(tooltip);

        return label;
    }

    /**
     * @param key      of the sequence of the damage.
     * @param die      the unmodified roll die of the damage.
     * @param modifier the bonus on the roll die of the damage.
     * @param fire     if it is the fire sequence.
     * @param besieger if it is the besieger side.
     * @param breach   if the fortress was just breached.
     * @return a text displaying all damage info for a given sequence.
     */
    private String getSequenceDamage(String key, Integer die, int modifier, boolean fire, boolean besieger, boolean breach) {
        StringBuilder sb = new StringBuilder();
        sb.append(GlobalConfiguration.getMessage(key));
        sb.append(" : ");
        if (die != null) {
            int min = GlobalConfiguration.getTables().getAssaultResults().stream()
                    .filter(result -> result.isFire() == fire && result.isBesieger() == besieger && result.isBreach() == breach)
                    .map(AssaultResult::getDice)
                    .min(Comparator.naturalOrder())
                    .orElse(3);
            int max = GlobalConfiguration.getTables().getAssaultResults().stream()
                    .filter(result -> result.isFire() == fire && result.isBesieger() == besieger && result.isBreach() == breach)
                    .map(AssaultResult::getDice)
                    .max(Comparator.naturalOrder())
                    .orElse(12);
            int modifiedDie = die + modifier < min ? min : die + modifier > max ? max : die + modifier;
            AssaultResult result = GlobalConfiguration.getTables().getAssaultResults().stream()
                    .filter(cr -> cr.isFire() == fire && cr.isBesieger() == besieger && cr.isBreach() == breach && cr.getDice() == modifiedDie)
                    .findAny()
                    .orElse(null);
            String localDamage = getDamage(result);
            sb.append(GlobalConfiguration.getMessage("military.battle.damage", die, modifier, localDamage));
        } else {
            sb.append(GlobalConfiguration.getMessage("military.battle.no_damage"));
        }
        return sb.toString();
    }

    /**
     * @param siege the siege.
     * @return the node for selecting forces at that start of the siege.
     */
    private Node createSiegeSelectForces(Siege siege) {
        HBox hBox = new HBox();
        List<Long> selectedCounters = new ArrayList<>();

        War war = game.getWars().stream()
                .filter(w -> Objects.equals(w.getId(), siege.getWar().getId()))
                .findAny()
                .orElse(null);
        boolean offensive = siege.isBesiegingOffensive();
        List<String> allies = war.getCountries().stream()
                .filter(c -> c.isOffensive() == offensive)
                .map(c -> c.getCountry().getName())
                .collect(Collectors.toList());
        Node counters = createMultiSelectCounterNode(allies, siege.getProvince(), "military.battle.counters", selectedCounters);
        Button select = new Button(GlobalConfiguration.getMessage("military.battle.select"));
        select.setOnAction(callServiceAsEvent(siegeService::selectForces, () -> new SelectForcesRequest(selectedCounters), "Error when selecting forces at the start of the siege."));

        hBox.getChildren().addAll(counters, select);

        return hBox;
    }

    /**
     * @return the node for selecting the mode (undermine or assault) of the siege.
     */
    private Node createSiegeChooseMode(Siege siege) {
        HBox hBox = new HBox();

        ChoiceBox<SiegeModeEnum> mode = new ChoiceBox<>();
        mode.converterProperty().set(new EnumConverter<>());
        mode.setItems(FXCollections.observableArrayList(SiegeModeEnum.values()));
        ChoiceBox<String> provinces = new ChoiceBox<>();
        IMapMarker marker = markers.stream()
                .filter(m -> StringUtils.equals(m.getId(), siege.getProvince()))
                .findAny()
                .orElse(null);
        provinces.setItems(FXCollections.observableList(marker.getNeighbours().stream()
                .map(BorderMarker::getProvince)
                .map(IMapMarker::getId)
                .collect(Collectors.toList())));
        provinces.setDisable(true);

        mode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            provinces.setDisable(newValue != SiegeModeEnum.REDEPLOY);
        });

        Button choose = new Button(GlobalConfiguration.getMessage("military.siege.choose_mode"));
        choose.setOnAction(callServiceAsEvent(siegeService::chooseMode, () -> new ChooseModeForSiegeRequest(mode.getValue(), provinces.getValue()), "Error when choosing mode of the siege."));

        hBox.getChildren().addAll(mode, provinces, choose);

        return hBox;
    }

    /**
     * @return the node for selecting to take the breach or not after an undermine.
     */
    private Node createSiegeChooseBreach() {
        HBox hBox = new HBox();

        ChoiceBox<ChooseBreachForSiegeRequest.ChoiceBreachEnum> breach = new ChoiceBox<>();
        breach.converterProperty().set(new EnumConverter<>());
        breach.setItems(FXCollections.observableArrayList(ChooseBreachForSiegeRequest.ChoiceBreachEnum.values()));

        Button choose = new Button(GlobalConfiguration.getMessage("military.siege.choose_breach"));
        choose.setOnAction(callServiceAsEvent(siegeService::chooseBreach, () -> new ChooseBreachForSiegeRequest(breach.getValue()), "Error when choosing to take the breach of the siege."));

        hBox.getChildren().addAll(breach, choose);

        return hBox;
    }

    /**
     * @return the node for choosing to man a fortress after taking it.
     */
    private Node createSiegeChooseMan(Siege siege) {
        HBox hBox = new HBox();

        War war = game.getWars().stream()
                .filter(w -> Objects.equals(w.getId(), siege.getWar().getId()))
                .findAny()
                .orElse(null);
        boolean offensive = siege.isBesiegingOffensive();
        List<String> allies = war.getCountries().stream()
                .filter(c -> c.isOffensive() == offensive)
                .map(c -> c.getCountry().getName())
                .collect(Collectors.toList());
        List<Counter> counterList = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), siege.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> allies.contains(counter.getCountry()))
                .collect(Collectors.toList());
        counterList.add(0, null);
        ComboBox<Counter> counter = new ComboBox<>();
        counter.setCellFactory(new CounterCellFactory());
        counter.converterProperty().set(new CounterConverter());
        counter.setItems(FXCollections.observableArrayList(counterList));

        Button choose = new Button(GlobalConfiguration.getMessage("military.siege.choose_man"));
        choose.setOnAction(callServiceAsEvent(siegeService::chooseMan, () -> new ChooseManForSiegeRequest(counter.getValue() != null, counter.getValue() != null ? counter.getValue().getId() : null), "Error when choosing to man the fortress."));

        counter.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            String key = "military.siege.choose_man";
            if (newValue == null) {
                key = "military.siege.choose_no_man";
            }
            choose.setText(GlobalConfiguration.getMessage(key));
        });

        hBox.getChildren().addAll(counter, choose);

        return hBox;
    }

    /**
     * @param siege   the siege.
     * @param phasing player.
     * @return the node for choosing losses at the end of a siege.
     */
    private Node createSiegeChooseLosses(Siege siege, boolean phasing) {
        List<Counter> counterList = siege.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing)
                .map(SiegeCounter::getCounter)
                .collect(Collectors.toList());
        int maxRound = CommonUtil.add(phasing ? siege.getPhasing().getLosses().getRoundLoss() : siege.getNonPhasing().getLosses().getRoundLoss(), 1);
        return createChooseLosses(counterList, maxRound, siegeService::chooseLossesAfterAssault);
    }

    /**
     * @return the node for choosing to man a fortress after taking it.
     */
    private Node createSiegeRedeploy(Siege siege) {
        List<Counter> counterList = siege.getCounters().stream()
                .filter(bc -> !bc.isPhasing())
                .map(SiegeCounter::getCounter)
                .collect(Collectors.toList());
        List<CounterFaceTypeEnum> faces = new ArrayList<>();
        faces.add(null);
        faces.add(CounterFaceTypeEnum.LAND_DETACHMENT);
        War war = game.getWars().stream()
                .filter(w -> Objects.equals(w.getId(), siege.getWar().getId()))
                .findAny()
                .orElse(null);
        boolean offensive = siege.isBesiegingOffensive();
        List<String> allies = war.getCountries().stream()
                .filter(c -> c.isOffensive() != offensive)
                .map(c -> c.getCountry().getName())
                .collect(Collectors.toList());
        List<String> provinces = markers.stream()
                .filter(province -> allies.contains(province.getController()))
                .map(IMapMarker::getId)
                .sorted(Comparator.<String>naturalOrder())
                .collect(Collectors.toList());
        String controller = markers.stream()
                .filter(province -> StringUtils.equals(province.getId(), siege.getProvince()))
                .map(IMapMarker::getController)
                .findAny()
                .orElse(null);

        List<RedeployLine> lines = new ArrayList<>();

        VBox vBox = new VBox();
        new RedeployLine(counterList, faces, provinces, (line, add) -> {
            if (add) {
                lines.add(line);
                vBox.getChildren().add(line.getNode());
            } else {
                lines.remove(line);
                vBox.getChildren().remove(line.getNode());
            }
        });


        Button redeploy = new Button(GlobalConfiguration.getMessage("military.siege.redeploy"));
        redeploy.setOnAction(callServiceAsEvent(siegeService::redeploy, () -> RedeployLine.toRequest(lines, controller), "Error when redeploying forces."));

        lines.get(0).getNode().getChildren().add(redeploy);

        return vBox;
    }

    /**
     * Update the list of battles.
     */
    private void updateBattles() {
        Integer turn = choiceBattleTurn.getSelectionModel().getSelectedItem();

        List<Integer> turns = game.getBattles().stream()
                .map(Battle::getTurn)
                .distinct()
                .sorted(Comparator.<Integer>reverseOrder())
                .collect(Collectors.toList());
        turns.add(0, null);
        choiceBattleTurn.setItems(FXCollections.observableArrayList(turns));

        if (turn != null) {
            choiceBattleTurn.getSelectionModel().select(turn);
        }

        List<Battle> battles = game.getBattles().stream()
                .filter(battle -> battle.getTurn().equals(game.getTurn()) && battle.getStatus() == BattleStatusEnum.NEW)
                .collect(Collectors.toList());
        choiceBattle.setItems(FXCollections.observableArrayList(battles));
    }

    /**
     * Update the list of sieges.
     */
    private void updateSieges() {
        Integer turn = choiceSiegeTurn.getSelectionModel().getSelectedItem();

        List<Integer> turns = game.getSieges().stream()
                .map(Siege::getTurn)
                .distinct()
                .sorted(Comparator.<Integer>reverseOrder())
                .collect(Collectors.toList());
        turns.add(0, null);
        choiceSiegeTurn.setItems(FXCollections.observableArrayList(turns));

        if (turn != null) {
            choiceSiegeTurn.getSelectionModel().select(turn);
        }

        List<Long> offensiveWars = game.getWars().stream()
                .filter(war -> war.getCountries().stream().anyMatch(country -> country.isOffensive() && StringUtils.equals(country.getCountry().getName(), gameConfig.getCountryName())))
                .map(War::getId)
                .collect(Collectors.toList());
        List<Long> defensiveWars = game.getWars().stream()
                .filter(war -> war.getCountries().stream().anyMatch(country -> !country.isOffensive() && StringUtils.equals(country.getCountry().getName(), gameConfig.getCountryName())))
                .map(War::getId)
                .collect(Collectors.toList());

        List<Siege> sieges = game.getSieges().stream()
                .filter(siege -> siege.getTurn().equals(game.getTurn()) && siege.getStatus() == SiegeStatusEnum.NEW &&
                        (siege.isBesiegingOffensive() && offensiveWars.contains(siege.getWar().getId()) || !siege.isBesiegingOffensive() && defensiveWars.contains(siege.getWar().getId())))
                .collect(Collectors.toList());
        choiceSiege.setItems(FXCollections.observableArrayList(sieges));
    }

    /** {@inheritDoc} */
    @Override
    public void update(Diff diff) {
        PlayableCountry country = game.getCountries().stream()
                .filter(playableCountry -> Objects.equals(playableCountry.getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        switch (diff.getTypeObject()) {
            case GAME:
            case STATUS:
            case TURN_ORDER:
            case COUNTER:
            case STACK:
                updateInfoPanel(country);
                break;
            case BATTLE:
                updateBattles();
                break;
            case SIEGE:
                updateSieges();
                break;
            default:
                break;
        }
    }

    /**
     * Inner class for the choose losses node.
     * It is a line for the choose losses.
     * Number of line can vary.
     */
    private class ChooseLossLine {
        /** The global node of a line. */
        private HBox node = new HBox();
        /** Choice box to select a counter. */
        private ChoiceBox<Counter> counters = new ChoiceBox<>();
        /** Choice box to select the number of round losses to take on the counter. */
        private ChoiceBox<Integer> round = new ChoiceBox<>();
        /** Choice box to select the number of third losses to take on the counter. */
        private ChoiceBox<Integer> third = new ChoiceBox<>();
        /** A button. It can be a Add button to add a new Line or a Remove button to remove current line. */
        private Button button;

        /**
         * Constructor.
         *
         * @param counterList the list of counters that can be selected.
         * @param rounds      the list of rounds losses that can be selected.
         * @param thirds      the list of thirds losses that can be selected.
         * @param listener    the listener to get the add and remove redeploy line events.
         */
        private ChooseLossLine(List<Counter> counterList, List<Integer> rounds, List<Integer> thirds, BiConsumer<ChooseLossLine, Boolean> listener) {
            counters.converterProperty().set(new CounterConverter());
            counters.setItems(FXCollections.observableList(counterList));
            round.setItems(FXCollections.observableList(rounds));
            third.setItems(FXCollections.observableList(thirds));
            button = new Button(GlobalConfiguration.getMessage("add"));
            button.setOnAction(event -> {
                        ChooseLossLine newLine = new ChooseLossLine(counterList, rounds, thirds, listener);
                        newLine.button.setText(GlobalConfiguration.getMessage("delete"));
                        newLine.button.setOnAction(delEvent -> listener.accept(newLine, false));
                        listener.accept(newLine, true);
                    }
            );

            node.getChildren().addAll(counters, round, third, button);
            listener.accept(this, true);
        }

        /**
         * Transform a list of ChooseLossLine into a ChooseLossesRequest.
         *
         * @param lines the list of ChooseLossLine.
         * @return a ChooseLossesRequest.
         */
        public ChooseLossesRequest toRequest(List<ChooseLossLine> lines) {
            List<ChooseLossesRequest.UnitLoss> losses = new ArrayList<>();
            for (ChooseLossLine clLine : lines) {
                ChooseLossesRequest.UnitLoss loss = new ChooseLossesRequest.UnitLoss();
                if (clLine.counters.getSelectionModel().getSelectedItem() != null) {
                    loss.setIdCounter(clLine.counters.getSelectionModel().getSelectedItem().getId());
                }
                if (clLine.round.getSelectionModel().getSelectedItem() != null) {
                    loss.setRoundLosses(clLine.round.getSelectionModel().getSelectedItem());
                }
                if (clLine.third.getSelectionModel().getSelectedItem() != null) {
                    loss.setThirdLosses(clLine.third.getSelectionModel().getSelectedItem());
                }
                losses.add(loss);
            }
            return new ChooseLossesRequest(losses);
        }
    }
}
