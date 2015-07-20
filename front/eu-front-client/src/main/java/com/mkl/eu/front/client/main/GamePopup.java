package com.mkl.eu.front.client.main;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.LoadGameRequest;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.map.InteractiveMap;
import com.mkl.eu.front.client.map.MapConfiguration;
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
    /** Spring application context. */
    private ApplicationContext context;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Board service. */
    @Autowired
    private IBoardService boardService;
    /** PApplet for the intercative map. */
    private InteractiveMap map;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Game displayed. */
    private Game game;
    /** Id to store between constructor and Spring init PostConstruct. */
    private Long id;
    /** List of JFrame opened by this popup in order to spread a close. */
    private java.util.List<JFrame> frames = new ArrayList<>();

    public GamePopup(Long id) {
        this.id = id;
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
        request.setRequest(new LoadGameRequest(id));

        game = boardService.loadGame(request);
    }

    /**
     * Initialize the interactive map.
     */
    private void initMap() {
        map = context.getBean(InteractiveMap.class, game);
        map.addDiffListener(this);

        MapConfiguration.setIdGame(game.getId());
        MapConfiguration.setVersionGame(game.getVersion());
    }

    /**
     * Initialize all the UIs on the popup.
     */
    private void initUI() {
        Stage dialog = new Stage();
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

            frames.add(frame);
        });
        grid.add(mapBtn, 0, 1, 1, 1);

        Button chatBtn = new Button(message.getMessage("game.popup.chat", null, globalConfiguration.getLocale()));
        grid.add(chatBtn, 0, 2, 1, 1);

        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();

        dialog.setOnCloseRequest(this);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void update(DiffEvent event) {
        if (event.getIdGame().equals(game.getId())) {
            for (Diff diff : event.getDiffs()) {
                if (MapConfiguration.getVersionGame() > diff.getVersionGame()) {
                    continue;
                }
                switch (diff.getTypeObject()) {
                    case COUNTER:
                        updateCounter(game, diff);
                        break;
                    case STACK:
                        updateStack(game, diff);
                        break;
                    default:
                        break;
                }
                map.update(diff);
            }

            game.setVersion(event.getNewVersion());
            MapConfiguration.setVersionGame(event.getNewVersion());
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

                attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.PROVINCE);
                if (attribute != null) {
                    stackTo.setProvince(attribute.getValue());
                } else {
                    LOGGER.error("Missing province in counter move event.");
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

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /** {@inheritDoc} */
    @Override
    public void handle(WindowEvent event) {
        frames.forEach(frame -> frame.dispatchEvent(new java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        map.destroy();
    }
}
