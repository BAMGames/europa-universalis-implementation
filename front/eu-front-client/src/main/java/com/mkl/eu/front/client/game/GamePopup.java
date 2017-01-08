package com.mkl.eu.front.client.game;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.IEconomicService;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.service.chat.LoadRoomRequest;
import com.mkl.eu.client.service.service.eco.EconomicalSheetCountry;
import com.mkl.eu.client.service.service.eco.LoadEcoSheetsRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.eco.AdministrativeAction;
import com.mkl.eu.client.service.vo.enumeration.AdminActionStatusEnum;
import com.mkl.eu.client.service.vo.enumeration.AdminActionTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
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
import com.mkl.eu.front.client.socket.ClientSocket;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Popup used when loading a game. Holds the actions of opening other popups (map, chat, actions,...).
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class GamePopup implements IDiffListener, EventHandler<WindowEvent>, ApplicationContextAware {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GamePopup.class);
    /** Flag saying that the popup has already been closed. */
    private boolean closed;
    /** Spring application context. */
    private ApplicationContext context;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
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
    /** Window containing all the chat. */
    private ChatWindow chatWindow;
    /** Window containing the economics. */
    private EcoWindow ecoWindow;
    /** Window containing the administrative actions. */
    private AdminActionsWindow adminActionsWindow;
    /** Socket listening to server diff on this game. */
    private ClientSocket client;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Game displayed. */
    private Game game;
    /** Game config to store between constructor and Spring init PostConstruct and to spread to other UIs. */
    private GameConfiguration gameConfig;
    /** List of JFrame opened by this popup in order to spread a close. */
    private java.util.List<JFrame> frames = new ArrayList<>();

    public GamePopup(Long idGame, Long idCountry) {
        gameConfig = new GameConfiguration();
        gameConfig.setIdGame(idGame);
        gameConfig.setIdCountry(idCountry);
    }

    /**
     * Initialize the popup.
     *
     * @throws FunctionalException Functional exception.
     */
    @PostConstruct
    public void init() throws FunctionalException {
        initGame();
        initMap();
        initChat();
        initEcos();
        initUI();
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
     */
    private void initMap() {
        map = context.getBean(InteractiveMap.class, game, gameConfig);
        map.addDiffListener(this);
    }

    /**
     * Initialize the chat window.
     */
    private void initChat() {
        chatWindow = context.getBean(ChatWindow.class, game.getChat(), game.getCountries(), gameConfig);
        chatWindow.addDiffListener(this);
    }

    /**
     * Initialize the eco window.
     */
    private void initEcos() {
        ecoWindow = context.getBean(EcoWindow.class, game.getCountries(), gameConfig);
        ecoWindow.addDiffListener(this);
        adminActionsWindow = context.getBean(AdminActionsWindow.class, game, map.getMarkers(), gameConfig);
        adminActionsWindow.addDiffListener(this);
    }

    /**
     * Initialize all the UIs on the popup.
     */
    private void initUI() {
        Stage dialog = new Stage();
        dialog.setTitle(message.getMessage("game.popup.title", null, globalConfiguration.getLocale()));
        dialog.initModality(Modality.WINDOW_MODAL);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text text = new Text(Long.toString(game.getId()));
        grid.add(text, 0, 0, 1, 1);

        Button mapBtn = new Button(message.getMessage("game.popup.map", null, globalConfiguration.getLocale()));
        mapBtn.setOnAction(event -> {
            JFrame frame = new JFrame();

            frame.setLayout(new BorderLayout());
            frame.add(map, BorderLayout.CENTER);
            frame.setPreferredSize(new Dimension(1000, 650));
            frame.setBounds(0, 0, 1000, 600);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            frames.add(frame);
        });
        grid.add(mapBtn, 0, 1, 1, 1);

        Button chatBtn = new Button(message.getMessage("game.popup.chat", null, globalConfiguration.getLocale()));
        chatBtn.setOnAction(event -> {
            if (!chatWindow.isShowing()) {
                chatWindow.show();
            }
        });
        grid.add(chatBtn, 0, 2, 1, 1);

        Button ecoBtn = new Button(message.getMessage("game.popup.eco", null, globalConfiguration.getLocale()));
        ecoBtn.setOnAction(event -> {
            if (!ecoWindow.isShowing()) {
                ecoWindow.show();
            }
        });
        grid.add(ecoBtn, 0, 3, 1, 1);

        Button admActBtn = new Button(message.getMessage("game.popup.admin_actions", null, globalConfiguration.getLocale()));
        admActBtn.setOnAction(event -> {
            if (!adminActionsWindow.isShowing()) {
                adminActionsWindow.show();
            }
        });
        grid.add(admActBtn, 0, 4, 1, 1);

        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();

        dialog.setOnCloseRequest(this);
    }

    /** {@inheritDoc} */
    @Override
    public void handleException(ExceptionEvent event) {
        UIUtil.showException(event.getException(), globalConfiguration, message);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void update(DiffEvent event) {
        if (event.getIdGame().equals(game.getId())) {
            for (Diff diff : event.getResponse().getDiffs()) {
                if (gameConfig.getVersionGame() >= diff.getVersionGame()) {
                    continue;
                }
                switch (diff.getTypeObject()) {
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
                    default:
                        break;
                }
                map.update(diff);
                chatWindow.update(diff);
                ecoWindow.update(diff);
                adminActionsWindow.update(diff);
            }

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
            default:
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
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.STACK);
        if (attribute != null) {
            Long idStack = Long.parseLong(attribute.getValue());
            stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
            if (stack == null) {
                stack = new Stack();
                stack.setId(idStack);
                game.getStacks().add(stack);
            }
        } else {
            LOGGER.error("Missing stack id in counter add event.");
            stack = new Stack();
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
        } else {
            LOGGER.error("Missing country in counter add event.");
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
                break;
            case MOVE:
                moveStack(game, diff);
                break;
            case REMOVE:
                break;
            default:
                break;
        }
    }

    /**
     * Process the move stack diff event.
     *
     * @param game to update.
     * @param diff involving a add counter.
     */
    private void moveStack(Game game, Diff diff) {
        Stack stack;
        Long idStack = diff.getIdObject();
        stack = findFirst(game.getStacks(), stack1 -> idStack.equals(stack1.getId()));
        if (stack == null) {
            LOGGER.error("Missing stack in stack move event.");
            stack = new Stack();
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
            } catch (FunctionalException e) {
                LOGGER.error("Can't load economic sheets.", e);
            }

            return;
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
            default:
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

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(WindowEvent event) {
        if (!closed) {
            map.destroy();
            frames.forEach(frame -> frame.dispatchEvent(new java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING)));
            chatWindow.hide();
            ecoWindow.hide();
            adminActionsWindow.hide();
            client.setTerminate(true);
            closed = true;
        }
    }
}
