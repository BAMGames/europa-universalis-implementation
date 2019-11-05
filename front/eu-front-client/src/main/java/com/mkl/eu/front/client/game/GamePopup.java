package com.mkl.eu.front.client.game;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.chat.LoadRoomRequest;
import com.mkl.eu.client.service.service.eco.*;
import com.mkl.eu.client.service.service.game.LoadGameRequest;
import com.mkl.eu.client.service.service.game.LoadTurnOrderRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.util.GameUtil;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.diplo.CountryOrder;
import com.mkl.eu.client.service.vo.diplo.War;
import com.mkl.eu.client.service.vo.diplo.WarLight;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.eco.Competition;
import com.mkl.eu.client.service.vo.eco.TradeFleet;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.military.Battle;
import com.mkl.eu.client.service.vo.military.BattleCounter;
import com.mkl.eu.client.service.vo.military.Siege;
import com.mkl.eu.client.service.vo.military.SiegeCounter;
import com.mkl.eu.front.client.chat.ChatWindow;
import com.mkl.eu.front.client.eco.AdminActionsWindow;
import com.mkl.eu.front.client.eco.EcoWindow;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.InteractiveMap;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import com.mkl.eu.front.client.military.MilitaryWindow;
import com.mkl.eu.front.client.socket.ClientSocket;
import com.mkl.eu.front.client.vo.AuthentHolder;
import de.fhpotsdam.unfolding.marker.Marker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import processing.core.PApplet;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Popup used when loading a game. Holds the actions of opening other popups (map, chat, actions,...).
 * Is not a popup anymore.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class GamePopup implements IDiffListener, ApplicationContextAware {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GamePopup.class);
    /** Content of this component. */
    private TabPane content;
    /** Flag saying that the popup has already been closed. */
    private boolean closed;
    /** Spring application context. */
    private ApplicationContext context;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Game service. */
    @Autowired
    private IGameService gameService;
    /** Chat service. */
    @Autowired
    private IChatService chatService;
    /** Economic service. */
    @Autowired
    private IEconomicService economicService;
    /** PApplet for the intercative map. */
    private InteractiveMap map;
    /** Flag saying that we already initialized the map. */
    private boolean mapInit;
    /** Window containing all the chat. */
    private ChatWindow chatWindow;
    /** Window containing the economics. */
    private EcoWindow ecoWindow;
    /** Window containing the administrative actions. */
    private AdminActionsWindow adminActionsWindow;
    /** Window containing the battles. */
    private MilitaryWindow militaryWindow;
    /** Socket listening to server diff on this game. */
    private ClientSocket client;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Game displayed. */
    private Game game;
    /** Game config to store between constructor and Spring init PostConstruct and to spread to other UIs. */
    private GameConfiguration gameConfig;
    /** Component to be refreshed when status changed. */
    private VBox activeCountries = new VBox();
    /** Title to be refreshed when status changed. */
    private Text info = new Text();

    public GamePopup(Long idGame, Long idCountry, String countryName) {
        gameConfig = new GameConfiguration();
        gameConfig.setIdGame(idGame);
        gameConfig.setIdCountry(idCountry);
        gameConfig.setCountryName(countryName);
    }

    /** @return the content. */
    public Node getContent() {
        return content;
    }

    /**
     * Initialize the popup.
     *
     * @throws FunctionalException Functional exception.
     */
    @PostConstruct
    public void init() throws FunctionalException {
        content = new TabPane();
        initGame();
        Map<String, Marker> markers = MarkerUtils.createMarkers(game);
        initMap(markers);
        initUI();
        initChat();
        List<IMapMarker> mapMarkers = markers.values().stream()
                .filter(marker -> marker instanceof IMapMarker)
                .map(marker -> (IMapMarker) marker)
                .collect(Collectors.toList());
        initEco(mapMarkers);
        initMilitary(mapMarkers);
    }

    /**
     * Load the game.
     *
     * @throws FunctionalException Functional exception.
     */
    private void initGame() throws FunctionalException {
        SimpleRequest<LoadGameRequest> request = new Request<>();
        authentHolder.fillAuthentInfo(request);
        request.setRequest(new LoadGameRequest(gameConfig.getIdGame(), gameConfig.getIdCountry()));

        game = gameService.loadGame(request);
        gameConfig.setVersionGame(game.getVersion());
        Optional<Message> opt = game.getChat().getGlobalMessages().stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));
        if (opt.isPresent()) {
            gameConfig.setMaxIdGlobalMessage(opt.get().getId());
        }
        Long maxIdMessage = null;
        for (Room room : game.getChat().getRooms()) {
            opt = room.getMessages().stream().max((o1, o2) -> (int) (o1.getId() - o2.getId()));
            if (opt.isPresent() && (maxIdMessage == null || opt.get().getId() > maxIdMessage)) {
                maxIdMessage = opt.get().getId();
            }
        }
        gameConfig.setMaxIdMessage(maxIdMessage);

        client = context.getBean(ClientSocket.class, gameConfig);
        client.addDiffListener(this);

        new Thread(client).start();
    }

    /**
     * Initialize the interactive map.
     *
     * @param markers displayed on the map.
     */
    private void initMap(Map<String, Marker> markers) {
        map = context.getBean(InteractiveMap.class, game, gameConfig, markers);
        map.addDiffListener(this);
    }

    /**
     * Initialize the chat window.
     */
    private void initChat() {
        chatWindow = context.getBean(ChatWindow.class, game.getChat(), game.getCountries(), gameConfig);
        chatWindow.addDiffListener(this);
        Tab tab = new Tab(globalConfiguration.getMessage("game.popup.chat"));
        tab.setClosable(false);
        tab.setContent(chatWindow.getTabPane());
        content.getTabs().add(tab);
    }

    /**
     * Initialize the eco window.
     *
     * @param mapMarkers displayed on the map.
     */
    private void initEco(List<IMapMarker> mapMarkers) {
        ecoWindow = context.getBean(EcoWindow.class, game.getCountries(), game.getTradeFleets(), gameConfig);
        ecoWindow.addDiffListener(this);
        Tab tab = new Tab(globalConfiguration.getMessage("game.popup.eco"));
        tab.setClosable(false);
        tab.setContent(ecoWindow.getTabPane());
        content.getTabs().add(tab);

        adminActionsWindow = context.getBean(AdminActionsWindow.class, game, mapMarkers, gameConfig);
        adminActionsWindow.addDiffListener(this);
        tab = new Tab(globalConfiguration.getMessage("game.popup.admin_actions"));
        tab.setClosable(false);
        tab.setContent(adminActionsWindow.getTabPane());
        content.getTabs().add(tab);
    }

    /**
     * Initialize the battle window.
     */
    private void initMilitary(List<IMapMarker> mapMarkers) {
        militaryWindow = context.getBean(MilitaryWindow.class, game, mapMarkers, gameConfig);
        militaryWindow.addDiffListener(this);
        Tab tab = new Tab(globalConfiguration.getMessage("military.title"));
        tab.setClosable(false);
        tab.setContent(militaryWindow.getTabPane());
        content.getTabs().add(tab);
    }

    /**
     * Initialize all the UIs on the popup.
     */
    private void initUI() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(info, 0, 0, 1, 1);
        updateTitle();
        updateActivePlayers();

        grid.add(activeCountries, 1, 0, 1, 5);

        Button mapBtn = new Button(globalConfiguration.getMessage("game.popup.map"));
        mapBtn.setOnAction(event -> {
            if (!mapInit) {
                PApplet.runSketch(new String[]{"InteractiveMap"}, map);
                mapInit = true;
            } else {
                if (map.isVisible()) {
                    map.requestFocus();
                } else {
                    map.setVisible(true);
                }
            }
        });
        grid.add(mapBtn, 0, 1, 1, 1);

        Tab tab = new Tab(globalConfiguration.getMessage("game.popup.global"));
        tab.setClosable(false);
        tab.setContent(grid);
        content.getTabs().add(tab);
    }

    private void updateTitle() {
        StringBuilder sb = new StringBuilder();

        sb.append(globalConfiguration.getMessage("game.popup.turn", game.getTurn()));
        sb.append("\n");
        String statusText = globalConfiguration.getMessage(game.getStatus());
        sb.append(globalConfiguration.getMessage("game.popup.info_phase", statusText));

        info.setText(sb.toString());
    }

    /**
     * Update the list of active countries.
     */
    private void updateActivePlayers() {
        activeCountries.getChildren().clear();
        switch (game.getStatus()) {
            case ECONOMICAL_EVENT:
            case POLITICAL_EVENT:
            case DIPLOMACY:
            case ADMINISTRATIVE_ACTIONS_CHOICE:
            case MILITARY_HIERARCHY:
                List<PlayableCountry> activePlayers = GameUtil.getActivePlayers(game).stream()
                        .collect(Collectors.toList());
                game.getCountries().stream()
                        .filter(c -> StringUtils.isNotEmpty(c.getUsername()))
                        .forEach(country -> {
                            HBox hBox = new HBox();
                            Text text = new Text(country.getName());
                            hBox.getChildren().add(text);
                            if (activePlayers.contains(country)) {
                                try {
                                    Image img = new Image(new FileInputStream(new File("data/img/cross.png")), 16, 16, true, false);
                                    ImageView imgView = new ImageView(img);
                                    hBox.getChildren().add(imgView);
                                } catch (FileNotFoundException e) {
                                    LOGGER.error("Image located at data/img/cross.png not found.", e);
                                }
                            } else {
                                try {
                                    Image img = new Image(new FileInputStream(new File("data/img/check.png")), 16, 16, true, false);
                                    ImageView imgView = new ImageView(img);
                                    hBox.getChildren().add(imgView);
                                } catch (FileNotFoundException e) {
                                    LOGGER.error("Image located at data/img/check.png not found.", e);
                                }
                            }
                            activeCountries.getChildren().add(hBox);
                        });
                break;
            case MILITARY_CAMPAIGN:
            case MILITARY_SUPPLY:
            case MILITARY_MOVE:
            case MILITARY_BATTLES:
            case MILITARY_SIEGES:
            case MILITARY_NEUTRALS:
                int activePosition = game.getOrders().stream()
                        .filter(CountryOrder::isActive)
                        .map(CountryOrder::getPosition)
                        .findFirst()
                        .orElse(-1);
                game.getOrders().stream()
                        .sorted(Comparator.comparing(CountryOrder::getPosition))
                        .forEach(order -> {
                            HBox hBox = new HBox();
                            Text text = new Text(order.getCountry().getName());
                            hBox.getChildren().add(text);
                            if (order.isActive() && (game.getStatus() != GameStatusEnum.MILITARY_MOVE || !order.isReady())) {
                                try {
                                    Image img = new Image(new FileInputStream(new File("data/img/cross.png")), 16, 16, true, false);
                                    ImageView imgView = new ImageView(img);
                                    hBox.getChildren().add(imgView);
                                } catch (FileNotFoundException e) {
                                    LOGGER.error("Image located at data/img/cross.png not found.", e);
                                }
                            } else if (order.getPosition() < activePosition || order.isActive()) {
                                try {
                                    Image img = new Image(new FileInputStream(new File("data/img/check.png")), 16, 16, true, false);
                                    ImageView imgView = new ImageView(img);
                                    hBox.getChildren().add(imgView);
                                } catch (FileNotFoundException e) {
                                    LOGGER.error("Image located at data/img/check.png not found.", e);
                                }
                            }
                            activeCountries.getChildren().add(hBox);
                        });
                break;
            default:
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void handleException(ExceptionEvent event) {
        UIUtil.showException(event.getException(), globalConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void update(DiffEvent event) {
        UIUtil.doInJavaFx(() -> internalUpdate(event));
    }

    /**
     * Method called by update.
     *
     * @param event the event to proceed.
     */
    private void internalUpdate(DiffEvent event) {
        if (event.getIdGame().equals(game.getId())) {
            for (Diff diff : event.getResponse().getDiffs()) {
                if (gameConfig.getVersionGame() >= diff.getVersionGame()) {
                    continue;
                }
                switch (diff.getTypeObject()) {
                    case COUNTRY:
                        updateCountry(game, diff);
                        break;
                    case COUNTER:
                        updateCounter(game, diff);
                        break;
                    case STACK:
                        updateStack(game, diff);
                        break;
                    case ROOM:
                        updateRoom(game, diff);
                        break;
                    case ECO_SHEET:
                        updateEcoSheet(game, diff);
                        break;
                    case ADM_ACT:
                        updateAdmAct(game, diff);
                        break;
                    case STATUS:
                        updateStatus(game, diff);
                        break;
                    case TURN_ORDER:
                        updateTurnOrder(game, diff);
                        break;
                    case BATTLE:
                        updateBattle(game, diff);
                        break;
                    case SIEGE:
                        updateSiege(game, diff);
                        break;
                    default:
                        LOGGER.error("Unknown diff " + diff);
                        break;
                }
                map.update(diff);
                chatWindow.update(diff);
                ecoWindow.update(diff);
                adminActionsWindow.update(diff);
                militaryWindow.update(diff);
            }
            ecoWindow.updateComplete();

            event.getResponse().getMessages().forEach(message -> {
                if ((message.getIdRoom() == null && message.getId() > gameConfig.getMaxIdGlobalMessage())
                        || (message.getIdRoom() != null && message.getId() > gameConfig.getMaxIdMessage())) {
                    Message msg = new Message();
                    msg.setId(message.getId());
                    msg.setMessage(message.getMessage());
                    msg.setDateRead(message.getDateRead());
                    msg.setDateSent(message.getDateSent());
                    PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> message.getIdSender().equals(playableCountry.getId()));
                    msg.setSender(country);
                    if (message.getIdRoom() == null) {
                        game.getChat().getGlobalMessages().add(msg);
                    } else {
                        Room room = CommonUtil.findFirst(game.getChat().getRooms(), room1 -> message.getIdRoom().equals(room1.getId()));
                        if (room != null) {
                            room.getMessages().add(msg);
                        }
                    }
                }
            });

            chatWindow.update(event.getResponse().getMessages());

            if (event.getResponse().getVersionGame() != null) {
                game.setVersion(event.getResponse().getVersionGame());
                gameConfig.setVersionGame(event.getResponse().getVersionGame());
            }
            Optional<MessageDiff> opt = event.getResponse().getMessages().stream().filter(messageDiff -> messageDiff.getIdRoom() == null).max((o1, o2) -> (int) (o1.getId() - o2.getId()));
            if (opt.isPresent() && opt.get().getId() > gameConfig.getMaxIdGlobalMessage()) {
                gameConfig.setMaxIdGlobalMessage(opt.get().getId());
            }
            opt = event.getResponse().getMessages().stream().filter(messageDiff -> messageDiff.getIdRoom() != null).max((o1, o2) -> (int) (o1.getId() - o2.getId()));
            if (opt.isPresent() && opt.get().getId() > gameConfig.getMaxIdMessage()) {
                gameConfig.setMaxIdMessage(opt.get().getId());
            }
        }
    }

    /**
     * Process a country diff event.
     *
     * @param game to update.
     * @param diff involving a country.
     */
    private void updateCountry(Game game, Diff diff) {
        switch (diff.getType()) {
            case MODIFY:
                modifyCountry(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the modify country diff event.
     *
     * @param game to update.
     * @param diff involving a modify country.
     */
    private void modifyCountry(Game game, Diff diff) {
        PlayableCountry country = game.getCountries().stream()
                .filter(c -> diff.getIdObject().equals(c.getId()))
                .findFirst()
                .orElse(null);

        if (country != null) {
            DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.DTI);
            if (attribute != null) {
                Integer dti = Integer.parseInt(attribute.getValue());
                country.setDti(dti);
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.FTI);
            if (attribute != null) {
                Integer fti = Integer.parseInt(attribute.getValue());
                country.setFti(fti);
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.FTI_ROTW);
            if (attribute != null) {
                Integer ftiRotw = Integer.parseInt(attribute.getValue());
                country.setFtiRotw(ftiRotw);
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TECH_LAND);
            if (attribute != null) {
                country.setLandTech(attribute.getValue());
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TECH_NAVAL);
            if (attribute != null) {
                country.setNavalTech(attribute.getValue());
            }
        } else {
            LOGGER.error("Invalid country in country modify event.");
        }
    }

    /**
     * Process a counter diff event.
     *
     * @param game to update.
     * @param diff involving a counter.
     */
    private void updateCounter(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addCounter(game, diff);
                break;
            case MOVE:
                moveCounter(game, diff);
                break;
            case REMOVE:
                removeCounter(game, diff);
                break;
            case MODIFY:
                modifyCounter(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add counter diff event.
     *
     * @param game to update.
     * @param diff involving a add counter.
     */
    private void addCounter(Game game, Diff diff) {
        Stack stack;
        boolean newStack = false;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stack == null) {
                stack = new Stack();
                stack.setId(idStack);
                game.getStacks().add(stack);
                newStack = true;
            }
        } else {
            LOGGER.error("Missing stack id in counter add event.");
            stack = new Stack();
            newStack = true;
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute != null) {
            stack.setProvince(attribute.getValue());
        } else {
            LOGGER.error("Missing province in counter add event.");
        }

        Counter counter = new Counter();
        counter.setId(diff.getIdObject());
        counter.setOwner(stack);
        stack.getCounters().add(counter);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
        if (attribute != null) {
            counter.setType(CounterFaceTypeEnum.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing type in counter add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTRY);
        if (attribute != null) {
            counter.setCountry(attribute.getValue());
            if (newStack) {
                stack.setCountry(attribute.getValue());
            }
        } else {
            LOGGER.error("Missing country in counter add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.LEVEL);
        if (attribute != null) {
            Integer level = Integer.parseInt(attribute.getValue());
            updateCounterLevel(counter, level, game);
        }
    }

    /**
     * Process the move counter diff event.
     *
     * @param game to update.
     * @param diff involving a move counter.
     */
    private void moveCounter(Game game, Diff diff) {
        Stack stack = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_FROM);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        }
        if (stack == null) {
            LOGGER.error("Missing stack from in counter move event.");
            return;
        }

        Counter counter = findFirst(stack.getCounters(), counter1 -> diff.getIdObject().equals(counter1.getId()));
        if (counter == null) {
            LOGGER.error("Missing counter in counter move event.");
            return;
        }

        Stack stackTo;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_TO);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stackTo = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stackTo == null) {
                stackTo = new Stack();
                stackTo.setId(idStack);
                stackTo.setCountry(counter.getCountry());

                attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_TO);
                if (attribute != null) {
                    stackTo.setProvince(attribute.getValue());
                } else {
                    LOGGER.error("Missing province_to in counter move event.");
                }

                game.getStacks().add(stackTo);
            }
        } else {
            LOGGER.error("Missing stack id in counter add event.");
            stackTo = new Stack();
            stackTo.setCountry(counter.getCountry());
        }

        stack.getCounters().remove(counter);
        stackTo.getCounters().add(counter);
        counter.setOwner(stackTo);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_DEL);
        if (attribute != null) {
            destroyStack(game, attribute);
        }
    }

    /**
     * Process the remove counter diff event.
     *
     * @param game to update.
     * @param diff involving a remove counter.
     */
    private void removeCounter(Game game, Diff diff) {
        Stack stack = null;
        Counter counter = null;
        for (Stack stackVo : game.getStacks()) {
            for (Counter counterVo : stackVo.getCounters()) {
                if (diff.getIdObject().equals(counterVo.getId())) {
                    counter = counterVo;
                    stack = stackVo;
                    break;
                }
            }
            if (counter != null) {
                break;
            }
        }

        if (counter == null) {
            LOGGER.error("Missing counter in counter remove event.");
            return;
        }

        stack.getCounters().remove(counter);

        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK_DEL);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            if (idStack.equals(stack.getId())) {
                game.getStacks().remove(stack);
            } else {
                LOGGER.error("Stack to del is not the counter owner in counter remove event.");
            }
        }

        updateCounterLevel(counter, 0, game);
    }

    /**
     * Process the modify counter diff event.
     *
     * @param game to update.
     * @param diff involving a modify counter.
     */
    private void modifyCounter(Game game, Diff diff) {
        Counter counter = findFirst(game.getStacks().stream()
                        .flatMap(stack -> stack.getCounters().stream()),
                counter1 -> diff.getIdObject().equals(counter1.getId()));
        if (counter == null) {
            LOGGER.error("Missing counter in counter move event.");
            return;
        }

        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
        if (attribute != null) {
            CounterFaceTypeEnum type = CounterFaceTypeEnum.valueOf(attribute.getValue());
            counter.setType(type);
        }
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.VETERANS);
        if (attribute != null) {
            Double veterans = Double.valueOf(attribute.getValue());
            counter.setVeterans(veterans);
        }
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.LEVEL);
        if (attribute != null) {
            Integer level = Integer.parseInt(attribute.getValue());
            updateCounterLevel(counter, level, game);
        }
    }

    /**
     * Update the level of a counter.
     *
     * @param counter whose level changed.
     * @param level   for trade fleet or establishment.
     * @param game    to update.
     */
    private void updateCounterLevel(Counter counter, Integer level, Game game) {
        if (CounterUtil.isTradingFleet(counter.getType())) {
            TradeFleet tradeFleet = game.getTradeFleets().stream()
                    .filter(tf -> StringUtils.equals(tf.getProvince(), counter.getOwner().getProvince()) &&
                            StringUtils.equals(tf.getCountry(), counter.getCountry()))
                    .findFirst()
                    .orElse(null);

            if (tradeFleet == null) {
                tradeFleet = new TradeFleet();
                tradeFleet.setCountry(counter.getCountry());
                tradeFleet.setProvince(counter.getOwner().getProvince());
                game.getTradeFleets().add(tradeFleet);
            }

            tradeFleet.setLevel(level);
        } else if (CounterUtil.isEstablishment(counter.getType())) {
            LOGGER.error("Establishment not yet implemented.");
        } else if (level != 0) {
            LOGGER.error("Unknown effect of level for this type: " + counter.getType());
        }
    }

    /**
     * Process a stack diff event.
     *
     * @param game to update.
     * @param diff involving a counter.
     */
    private void updateStack(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addStack(game, diff);
                break;
            case MOVE:
                moveStack(game, diff);
                break;
            case MODIFY:
                modifyStack(game, diff);
                break;
            case REMOVE:
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add stack diff event.
     *
     * @param game to update.
     * @param diff involving a add stack.
     */
    private void addStack(Game game, Diff diff) {
        Stack stack = new Stack();
        stack.setId(diff.getIdObject());
        doIfAttribute(diff, DiffAttributeTypeEnum.PROVINCE, stack::setProvince);
        doIfAttribute(diff, DiffAttributeTypeEnum.COUNTRY, stack::setCountry);
        doIfAttributeEnum(diff, DiffAttributeTypeEnum.MOVE_PHASE, stack::setMovePhase, MovePhaseEnum.class);
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.BESIEGED, stack::setBesieged);

        game.getStacks().add(stack);
    }

    /**
     * Process the move stack diff event.
     *
     * @param game to update.
     * @param diff involving a move stack.
     */
    private void moveStack(Game game, Diff diff) {
        Stack stack;
        Long idStack = diff.getIdObject();
        stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        if (stack == null) {
            LOGGER.error("Missing stack in stack move event.");
            return;
        }

        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_FROM);
        if (attribute != null) {
            if (!StringUtils.equals(attribute.getValue(), stack.getProvince())) {
                LOGGER.error("Stack was not in from province in stack move event.");
            }
        } else {
            LOGGER.error("Missing province from in stack move event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE_TO);
        if (attribute != null) {
            stack.setProvince(attribute.getValue());
        } else {
            LOGGER.error("Missing province to in stack move event.");
        }

        doIfAttributeInteger(diff, DiffAttributeTypeEnum.MOVE_POINTS, stack::setMove);
        doIfAttributeEnum(diff, DiffAttributeTypeEnum.MOVE_PHASE, stack::setMovePhase, MovePhaseEnum.class);
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.BESIEGED, stack::setBesieged);
    }

    /**
     * Process the modify stack diff event.
     *
     * @param game to update.
     * @param diff involving a modify stack.
     */
    private void modifyStack(Game game, Diff diff) {
        Stack stack;
        Long idStack = diff.getIdObject();
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.MOVE_PHASE);
        if (idStack != null) {
            stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stack != null) {
                doIfAttributeEnum(diff, DiffAttributeTypeEnum.MOVE_PHASE, stack::setMovePhase, MovePhaseEnum.class);
                doIfAttribute(diff, DiffAttributeTypeEnum.COUNTRY, stack::setCountry);
            }
        } else if (attribute != null && StringUtils.equals(attribute.getValue(), MovePhaseEnum.NOT_MOVED.name())) {
            // If no stack set and new move phase is NOT_MOVED, then it is the reset of each round of MOVED stacks.
            game.getStacks().stream()
                    .filter(stack1 -> stack1.getMovePhase() != null)
                    .forEach(stack1 -> {
                        stack1.setMove(0);
                        if (stack1.getMovePhase().isBesieging()) {
                            stack1.setMovePhase(MovePhaseEnum.STILL_BESIEGING);
                        } else {
                            stack1.setMovePhase(MovePhaseEnum.NOT_MOVED);
                        }
                    });
        }
    }

    /**
     * Generic destroyStack diff update.
     *
     * @param game      to update.
     * @param attribute of type destroy stack.
     */
    private void destroyStack(Game game, DiffAttributes attribute) {
        Long idStack = Long.parseLong(attribute.getValue());
        Stack stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        if (stack != null) {
            game.getStacks().remove(stack);
        } else {
            LOGGER.error("Missing stack for destroy stack generic event.");
        }
    }

    /**
     * Process a room diff event.
     *
     * @param game to update.
     * @param diff involving a room.
     */
    private void updateRoom(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addRoom(game, diff);
                break;
            case LINK:
                inviteKickRoom(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add room diff event.
     *
     * @param game to update.
     * @param diff involving a add room.
     */
    private void addRoom(Game game, Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());

            if (idCountry.equals(gameConfig.getIdCountry())) {
                Room room = new Room();
                room.setId(diff.getIdObject());
                attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.NAME);
                if (attribute != null) {
                    room.setName(attribute.getValue());
                } else {
                    LOGGER.error("Missing name in room add event.");
                }
                room.setVisible(true);
                room.setPresent(true);
                PlayableCountry country = findFirst(game.getCountries(), country1 -> idCountry.equals(country1.getId()));
                room.setOwner(country);
                room.getCountries().add(country);

                game.getChat().getRooms().add(room);
            }
        } else {
            LOGGER.error("Missing country id in counter add event.");
        }
    }

    /**
     * Process the link room diff event.
     *
     * @param game to update.
     * @param diff involving a add room.
     */
    private void inviteKickRoom(Game game, Diff diff) {
        boolean invite = false;
        Long idRoom = diff.getIdObject();
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.INVITE);
        if (attribute != null) {
            invite = Boolean.parseBoolean(attribute.getValue());
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());

            if (idCountry.equals(gameConfig.getIdCountry())) {

                Room room = CommonUtil.findFirst(game.getChat().getRooms(), room1 -> idRoom.equals(room1.getId()));
                if (room == null) {
                    SimpleRequest<LoadRoomRequest> request = new SimpleRequest<>();
                    authentHolder.fillAuthentInfo(request);
                    request.setRequest(new LoadRoomRequest(gameConfig.getIdGame(), gameConfig.getIdCountry(), idRoom));
                    try {
                        room = chatService.loadRoom(request);
                        game.getChat().getRooms().add(room);
                    } catch (FunctionalException e) {
                        LOGGER.error("Can't load room.", e);
                    }

                    return;
                } else {
                    room.setPresent(invite);
                }
            }
            Room room = CommonUtil.findFirst(game.getChat().getRooms(), room1 -> idRoom.equals(room1.getId()));
            PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> idCountry.equals(playableCountry.getId()));
            if (room != null && country != null) {
                if (invite) {
                    room.getCountries().add(country);
                } else {
                    room.getCountries().remove(country);
                }
            }
        } else {
            LOGGER.error("Missing country id in counter add event.");
        }
    }

    /**
     * Process a economical sheet diff event.
     *
     * @param game to update.
     * @param diff involving an economical sheet.
     */
    private void updateEcoSheet(Game game, Diff diff) {
        switch (diff.getType()) {
            case INVALIDATE:
                invalidateSheet(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the invalidate sheet diff event.
     *
     * @param game to update.
     * @param diff involving an invalidate sheet.
     */
    private void invalidateSheet(Game game, Diff diff) {
        Long idCountry = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null && !StringUtils.isEmpty(attribute.getValue())) {
            idCountry = Long.parseLong(attribute.getValue());
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TURN);
        if (attribute != null) {
            Integer turn = Integer.parseInt(attribute.getValue());
            SimpleRequest<LoadEcoSheetsRequest> request = new SimpleRequest<>();
            authentHolder.fillAuthentInfo(request);
            request.setRequest(new LoadEcoSheetsRequest(gameConfig.getIdGame(), idCountry, turn));
            try {
                java.util.List<EconomicalSheetCountry> sheets = economicService.loadEconomicSheets(request);

                if (sheets != null) {
                    for (EconomicalSheetCountry sheet : sheets) {
                        PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> sheet.getIdCountry().equals(playableCountry.getId()));
                        if (country != null) {
                            int index = country.getEconomicalSheets().indexOf(
                                    CommonUtil.findFirst(country.getEconomicalSheets(), o -> o.getId().equals(sheet.getSheet().getId())));
                            if (index != -1) {
                                country.getEconomicalSheets().set(index, sheet.getSheet());
                            } else {
                                country.getEconomicalSheets().add(sheet.getSheet());
                            }
                        }
                    }
                }
            } catch (FunctionalException e) {
                LOGGER.error("Can't load economic sheets.", e);
            }
        } else {
            LOGGER.error("Missing turn in invalidate sheet event.");
        }
    }

    /**
     * Process a administrative action diff event.
     *
     * @param game to update.
     * @param diff involving an administrative action.
     */
    private void updateAdmAct(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addAdmAct(game, diff);
                break;
            case REMOVE:
                removeAdmAct(game, diff);
                break;
            case VALIDATE:
                validateAdmAct(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add administrative action diff event.
     *
     * @param game to update.
     * @param diff involving a add administrative action.
     */
    private void addAdmAct(Game game, Diff diff) {
        PlayableCountry country = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());
            country = findFirst(game.getCountries(), c -> idCountry.equals(c.getId()));
        }

        if (country != null) {
            AdministrativeAction admAct = new AdministrativeAction();
            admAct.setId(diff.getIdObject());
            admAct.setStatus(AdminActionStatusEnum.PLANNED);
            country.getAdministrativeActions().add(admAct);

            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TURN);
            if (attribute != null) {
                admAct.setTurn(Integer.parseInt(attribute.getValue()));
            } else {
                LOGGER.error("Missing turn in adm act add event.");
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
            if (attribute != null) {
                admAct.setType(AdminActionTypeEnum.valueOf(attribute.getValue()));
            } else {
                LOGGER.error("Missing type in adm act add event.");
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COST);
            if (attribute != null) {
                admAct.setCost(Integer.parseInt(attribute.getValue()));
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_OBJECT);
            if (attribute != null) {
                admAct.setIdObject(Long.parseLong(attribute.getValue()));
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
            if (attribute != null) {
                admAct.setProvince(attribute.getValue());
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTER_FACE_TYPE);
            if (attribute != null) {
                admAct.setCounterFaceType(CounterFaceTypeEnum.valueOf(attribute.getValue()));
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COLUMN);
            if (attribute != null) {
                admAct.setColumn(Integer.parseInt(attribute.getValue()));
            }
            attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.BONUS);
            if (attribute != null) {
                admAct.setBonus(Integer.parseInt(attribute.getValue()));
            }
        } else {
            LOGGER.error("Missing or wrong country in adm act add event.");
        }
    }

    /**
     * Process the remove administrative action diff event.
     *
     * @param game to update.
     * @param diff involving a remove administrative action.
     */
    private void removeAdmAct(Game game, Diff diff) {
        PlayableCountry country = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());
            country = findFirst(game.getCountries(), c -> idCountry.equals(c.getId()));
        }

        if (country != null) {
            AdministrativeAction admAct = CommonUtil.findFirst(country.getAdministrativeActions().stream(), action -> action.getId().equals(diff.getIdObject()));
            if (admAct != null) {
                country.getAdministrativeActions().remove(admAct);
            } else {
                LOGGER.error("Wrong administrative action id in adm act remove event.");
            }
        } else {
            LOGGER.error("Missing or wrong country in adm act remove event.");
        }
    }

    /**
     * Process the validate administrative action event.
     *
     * @param game to update.
     * @param diff involving a validate administrative action.
     */
    private void validateAdmAct(Game game, Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TURN);
        if (attribute != null) {
            Integer turn = Integer.parseInt(attribute.getValue());
            SimpleRequest<LoadAdminActionsRequest> request = new SimpleRequest<>();
            authentHolder.fillAuthentInfo(request);
            request.setRequest(new LoadAdminActionsRequest(gameConfig.getIdGame(), turn));
            try {
                java.util.List<AdministrativeActionCountry> actions = economicService.loadAdminActions(request);

                for (AdministrativeActionCountry action : actions) {
                    PlayableCountry country = CommonUtil.findFirst(game.getCountries(), playableCountry -> action.getIdCountry().equals(playableCountry.getId()));
                    if (country != null) {
                        int index = country.getAdministrativeActions().indexOf(
                                CommonUtil.findFirst(country.getAdministrativeActions(), o -> o.getId().equals(action.getAction().getId())));
                        if (index != -1) {
                            country.getAdministrativeActions().set(index, action.getAction());
                        } else {
                            country.getAdministrativeActions().add(action.getAction());
                        }
                    }
                }
            } catch (FunctionalException e) {
                LOGGER.error("Can't load administrative actions.", e);
            }

            SimpleRequest<LoadCompetitionsRequest> requestComp = new SimpleRequest<>();
            authentHolder.fillAuthentInfo(requestComp);
            requestComp.setRequest(new LoadCompetitionsRequest(gameConfig.getIdGame(), turn));
            try {
                java.util.List<Competition> competitions = economicService.loadCompetitions(requestComp);

                if (CollectionUtils.isNotEmpty(competitions)) {
                    game.getCompetitions().addAll(competitions);
                }
            } catch (FunctionalException e) {
                LOGGER.error("Can't load competitions.", e);
            }
        } else {
            LOGGER.error("Missing turn in invalidate administrative action event.");
        }
    }

    /**
     * Process a status diff event.
     *
     * @param game to update.
     * @param diff involving a status.
     */
    private void updateStatus(Game game, Diff diff) {
        switch (diff.getType()) {
            case MODIFY:
                modifyStatus(game, diff);
                break;
            case VALIDATE:
                validateStatus(game, diff);
                break;
            case INVALIDATE:
                invalidateStatus(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
        updateTitle();
        updateActivePlayers();
    }

    /**
     * Process the modify status action diff event.
     *
     * @param game to update.
     * @param diff involving a modify status.
     */
    private void modifyStatus(Game game, Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (attribute != null) {
            game.setStatus(GameStatusEnum.valueOf(attribute.getValue()));

            // New turn order for military phase
            if (game.getStatus() == GameStatusEnum.MILITARY_MOVE) {
                SimpleRequest<LoadTurnOrderRequest> request = new SimpleRequest<>();
                authentHolder.fillAuthentInfo(request);
                request.setRequest(new LoadTurnOrderRequest(gameConfig.getIdGame()));
                try {
                    List<CountryOrder> orders = gameService.loadTurnOrder(request);

                    game.getOrders().clear();
                    game.getOrders().addAll(orders);
                } catch (FunctionalException e) {
                    LOGGER.error("Can't load turn order.", e);
                }
            }
        }
    }

    /**
     * Process the validate status action diff event.
     *
     * @param game to update.
     * @param diff involving a validatation status.
     */
    private void validateStatus(Game game, Diff diff) {
        PlayableCountry country = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());
            country = findFirst(game.getCountries(), c -> idCountry.equals(c.getId()));
        }

        switch (game.getStatus()) {
            case ADMINISTRATIVE_ACTIONS_CHOICE:
                if (country != null) {
                    country.setReady(true);
                } else {
                    game.getCountries().stream()
                            .filter(c -> StringUtils.isNotEmpty(c.getUsername()))
                            .forEach(c -> c.setReady(true));
                }
                break;
            case MILITARY_MOVE:
                if (country != null) {
                    Long idCountry = country.getId();
                    game.getOrders().stream()
                            .filter(order -> order.getCountry().getId().equals(idCountry) &&
                                    order.isActive())
                            .forEach(order -> order.setActive(false));
                } else {
                    game.getOrders().stream()
                            .forEach(order -> order.setActive(false));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Process the invalidate status action diff event.
     *
     * @param game to update.
     * @param diff involving an invalidattion status.
     */
    private void invalidateStatus(Game game, Diff diff) {
        PlayableCountry country = null;
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());
            country = findFirst(game.getCountries(), c -> idCountry.equals(c.getId()));
        }

        switch (game.getStatus()) {
            case ADMINISTRATIVE_ACTIONS_CHOICE:
                if (country != null) {
                    country.setReady(false);
                } else {
                    game.getCountries().stream()
                            .filter(c -> StringUtils.isNotEmpty(c.getUsername()))
                            .forEach(c -> c.setReady(false));
                }
                break;
            case MILITARY_MOVE:
            default:
                break;
        }
    }

    /**
     * Process a turn order diff event.
     *
     * @param game to update.
     * @param diff involving a turn order.
     */
    private void updateTurnOrder(Game game, Diff diff) {
        switch (diff.getType()) {
            case VALIDATE:
                validateTurnOrder(game, diff);
                break;
            case INVALIDATE:
                invalidateTurnOrder(game, diff);
                break;
            case MODIFY:
                modifyTurnOrder(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
        updateActivePlayers();
    }

    /**
     * Process the validate turn order diff event.
     *
     * @param game to update.
     * @param diff involving an validate turn order.
     */
    private void validateTurnOrder(Game game, Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (StringUtils.isEmpty(attribute.getValue())) {
            LOGGER.error("Missing status in modify turn order event.");
        }
        GameStatusEnum gameStatus = GameStatusEnum.valueOf(attribute.getValue());

        Long tmp = null;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            tmp = Long.parseLong(attribute.getValue());
        }
        Long idCountry = tmp;

        game.getOrders().stream()
                .filter(o -> (idCountry == null || idCountry.equals(o.getCountry().getId())))
                .forEach(o -> o.setReady(true));
    }

    /**
     * Process the invalidate turn order diff event.
     *
     * @param game to update.
     * @param diff involving an invalidate turn order.
     */
    private void invalidateTurnOrder(Game game, Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (StringUtils.isEmpty(attribute.getValue())) {
            LOGGER.error("Missing status in modify turn order event.");
        }
        GameStatusEnum gameStatus = GameStatusEnum.valueOf(attribute.getValue());

        Long tmp = null;
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            tmp = Long.parseLong(attribute.getValue());
        }
        Long idCountry = tmp;

        game.getOrders().stream()
                .filter(o -> (idCountry == null || idCountry.equals(o.getCountry().getId())))
                .forEach(o -> o.setReady(false));
    }

    /**
     * Process the modify turn order diff event.
     *
     * @param game to update.
     * @param diff involving an modify turn order.
     */
    private void modifyTurnOrder(Game game, Diff diff) {
        DiffAttributes attributeActive = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ACTIVE);
        DiffAttributes attributeStatus = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (StringUtils.isEmpty(attributeActive.getValue())) {
            LOGGER.error("Missing active in modify turn order event.");
        }
        if (StringUtils.isEmpty(attributeStatus.getValue())) {
            LOGGER.error("Missing status in modify turn order event.");
        }
        int position = Integer.valueOf(attributeActive.getValue());
        GameStatusEnum gameStatus = GameStatusEnum.valueOf(attributeStatus.getValue());

        game.getOrders().stream()
                .forEach(o -> {
                    o.setActive(false);
                    o.setReady(false);
                });
        game.getOrders().stream()
                .filter(o -> o.getPosition() == position)
                .forEach(o -> o.setActive(true));
    }

    /**
     * Process a battle diff event.
     *
     * @param game to update.
     * @param diff involving a battle.
     */
    private void updateBattle(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addBattle(game, diff);
                break;
            case MODIFY:
                modifyBattle(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add battle diff event.
     *
     * @param game to update.
     * @param diff involving a add battle.
     */
    private void addBattle(Game game, Diff diff) {
        Battle battle = new Battle();
        game.getBattles().add(battle);
        battle.setId(diff.getIdObject());

        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute != null) {
            battle.setProvince(attribute.getValue());
        } else {
            LOGGER.error("Missing province in battle add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TURN);
        if (attribute != null) {
            Integer turn = Integer.parseInt(attribute.getValue());
            battle.setTurn(turn);
        } else {
            LOGGER.error("Missing turn in battle add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (attribute != null) {
            battle.setStatus(BattleStatusEnum.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing status in battle add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_WAR);
        if (attribute != null) {
            Long idWar = Long.parseLong(attribute.getValue());
            WarLight war = game.getWars().stream()
                    .filter(w -> Objects.equals(w.getId(), idWar))
                    .map(War::toLight)
                    .findAny()
                    .orElse(null);
            battle.setWar(war);
        } else {
            LOGGER.error("Missing war in battle add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_OFFENSIVE);
        if (attribute != null) {
            battle.setPhasingOffensive(Boolean.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing phasing offensive in battle add event.");
        }
    }

    /**
     * Process the modify battle diff event.
     *
     * @param game to update.
     * @param diff involving a modify battle.
     */
    private void modifyBattle(Game game, Diff diff) {
        Battle battle = game.getBattles().stream()
                .filter(b -> Objects.equals(b.getId(), diff.getIdObject()))
                .findAny()
                .orElse(null);
        if (battle == null) {
            LOGGER.error("Missing battle in battle modify event.");
            return;
        }

        doIfAttributeEnum(diff, DiffAttributeTypeEnum.STATUS, battle::setStatus, BattleStatusEnum.class);
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_READY);
        if (attribute != null) {
            boolean ready = Boolean.valueOf(attribute.getValue());
            switch (battle.getStatus()) {
                case SELECT_FORCES:
                    battle.getPhasing().setForces(ready);
                    break;
                case CHOOSE_LOSS:
                    battle.getPhasing().setLossesSelected(ready);
                    break;
                case RETREAT:
                    battle.getPhasing().setRetreatSelected(ready);
                    break;
            }
        }
        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.NON_PHASING_READY);
        if (attribute != null) {
            boolean ready = Boolean.valueOf(attribute.getValue());
            switch (battle.getStatus()) {
                case SELECT_FORCES:
                    battle.getNonPhasing().setForces(ready);
                    break;
                case CHOOSE_LOSS:
                    battle.getNonPhasing().setLossesSelected(ready);
                    break;
                case RETREAT:
                    battle.getNonPhasing().setRetreatSelected(ready);
                    break;
            }
        }
        Function<Boolean, Consumer<DiffAttributes>> addCounter = phasing -> attr -> {
            Long idCounter = Long.parseLong(attr.getValue());
            Counter counter = game.getStacks().stream()
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(c -> Objects.equals(c.getId(), idCounter))
                    .findAny()
                    .orElse(null);
            if (counter == null) {
                LOGGER.error("Counter does not exist in modify battle phasing counter add diff event.");
            } else {
                BattleCounter battleCounter = new BattleCounter();
                battleCounter.setPhasing(phasing);
                battleCounter.setCounter(counter);
                battle.getCounters().add(battleCounter);
            }
        };
        diff.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_COUNTER_ADD)
                .forEach(addCounter.apply(true));
        diff.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD)
                .forEach(addCounter.apply(false));

        doIfAttributeEnum(diff, DiffAttributeTypeEnum.END, battle::setEnd, BattleEndEnum.class);
        doIfAttributeEnum(diff, DiffAttributeTypeEnum.WINNER, battle::setWinner, BattleWinnerEnum.class);

        doIfAttributeDouble(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SIZE, value -> battle.getPhasing().setSize(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_TECH, value -> battle.getPhasing().setTech(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRE_COL, value -> battle.getPhasing().setFireColumn(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SHOCK_COL, value -> battle.getPhasing().setShockColumn(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_MORAL, value -> battle.getPhasing().setMoral(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT_MOD, value -> battle.getPhasing().setPursuitMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_PURSUIT, value -> battle.getPhasing().setPursuit(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SIZE_DIFF, value -> battle.getPhasing().setSizeDiff(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_RETREAT, value -> battle.getPhasing().setRetreat(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS, value -> battle.getPhasing().getLosses().setRoundLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS, value -> battle.getPhasing().getLosses().setThirdLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS, value -> battle.getPhasing().getLosses().setMoraleLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE_MOD, value -> battle.getPhasing().getFirstDay().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE, value -> battle.getPhasing().getFirstDay().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK_MOD, value -> battle.getPhasing().getFirstDay().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK, value -> battle.getPhasing().getFirstDay().setShock(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE_MOD, value -> battle.getPhasing().getSecondDay().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_FIRE, value -> battle.getPhasing().getSecondDay().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK_MOD, value -> battle.getPhasing().getSecondDay().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SECOND_DAY_SHOCK, value -> battle.getPhasing().getSecondDay().setShock(value));

        doIfAttributeDouble(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE, value -> battle.getNonPhasing().setSize(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_TECH, value -> battle.getNonPhasing().setTech(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRE_COL, value -> battle.getNonPhasing().setFireColumn(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SHOCK_COL, value -> battle.getNonPhasing().setShockColumn(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORAL, value -> battle.getNonPhasing().setMoral(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT_MOD, value -> battle.getNonPhasing().setPursuitMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_PURSUIT, value -> battle.getNonPhasing().setPursuit(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE_DIFF, value -> battle.getNonPhasing().setSizeDiff(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_RETREAT, value -> battle.getNonPhasing().setRetreat(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS, value -> battle.getNonPhasing().getLosses().setRoundLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS, value -> battle.getNonPhasing().getLosses().setThirdLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS, value -> battle.getNonPhasing().getLosses().setMoraleLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD, value -> battle.getNonPhasing().getFirstDay().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE, value -> battle.getNonPhasing().getFirstDay().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD, value -> battle.getNonPhasing().getFirstDay().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK, value -> battle.getNonPhasing().getFirstDay().setShock(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE_MOD, value -> battle.getNonPhasing().getSecondDay().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_FIRE, value -> battle.getNonPhasing().getSecondDay().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK_MOD, value -> battle.getNonPhasing().getSecondDay().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SECOND_DAY_SHOCK, value -> battle.getNonPhasing().getSecondDay().setShock(value));
    }

    /**
     * Process a siege diff event.
     *
     * @param game to update.
     * @param diff involving a siege.
     */
    private void updateSiege(Game game, Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addSiege(game, diff);
                break;
            case MODIFY:
                modifySiege(game, diff);
                break;
            default:
                LOGGER.error("Unknown diff " + diff);
                break;
        }
    }

    /**
     * Process the add siege diff event.
     *
     * @param game to update.
     * @param diff involving a add siege.
     */
    private void addSiege(Game game, Diff diff) {
        Siege siege = new Siege();
        game.getSieges().add(siege);
        siege.setId(diff.getIdObject());

        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
        if (attribute != null) {
            siege.setProvince(attribute.getValue());
        } else {
            LOGGER.error("Missing province in siege add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TURN);
        if (attribute != null) {
            Integer turn = Integer.parseInt(attribute.getValue());
            siege.setTurn(turn);
        } else {
            LOGGER.error("Missing turn in siege add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STATUS);
        if (attribute != null) {
            siege.setStatus(SiegeStatusEnum.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing status in siege add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_WAR);
        if (attribute != null) {
            Long idWar = Long.parseLong(attribute.getValue());
            WarLight war = game.getWars().stream()
                    .filter(w -> Objects.equals(w.getId(), idWar))
                    .map(War::toLight)
                    .findAny()
                    .orElse(null);
            siege.setWar(war);
        } else {
            LOGGER.error("Missing war in siege add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_OFFENSIVE);
        if (attribute != null) {
            siege.setBesiegingOffensive(Boolean.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing phasing offensive in siege add event.");
        }
    }

    /**
     * Process the modify siege diff event.
     *
     * @param game to update.
     * @param diff involving a modify siege.
     */
    private void modifySiege(Game game, Diff diff) {
        Siege siege = game.getSieges().stream()
                .filter(b -> Objects.equals(b.getId(), diff.getIdObject()))
                .findAny()
                .orElse(null);
        if (siege == null) {
            LOGGER.error("Missing siege in siege modify event.");
            return;
        }

        doIfAttributeEnum(diff, DiffAttributeTypeEnum.STATUS, siege::setStatus, SiegeStatusEnum.class);
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.PHASING_READY, value -> siege.getPhasing().setLossesSelected(value));
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.NON_PHASING_READY, value -> siege.getNonPhasing().setLossesSelected(value));
        Function<Boolean, Consumer<DiffAttributes>> addCounter = phasing -> attr -> {
            Long idCounter = Long.parseLong(attr.getValue());
            Counter counter = game.getStacks().stream()
                    .flatMap(stack -> stack.getCounters().stream())
                    .filter(c -> Objects.equals(c.getId(), idCounter))
                    .findAny()
                    .orElse(null);
            if (counter == null) {
                LOGGER.error("Counter does not exist in modify battle phasing counter add diff event.");
            } else {
                SiegeCounter siegeCounter = new SiegeCounter();
                siegeCounter.setPhasing(phasing);
                siegeCounter.setCounter(counter);
                siege.getCounters().add(siegeCounter);
            }
        };
        diff.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.PHASING_COUNTER_ADD)
                .forEach(addCounter.apply(true));
        diff.getAttributes().stream()
                .filter(attr -> attr.getType() == DiffAttributeTypeEnum.NON_PHASING_COUNTER_ADD)
                .forEach(addCounter.apply(false));

        doIfAttributeInteger(diff, DiffAttributeTypeEnum.LEVEL, siege::setFortressLevel);
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BONUS, siege::setBonus);
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.SIEGE_UNDERMINE_DIE, siege::setUndermineDie);
        doIfAttributeEnum(diff, DiffAttributeTypeEnum.SIEGE_UNDERMINE_RESULT, siege::setUndermineResult, SiegeUndermineResultEnum.class);
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.SIEGE_FORTRESS_FALLS, siege::setFortressFalls);
        doIfAttributeBoolean(diff, DiffAttributeTypeEnum.SIEGE_BREACH, siege::setBreach);

        doIfAttributeDouble(diff, DiffAttributeTypeEnum.BATTLE_PHASING_SIZE, value -> siege.getPhasing().setSize(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_PHASING_TECH, value -> siege.getPhasing().setTech(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_MORAL, value -> siege.getPhasing().setMoral(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_ROUND_LOSS, value -> siege.getPhasing().getLosses().setRoundLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_THIRD_LOSS, value -> siege.getPhasing().getLosses().setThirdLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_MORALE_LOSS, value -> siege.getPhasing().getLosses().setMoraleLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE_MOD, value -> siege.getPhasing().getModifiers().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_FIRE, value -> siege.getPhasing().getModifiers().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK_MOD, value -> siege.getPhasing().getModifiers().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_PHASING_FIRST_DAY_SHOCK, value -> siege.getPhasing().getModifiers().setShock(value));

        doIfAttributeDouble(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_SIZE, value -> siege.getNonPhasing().setSize(value));
        doIfAttribute(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_TECH, value -> siege.getNonPhasing().setTech(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORAL, value -> siege.getNonPhasing().setMoral(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_ROUND_LOSS, value -> siege.getNonPhasing().getLosses().setRoundLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_THIRD_LOSS, value -> siege.getNonPhasing().getLosses().setThirdLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_MORALE_LOSS, value -> siege.getNonPhasing().getLosses().setMoraleLoss(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE_MOD, value -> siege.getNonPhasing().getModifiers().setFireMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_FIRE, value -> siege.getNonPhasing().getModifiers().setFire(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK_MOD, value -> siege.getNonPhasing().getModifiers().setShockMod(value));
        doIfAttributeInteger(diff, DiffAttributeTypeEnum.BATTLE_NON_PHASING_FIRST_DAY_SHOCK, value -> siege.getNonPhasing().getModifiers().setShock(value));
    }

    /**
     * Apply a consumer to a retrieved diff attribute.
     *
     * @param diff     the global diff.
     * @param type     the type of diff attribute we want.
     * @param setValue the consumer.
     */
    private void doIfAttribute(Diff diff, DiffAttributeTypeEnum type, Consumer<String> setValue) {
        diff.getAttributes().stream()
                .filter(attr -> attr.getType() == type)
                .map(DiffAttributes::getValue)
                .forEach(setValue);
    }

    /**
     * Apply a consumer to a retrieved diff attribute after transforming it to an Integer.
     *
     * @param diff     the global diff.
     * @param type     the type of diff attribute we want.
     * @param setValue the consumer.
     */
    private void doIfAttributeInteger(Diff diff, DiffAttributeTypeEnum type, Consumer<Integer> setValue) {
        doIfAttribute(diff, type, attribute -> {
            if (!StringUtils.isEmpty(attribute)) {
                setValue.accept(Integer.parseInt(attribute));
            }
        });
    }

    /**
     * Apply a consumer to a retrieved diff attribute after transforming it to a Double.
     *
     * @param diff     the global diff.
     * @param type     the type of diff attribute we want.
     * @param setValue the consumer.
     */
    private void doIfAttributeDouble(Diff diff, DiffAttributeTypeEnum type, Consumer<Double> setValue) {
        doIfAttribute(diff, type, attribute -> {
            if (!StringUtils.isEmpty(attribute)) {
                setValue.accept(Double.parseDouble(attribute));
            }
        });
    }

    /**
     * Apply a consumer to a retrieved diff attribute after transforming it to an Enum.
     *
     * @param diff      the global diff.
     * @param type      the type of diff attribute we want.
     * @param setValue  the consumer.
     * @param enumClass class of the Enum.
     */
    private <T extends Enum<T>> void doIfAttributeEnum(Diff diff, DiffAttributeTypeEnum type, Consumer<T> setValue, Class<T> enumClass) {
        doIfAttribute(diff, type, attribute -> {
            if (!StringUtils.isEmpty(attribute)) {
                setValue.accept(Enum.valueOf(enumClass, attribute));
            }
        });
    }

    /**
     * Apply a consumer to a retrieved diff attribute after transforming it to a Boolean.
     *
     * @param diff     the global diff.
     * @param type     the type of diff attribute we want.
     * @param setValue the consumer.
     */
    private void doIfAttributeBoolean(Diff diff, DiffAttributeTypeEnum type, Consumer<Boolean> setValue) {
        doIfAttribute(diff, type, attribute -> {
            if (!StringUtils.isEmpty(attribute)) {
                setValue.accept(Boolean.parseBoolean(attribute));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /** {@inheritDoc} */
    public void close() {
        if (!closed) {
            map.destroy();
            client.setTerminate(true);
            closed = true;
        }
    }
}
