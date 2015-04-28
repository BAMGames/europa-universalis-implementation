package com.mkl.eu.front.client.main;

import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.vo.Game;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.board.Stack;
import com.mkl.eu.client.service.vo.country.Country;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.CounterTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.DiffListener;
import com.mkl.eu.front.client.map.InteractiveMap;
import com.mkl.eu.front.client.map.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;

/**
 * Java FX component which holds a PApplet.
 *
 * @author MKL.
 */
@org.springframework.stereotype.Component
public class Main extends JFrame implements DiffListener {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    /** Game service. */
    @Autowired
    private IGameService gameService;
    /** PApplet for the intercative map. */
    @Autowired
    private InteractiveMap map;
    /** Game displayed. */
    private Game game;

    /**
     * Initialize the component.
     */
    @PostConstruct
    public void init() {
        map.addDiffListener(this);
        game = gameService.loadGame(1L);
        MapConfiguration.setIdGame(game.getId());
        MapConfiguration.setVersionGame(game.getVersion());
        map.setGame(game);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 650));
        setBounds(0, 0, 1000, 600);
    }

    /**
     * Center a component.
     *
     * @param component to center.
     */
    public static void centerFrame(Component component) {
        // We retrieve the screen size.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        // And we move our component in the middle of the screen.
        component.setLocation((screen.width - component.getSize().width) / 2, (screen.height - component.getSize().height) / 2);
    }

    /**
     * Main to launche JavaFX.
     *
     * @param args no args.
     */
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        Main main = context.getBean(Main.class);

        centerFrame(main);
        main.pack();
        main.setVisible(true);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void update(DiffEvent event) {
        if (event.getIdGame().equals(game.getId())) {
            for (Diff diff : event.getDiffs()) {
                switch (diff.getTypeObject()) {
                    case COUNTER:
                        updateCounter(game, diff);
                        break;
                    case STACK:
                        break;
                    default:
                        break;
                }
            }
            game.setVersion(event.getNewVersion());
            MapConfiguration.setVersionGame(event.getNewVersion());

            map.update(event);
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
                break;
            case REMOVE:
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
        counter.setOwner(stack);
        stack.getCounters().add(counter);

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTER);
        if (attribute != null) {
            counter.setId(Long.parseLong(attribute.getValue()));
        } else {
            LOGGER.error("Missing counter id in counter add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.TYPE);
        if (attribute != null) {
            counter.setType(CounterTypeEnum.valueOf(attribute.getValue()));
        } else {
            LOGGER.error("Missing type in counter add event.");
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.COUNTRY);
        if (attribute != null) {
            String nameCountry = attribute.getValue();
            Country country = findFirst(game.getCountries(), country1 -> StringUtils.equals(nameCountry, country1.getName()));
            if (country == null) {
                country = new Country();
                country.setName(nameCountry);
            }
            counter.setCountry(country);
        } else {
            LOGGER.error("Missing country in counter add event.");
        }
    }

    /**
     * @return a mocked game.
     */
    private Game mockGame() {
        Game game = new Game();

        java.util.List<Stack> stacks = new ArrayList<>();
        Stack stack1 = new Stack();
        stack1.setProvince("Prypeć");
        Counter counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        stack1.getCounters().add(counter1);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince("Prypeć");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        Counter counter2 = new Counter();
        counter2.setCountry(new Country());
        counter2.getCountry().setName("FRA");
        counter2.setType(CounterTypeEnum.ARMY_MINUS);
        stack1.getCounters().add(counter2);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince("Prypeć");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        counter2 = new Counter();
        counter2.setCountry(new Country());
        counter2.getCountry().setName("FRA");
        counter2.setType(CounterTypeEnum.ARMY_MINUS);
        stack1.getCounters().add(counter2);
        Counter counter3 = new Counter();
        counter3.setCountry(new Country());
        counter3.getCountry().setName("FRA");
        counter3.setType(CounterTypeEnum.LAND_DETACHMENT);
        stack1.getCounters().add(counter3);
        stacks.add(stack1);

        stack1 = new Stack();
        stack1.setProvince("Languedoc");
        counter1 = new Counter();
        counter1.setCountry(new Country());
        counter1.getCountry().setName("FRA");
        counter1.setType(CounterTypeEnum.ARMY_PLUS);
        stack1.getCounters().add(counter1);
        stacks.add(stack1);

        game.setStacks(stacks);

        return game;
    }
}