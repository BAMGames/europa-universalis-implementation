package com.mkl.eu.front.client.military;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.common.ValidateRequest;
import com.mkl.eu.client.service.service.military.*;
import com.mkl.eu.client.service.vo.AbstractWithLoss;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.enumeration.BattleStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.BattleWinnerEnum;
import com.mkl.eu.client.service.vo.enumeration.GameStatusEnum;
import com.mkl.eu.client.service.vo.military.Battle;
import com.mkl.eu.client.service.vo.military.BattleCounter;
import com.mkl.eu.client.service.vo.military.BattleSide;
import com.mkl.eu.client.service.vo.tables.CombatResult;
import com.mkl.eu.client.service.vo.tables.Tech;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.marker.BorderMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.*;
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
public class MilitaryWindow extends AbstractDiffListenerContainer {
    /** Board service. */
    @Autowired
    private IBoardService boardService;
    /** Battle service. */
    @Autowired
    private IBattleService battleService;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
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
    /**         Nodes about military            */
    /********************************************/
    /** The validate military phase button. */
    private Button validateMilitaryPhase;
    /** The invalidate military phase button. */
    private Button invalidateMilitaryPhase;
    /** The selected battle. */
    private ChoiceBox<Battle> choiceBattle;
    /** The choose battle button. */
    private Button chooseBattle;

    /********************************************/
    /**         Nodes about battles             */
    /********************************************/
    /** The selected turn. */
    private ChoiceBox<Integer> choiceBattleTurn;


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
        tabPane.getTabs().add(createInfoTab());
        tabPane.getTabs().add(createBattles());

        updateBattles();
        Integer maxTurn = choiceBattleTurn.getItems().stream()
                .filter(Objects::nonNull)
                .max(Comparator.<Integer>naturalOrder())
                .orElse(null);
        choiceBattleTurn.getSelectionModel().select(maxTurn);
    }

    /**
     * @return the info tab.
     */
    private Tab createInfoTab() {
        Tab tab = new Tab(message.getMessage("military.info.title", null, globalConfiguration.getLocale()));
        tab.setClosable(false);
        VBox vBox = new VBox();
        tab.setContent(vBox);

        Function<Boolean, EventHandler<ActionEvent>> endMilitaryPhase = validate -> event -> {
            Request<ValidateRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new ValidateRequest(validate));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = boardService.validateMilitaryRound(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (FunctionalException e) {
                LOGGER.error("Error when validating the military round.", e);

                processExceptionEvent(new ExceptionEvent(e));
            }
        };

        validateMilitaryPhase = new Button(message.getMessage("military.info.validate", null, globalConfiguration.getLocale()));
        validateMilitaryPhase.setOnAction(endMilitaryPhase.apply(true));
        invalidateMilitaryPhase = new Button(message.getMessage("military.info.invalidate", null, globalConfiguration.getLocale()));
        invalidateMilitaryPhase.setOnAction(endMilitaryPhase.apply(false));
        HBox hBox = new HBox();
        hBox.getChildren().addAll(validateMilitaryPhase, invalidateMilitaryPhase);
        vBox.getChildren().add(hBox);

        choiceBattle = new ChoiceBox<>();
        chooseBattle = new Button(message.getMessage("military.info.choose_battle", null, globalConfiguration.getLocale()));
        choiceBattle.converterProperty().set(new StringConverter<Battle>() {
            /** {@inheritDoc} */
            @Override
            public String toString(Battle object) {
                String province = message.getMessage(object.getProvince(), null, globalConfiguration.getLocale());
                return province;
            }

            /** {@inheritDoc} */
            @Override
            public Battle fromString(String string) {
                return null;
            }
        });
        choiceBattle.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> chooseBattle.setDisable(newValue == null));
        chooseBattle.setOnAction(event -> {
            Request<ChooseProvinceRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new ChooseProvinceRequest(choiceBattle.getSelectionModel().getSelectedItem().getProvince()));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = battleService.chooseBattle(request);

                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (FunctionalException e) {
                LOGGER.error("Error when choosing the battle to proceed.", e);

                processExceptionEvent(new ExceptionEvent(e));
            }
        });
        hBox = new HBox();
        hBox.getChildren().addAll(choiceBattle, chooseBattle);
        vBox.getChildren().add(hBox);

        updateInfoPanel();

        return tab;
    }

    /**
     * Updates the info tab.
     */
    private void updateInfoPanel() {
        CountryOrder countryOrder = game.getOrders().stream()
                .filter(order -> order.getGameStatus() == GameStatusEnum.MILITARY_MOVE && Objects.equals(order.getCountry().getId(), gameConfig.getIdCountry()))
                .findAny()
                .orElse(null);
        validateMilitaryPhase.setDisable(true);
        invalidateMilitaryPhase.setDisable(true);
        chooseBattle.setDisable(true);

        if (countryOrder != null && countryOrder.isActive()) {
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE) {
                validateMilitaryPhase.setDisable(countryOrder.isReady());
                invalidateMilitaryPhase.setDisable(!countryOrder.isReady());
            } else if (game.getStatus() == GameStatusEnum.MILITARY_BATTLES &&
                    game.getBattles().stream().noneMatch(battle -> battle.getStatus() != BattleStatusEnum.NEW && battle.getStatus() != BattleStatusEnum.DONE)) {
                chooseBattle.setDisable(false);
            }
        }
    }

    /**
     * Creates the tab for the battles.
     *
     * @return the tab for the battles.
     */
    private Tab createBattles() {
        Tab tab = new Tab(message.getMessage("military.battle.title", null, globalConfiguration.getLocale()));
        tab.setClosable(false);

        choiceBattleTurn = new ChoiceBox<>();


        VBox vBox = new VBox();
        vBox.getChildren().addAll(choiceBattleTurn);

        tab.setContent(vBox);

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
                        try {
                            FileInputStream fis = new FileInputStream(MarkerUtils.getImagePath(counter.getCounter()));
                            ImageView image = new ImageView(new Image(fis, 40, 40, true, false));
                            if (counter.isPhasing()) {
                                phasingCounters.getChildren().add(image);
                            } else {
                                nonPhasingCounters.getChildren().add(image);
                            }
                        } catch (FileNotFoundException e) {
                            LOGGER.error("Can't load image of counter " + counter);
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

            TitledPane battleNode = new TitledPane(message.getMessage(battle.getProvince(), null, globalConfiguration.getLocale()) + " - " + battle.getStatus(), splitBattle);
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
            Tooltip tooltip = new Tooltip(message.getMessage("military.battle.info",
                    new Object[]{side.getTech(), side.getFireColumn(), side.getShockColumn(), side.getMoral(), side.getSize(), side.getSizeDiff(), side.getPursuitMod()},
                    globalConfiguration.getLocale()));
            Tooltip.install(img, tooltip);
            patchTooltipUntilMigrationJava9(tooltip);
            return img;
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot find help icon.");
            return null;
        }
    }

    /**
     * Patch to make tooltip lasts longer and display immediatly.
     *
     * @param tooltip the tooltip to patch.
     */
    private void patchTooltipUntilMigrationJava9(Tooltip tooltip) {
        // FIXME remove when migrating to java 9 or above
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));

            fieldTimer = objBehavior.getClass().getDeclaredField("hideTimer");
            fieldTimer.setAccessible(true);
            objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(60000)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param side of the battle.
     * @return the node displaying the modifiers of a battle side.
     */
    private Label createBattleModifiers(BattleSide side) {
        return new Label(side.getFirstDay().getFireMod() + " / " + side.getFirstDay().getShockMod() + "   "
                + side.getSecondDay().getFireMod() + " / " + side.getSecondDay().getShockMod());
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
        patchTooltipUntilMigrationJava9(tooltip);
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
        sb.append(message.getMessage(key, null, globalConfiguration.getLocale()));
        sb.append(" : ");
        if (die != null) {
            int min = globalConfiguration.getTables().getCombatResults().stream()
                    .filter(result -> StringUtils.equals(column, result.getColumn()))
                    .map(CombatResult::getDice)
                    .min(Comparator.naturalOrder())
                    .orElse(3);
            int max = globalConfiguration.getTables().getCombatResults().stream()
                    .filter(result -> StringUtils.equals(column, result.getColumn()))
                    .map(CombatResult::getDice)
                    .max(Comparator.naturalOrder())
                    .orElse(12);
            int modifiedDie = die + modifier < min ? min : die + modifier > max ? max : die + modifier;
            CombatResult result = globalConfiguration.getTables().getCombatResults().stream()
                    .filter(cr -> StringUtils.equals(cr.getColumn(), column) && cr.getDice() == modifiedDie)
                    .findAny()
                    .orElse(null);
            String localDamage = getDamage(result.adjustToTech(tech));
            sb.append(message.getMessage("military.battle.damage",
                    new Object[]{die, modifier, localDamage}, globalConfiguration.getLocale()));
        } else {
            sb.append(message.getMessage("military.battle.no_damage", null, globalConfiguration.getLocale()));
        }
        return sb.toString();
    }

    /**
     * @param die the unmodified die roll of the retreat.
     * @return a text displaying all damage info for the retreat.
     */
    private String getRetreatDamage(Integer die) {
        StringBuilder sb = new StringBuilder();
        sb.append(message.getMessage("military.battle.retreat", null, globalConfiguration.getLocale()));
        sb.append(" : ");
        if (die != null) {
            // cap die to [1-8)
            int modifiedDie = Math.max(1, Math.min(8, die));
            String localDamage = getDamage(AbstractWithLoss.create((modifiedDie - 1) / 2));
            sb.append(message.getMessage("military.battle.damage",
                    new Object[]{die, 0, localDamage}, globalConfiguration.getLocale()));
        } else {
            sb.append(message.getMessage("military.battle.no_damage", null, globalConfiguration.getLocale()));
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
        Button withdraw = new Button(message.getMessage("military.battle.select", null, globalConfiguration.getLocale()));
        withdraw.setOnAction(callService(battleService::selectForces, () -> new SelectForcesRequest(selectedCounters), "Error when selecting forces at the start of the battle."));

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
        List<Counter> counterList = game.getStacks().stream()
                .filter(stack -> StringUtils.equals(stack.getProvince(), battle.getProvince()))
                .flatMap(stack -> stack.getCounters().stream())
                .filter(counter -> allies.contains(counter.getCountry()))
                .collect(Collectors.toList());

        MenuButton counters = new MenuButton(message.getMessage(key, null, globalConfiguration.getLocale()));
        for (Counter counter : counterList) {
            CheckBox check = new CheckBox();
            try {
                FileInputStream fis = new FileInputStream(MarkerUtils.getImagePath(counter));
                ImageView image = new ImageView(new Image(fis, 40, 40, true, false));
                check.setGraphic(image);
            } catch (FileNotFoundException e) {
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
        Button withdraw = new Button(message.getMessage("military.battle.withdraw", null, globalConfiguration.getLocale()));
        withdraw.setOnAction(callService(battleService::withdrawBeforeBattle, () -> {
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
                    return message.getMessage("military.battle.retreat", null, globalConfiguration.getLocale());
                }
                return message.getMessage("military.battle.stay", null, globalConfiguration.getLocale());
            }

            @Override
            public Boolean fromString(String string) {
                return null;
            }
        });
        Button retreat = new Button(message.getMessage("military.battle.retreat_first_day", null, globalConfiguration.getLocale()));
        retreat.setOnAction(callService(battleService::retreatFirstDay, () -> {
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
        provinces.setItems(FXCollections.observableList(marker.getNeighbours().stream()
                .map(BorderMarker::getProvince)
                .map(IMapMarker::getId)
                .collect(Collectors.toList())));
        List<Long> selectedCounters = new ArrayList<>();

        Node counters = createMultiSelectCounterNode(battle, phasing, "military.battle.retreat_in_fortress", selectedCounters);


        Button withdraw = new Button(message.getMessage("military.battle.retreat", null, globalConfiguration.getLocale()));
        withdraw.setOnAction(callService(battleService::retreatAfterBattle, () -> {
            String province = provinces.getSelectionModel().getSelectedItem();
            return new RetreatAfterBattleRequest(selectedCounters, province);
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
        StringConverter<Counter> counterConverter = new StringConverter<Counter>() {
            /** {@inheritDoc} */
            @Override
            public String toString(Counter object) {
                return object.getType() + " - " + object.getCountry();
            }

            /** {@inheritDoc} */
            @Override
            public Counter fromString(String string) {
                return null;
            }
        };
        List<Counter> counterList = battle.getCounters().stream()
                .filter(bc -> bc.isPhasing() == phasing)
                .map(BattleCounter::getCounter)
                .collect(Collectors.toList());
        int maxRound = CommonUtil.add(phasing ? battle.getPhasing().getLosses().getRoundLoss() : battle.getNonPhasing().getLosses().getRoundLoss(), 1);
        List<Integer> rounds = Stream.iterate(0, t -> t + 1).limit(maxRound).collect(Collectors.toList());
        List<Integer> thirds = Stream.iterate(0, t -> t + 1).limit(3).collect(Collectors.toList());
        List<ChooseLossLine> lines = new ArrayList<>();

        VBox vBox = new VBox();
        ChooseLossLine line = new ChooseLossLine(counterConverter, counterList, rounds, thirds, "add");
        lines.add(line);
        vBox.getChildren().add(line.node);
        line.buttonBehavior(addEvent -> {
            ChooseLossLine newLine = new ChooseLossLine(counterConverter, counterList, rounds, thirds, "delete");
            newLine.buttonBehavior(delEvent -> {
                vBox.getChildren().remove(newLine.node);
                lines.remove(newLine);
            });
            vBox.getChildren().add(newLine.node);
            lines.add(newLine);
        });
        Button chooseLoss = new Button(message.getMessage("military.battle.choose_losses", null, globalConfiguration.getLocale()));
        chooseLoss.setOnAction(callService(battleService::chooseLossesFromBattle, () -> {
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
        }, "Error when choosing losses from the current battle."));

        lines.get(0).node.getChildren().add(chooseLoss);

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
     * Update the window given the diff.
     *
     * @param diff that will update the window.
     */
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case STATUS:
            case TURN_ORDER:
                updateInfoPanel();
                break;
            case BATTLE:
                updateBattles();
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
         * @param counterConverter converter to apply to the choice box to select the counter.
         * @param counterList      the list of counters that can be selected.
         * @param rounds           the list of rounds losses that can be selected.
         * @param thirds           the list of thirds losses that can be selected.
         * @param buttonKey        the key message for the button.
         */
        private ChooseLossLine(StringConverter<Counter> counterConverter, List<Counter> counterList, List<Integer> rounds, List<Integer> thirds, String buttonKey) {
            counters.converterProperty().set(counterConverter);
            counters.setItems(FXCollections.observableList(counterList));
            round.setItems(FXCollections.observableList(rounds));
            third.setItems(FXCollections.observableList(thirds));
            button = new Button(message.getMessage(buttonKey, null, globalConfiguration.getLocale()));

            node.getChildren().addAll(counters, round, third, button);
        }

        /**
         * Apply a behavior to the button.
         *
         * @param buttonBehavior the behavior to set to the button.
         */
        private void buttonBehavior(EventHandler<ActionEvent> buttonBehavior) {
            button.setOnAction(buttonBehavior);
        }
    }
}
