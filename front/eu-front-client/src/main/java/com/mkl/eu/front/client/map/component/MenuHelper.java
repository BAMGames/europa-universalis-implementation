package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.MapConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.component.menu.ContextualMenuItem;
import com.mkl.eu.front.client.map.marker.BorderMarker;
import com.mkl.eu.front.client.map.marker.CounterMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.StackMarker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * @param province     where the contextual menu is.
     * @param boardService service for board manipulation.
     * @param container    container to call back when services are called.
     * @return a Contextual Menu for a Province.
     */
    public static ContextualMenu createMenuProvince(final IMapMarker province, IBoardService boardService,
                                                    IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(container.getMessage().getMessage("map.menu.province", null, container.getGlobalConfiguration().getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getMessage().getMessage(province.getId(), null, container.getGlobalConfiguration().getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        menu.addAllMenuItems(createAdminMenu(province, boardService, container));

        ContextualMenu neighbours = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("map.menu.province.neighbors", null, container.getGlobalConfiguration().getLocale()));
        for (final BorderMarker border : province.getNeighbours()) {
            StringBuilder label = new StringBuilder(container.getMessage().getMessage(border.getProvince().getId(), null, container.getGlobalConfiguration().getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(container.getMessage().getMessage("border." + border.getType().getCode(), null, container.getGlobalConfiguration().getLocale())).append(")");
            }
            neighbours.addMenuItem(ContextualMenuItem.createMenuLabel(label.toString()));
        }
        menu.addMenuItem(neighbours);
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

    private static List<ContextualMenuItem> createAdminMenu(IMapMarker province, IBoardService boardService, IMenuContainer container) {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu admin = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("map.menu.admin", null, container.getGlobalConfiguration().getLocale()));
        Map<CountryTypeEnum, ContextualMenu> countryMenus = new HashMap<>();
        for (CountryReferential country : container.getGlobalConfiguration().getReferential().getCountries()) {
            ContextualMenu countryTypeMenu = countryMenus.get(country.getType());
            if (countryTypeMenu == null) {
                countryTypeMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage(country.getType().name(), null, container.getGlobalConfiguration().getLocale()));
                admin.addMenuItem(countryTypeMenu);
                countryMenus.put(country.getType(), countryTypeMenu);
            }
            ContextualMenu countryMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage(country.getName(), null, container.getGlobalConfiguration().getLocale()));
            countryTypeMenu.addMenuItem(countryMenu);
            ContextualMenu landMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("Land army", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(landMenu);
            ContextualMenu navalMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("Naval army", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(navalMenu);
            ContextualMenu ecoMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("Economic", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(ecoMenu);
            ContextualMenu warMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("War", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(warMenu);
            ContextualMenu diploMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("Diplomacy", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(diploMenu);
            ContextualMenu trashMenu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("Others", null, container.getGlobalConfiguration().getLocale()));
            countryMenu.addMenuItem(trashMenu);

            for (CounterFaceTypeEnum counter : CounterFaceTypeEnum.values()) {
                ContextualMenu menu;

                if (CounterUtil.isLandArmy(counter)) {
                    menu = landMenu;
                } else if (CounterUtil.isNavalArmy(counter)) {
                    menu = navalMenu;
                } else if (CounterUtil.isManufacture(counter) || CounterUtil.isTradingFleet(counter)) {
                    menu = ecoMenu;
                } else if (counter == CounterFaceTypeEnum.OWN || counter == CounterFaceTypeEnum.CONTROL) {
                    menu = warMenu;
                } else if (counter == CounterFaceTypeEnum.DIPLOMACY || counter == CounterFaceTypeEnum.DIPLOMACY_WAR) {
                    menu = diploMenu;
                } else {
                    menu = trashMenu;
                }

                menu.addMenuItem(ContextualMenuItem.createMenuItem(counter.toString(), container.callServiceAsEvent(boardService::createCounter, () -> new CreateCounterRequest(province.getId(), counter, country.getName()), "Error when creating counter.")));
            }
        }

        menus.add(admin);
        menus.add(ContextualMenuItem.createMenuSeparator());
        return menus;
    }

    /**
     * Create a Contextual Menu for a Stack.
     *
     * @param stack        where the contextual menu is.
     * @param boardService service for board actions.
     * @param container    container to call back when services are called.
     * @return a Contextual Menu for a Stack.
     */
    public static ContextualMenu createMenuStack(final StackMarker stack, IBoardService boardService,
                                                 IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(container.getMessage().getMessage("map.menu.stack", null, container.getGlobalConfiguration().getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getMessage().getMessage("map.menu.stack", null, container.getGlobalConfiguration().getLocale())));
        String owner = UIUtil.getCountryName(stack.getStack().getCountry(), container.getMessage(), container.getGlobalConfiguration());
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getMessage().getMessage("map.menu.stack.info", new Object[]{owner, stack.getStack().getMove()}, container.getGlobalConfiguration().getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        ContextualMenu control = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("map.menu.stack.control", null, container.getGlobalConfiguration().getLocale()));
        List<String> countries = stack.getCounters().stream()
                .map(CounterMarker::getCountry)
                .distinct()
                .collect(Collectors.toList());
        for (String country : countries) {
            if (StringUtils.equals(country, stack.getStack().getCountry())) {
                continue;
            }
            String countryName = UIUtil.getCountryName(country, container.getMessage(), container.getGlobalConfiguration());
            control.addMenuItem(ContextualMenuItem.createMenuItem(countryName, container.callServiceAsEvent(boardService::takeStackControl, () -> new TakeStackControlRequest(stack.getId(), country), "Error when taking control of stack.")));
        }
        menu.addMenuItem(control);
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("map.menu.stack.move", null, container.getGlobalConfiguration().getLocale()));
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(container.getMessage().getMessage(border.getProvince().getId(), null, container.getGlobalConfiguration().getLocale()));
            if (border.getType() != null) {
                label.append(" (").append(container.getMessage().getMessage("border." + border.getType().getCode(), null, container.getGlobalConfiguration().getLocale())).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), container.callServiceAsEvent(boardService::moveStack, () -> new MoveStackRequest(stack.getId(), border.getProvince().getId()), "Error when moving stack.")));
        }
        menu.addMenuItem(move);
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getMessage().getMessage("map.menu.stack.end_move", null, container.getGlobalConfiguration().getLocale()),
                container.callServiceAsEvent(boardService::endMoveStack, () -> new EndMoveStackRequest(stack.getId()), "Error when ending movement of stack.")));

        return menu;
    }

    /**
     * Create a Contextual Menu for a Counter.
     *
     * @param counter      where the contextual menu is.
     * @param boardService service for board manipulation.
     * @param container    container to call back when services are called.
     * @return a Contextual Menu for a Counter.
     */
    public static ContextualMenu createMenuCounter(final CounterMarker counter, IBoardService boardService,
                                                   IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(container.getMessage().getMessage("map.menu.counter", null, container.getGlobalConfiguration().getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getMessage().getMessage("map.menu.counter", null, container.getGlobalConfiguration().getLocale())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getMessage().getMessage("map.menu.counter.disband", null, container.getGlobalConfiguration().getLocale()),
                container.callServiceAsEvent(boardService::removeCounter, () -> new RemoveCounterRequest(counter.getId()), "Error when deleting counter.")));

        return menu;
    }

    private static List<ContextualMenuItem> createGlobalMenu(IMenuContainer container) {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu menu = ContextualMenuItem.createMenuSubMenu(container.getMessage().getMessage("map.menu.map", null, container.getGlobalConfiguration().getLocale()));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getMessage().getMessage("map.menu.map.color", null, container.getGlobalConfiguration().getLocale()),
                event -> {
                    MapConfiguration.switchColor();
                }));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getMessage().getMessage("map.menu.map.moving_stack", null, container.getGlobalConfiguration().getLocale()),
                event -> {
                    MapConfiguration.switchStacksMovePhase();
                }));

        menus.add(menu);
        menus.add(ContextualMenuItem.createMenuSeparator());
        return menus;
    }
}
