package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.IGameAdminService;
import com.mkl.eu.client.service.service.board.MoveStackRequest;
import com.mkl.eu.client.service.vo.board.CounterForCreation;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.event.ExceptionEvent;
import com.mkl.eu.front.client.event.IDiffListenerContainer;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.component.menu.ContextualMenuItem;
import com.mkl.eu.front.client.map.marker.BorderMarker;
import com.mkl.eu.front.client.map.marker.CounterMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.StackMarker;
import com.mkl.eu.front.client.vo.AuthentHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Utility class for menus.
 *
 * @author MKL.
 */
public final class MenuHelper {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuHelper.class);

    /**
     * No constructor for utility class.
     */
    private MenuHelper() {

    }

    /**
     * Create a Contextual Menu for a Province.
     *
     * @param province            where the contextual menu is.
     * @param message             for internationalisation.
     * @param globalConfiguration Configuration of the application.
     * @param gameConfig          Game configuration.
     * @param authentHolder       Component holding the authentication information.
     * @param gameAdminService    service for game administration.
     * @param container           container to call back when services are called.
     * @return a Contextual Menu for a Province.
     */
    public static ContextualMenu createMenuProvince(final IMapMarker province, MessageSource message, GlobalConfiguration globalConfiguration,
                                                    GameConfiguration gameConfig, AuthentHolder authentHolder, IGameAdminService gameAdminService,
                                                    IDiffListenerContainer container) {
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.province", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(province.getId()));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        ContextualMenu neighbours = ContextualMenuItem.createMenuSubMenu(message.getMessage("map.menu.neighbors", null, globalConfiguration.getLocale()));
        for (final BorderMarker border : province.getNeighbours()) {
            StringBuilder label = new StringBuilder(message.getMessage(border.getProvince().getId(), null, globalConfiguration.getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(message.getMessage("border." + border.getType().getCode(), null, globalConfiguration.getLocale())).append(")");
            }
            neighbours.addMenuItem(ContextualMenuItem.createMenuLabel(label.toString()));
        }
        menu.addMenuItem(neighbours);
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A+", event -> createStack(CounterFaceTypeEnum.ARMY_PLUS, province, gameConfig, gameAdminService, container)));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add A-", event -> createStack(CounterFaceTypeEnum.ARMY_MINUS, province, gameConfig, gameAdminService, container)));
        menu.addMenuItem(ContextualMenuItem.createMenuItem("Add D", event -> createStack(CounterFaceTypeEnum.LAND_DETACHMENT, province, gameConfig, gameAdminService, container)));
        ContextualMenu subMenu1 = ContextualMenuItem.createMenuSubMenu("Test");
        ContextualMenu subMenu2 = ContextualMenuItem.createMenuSubMenu("Sous menu !");
        subMenu2.addMenuItem(ContextualMenuItem.createMenuItem("action", null));
        subMenu2.addMenuItem(ContextualMenuItem.createMenuLabel("text"));
        subMenu2.addMenuItem(ContextualMenuItem.createMenuItem("reaction", null));
        subMenu1.addMenuItem(subMenu2);
        subMenu1.addMenuItem(ContextualMenuItem.createMenuItem("Amen", null));
        subMenu1.addMenuItem(ContextualMenuItem.createMenuLabel("Upide"));
        ContextualMenu subMenu3 = ContextualMenuItem.createMenuSubMenu("Un autre");
        subMenu3.addMenuItem(ContextualMenuItem.createMenuLabel("OK"));
        subMenu3.addMenuItem(ContextualMenuItem.createMenuItem("Ou pas", null));
        subMenu1.addMenuItem(subMenu3);
        subMenu1.addMenuItem(ContextualMenuItem.createMenuItem("Icule", null));
        menu.addMenuItem(subMenu1);

        return menu;
    }

    /**
     * Creates a French stack of one counter on the province.
     *
     * @param type             of the counter to create.
     * @param province         where the stack should be created.
     * @param gameConfig       Game configuration.
     * @param gameAdminService service for game administration.
     * @param container        container to call back when services are called.
     */
    private static void createStack(CounterFaceTypeEnum type, IMapMarker province, GameConfiguration gameConfig,
                                    IGameAdminService gameAdminService, IDiffListenerContainer container) {
        CounterForCreation counter = new CounterForCreation();
        counter.setCountry("france");
        counter.setType(type);
        Long idGame = gameConfig.getIdGame();
        try {
            DiffResponse response = gameAdminService.createCounter(idGame, gameConfig.getVersionGame(), counter, province.getId());
            DiffEvent event = new DiffEvent(response, idGame);
            container.processDiffEvent(event);
        } catch (Exception e) {
            LOGGER.error("Error when creating counter.", e);

            container.processExceptionEvent(new ExceptionEvent(e));
        }
    }

    /**
     * Create a Contextual Menu for a Stack.
     *
     * @param stack               where the contextual menu is.
     * @param message             for internationalisation.
     * @param globalConfiguration Configuration of the application.
     * @param gameConfig          Game configuration.
     * @param authentHolder       Component holding the authentication information.
     * @param boardService        service for board actions.
     * @param container           container to call back when services are called.
     * @return a Contextual Menu for a Stack.
     */
    public static ContextualMenu createMenuStack(final StackMarker stack, MessageSource message, GlobalConfiguration globalConfiguration,
                                                 GameConfiguration gameConfig, AuthentHolder authentHolder, IBoardService boardService,
                                                 IDiffListenerContainer container) {
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.stack", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(message.getMessage("map.menu.stack", null, globalConfiguration.getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu(message.getMessage("map.menu.move", null, globalConfiguration.getLocale()));
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(message.getMessage(border.getProvince().getId(), null, globalConfiguration.getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(message.getMessage("border." + border.getType().getCode(), null, globalConfiguration.getLocale())).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), event -> {
                Long idGame = gameConfig.getIdGame();
                try {
                    Request<MoveStackRequest> request = new Request<>();
                    authentHolder.fillAuthentInfo(request);
                    gameConfig.fillGameInfo(request);
                    gameConfig.fillChatInfo(request);
                    request.setRequest(new MoveStackRequest(stack.getId(), border.getProvince().getId()));
                    DiffResponse response = boardService.moveStack(request);
                    DiffEvent diff = new DiffEvent(response, idGame);
                    container.processDiffEvent(diff);
                } catch (Exception e) {
                    LOGGER.error("Error when moving stack.", e);

                    container.processExceptionEvent(new ExceptionEvent(e));
                }
            }));
        }
        menu.addMenuItem(move);

        return menu;
    }

    /**
     * Create a Contextual Menu for a Counter.
     *
     * @param counter             where the contextual menu is.
     * @param message             for internationalisation.
     * @param globalConfiguration Configuration of the application.
     * @param gameConfig          Game configuration.
     * @param authentHolder       Component holding the authentication information.
     * @param gameAdminService    service for game administration.
     * @param container           container to call back when services are called.
     * @return a Contextual Menu for a Counter.
     */
    public static ContextualMenu createMenuCounter(final CounterMarker counter, MessageSource message, GlobalConfiguration globalConfiguration,
                                                   GameConfiguration gameConfig, AuthentHolder authentHolder, IGameAdminService gameAdminService,
                                                   IDiffListenerContainer container) {
        ContextualMenu menu = new ContextualMenu(message.getMessage("map.menu.counter", null, globalConfiguration.getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(message.getMessage("map.menu.counter", null, globalConfiguration.getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addMenuItem(ContextualMenuItem.createMenuItem(message.getMessage("map.menu.disband", null, globalConfiguration.getLocale()), event -> {
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = gameAdminService.removeCounter(idGame, gameConfig.getVersionGame(),
                        counter.getId());
                DiffEvent diff = new DiffEvent(response, idGame);
                container.processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when moving stack.", e);

                container.processExceptionEvent(new ExceptionEvent(e));
            }
        }));

        return menu;
    }
}
