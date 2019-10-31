package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.IBattleService;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.ISiegeService;
import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.CountryTypeEnum;
import com.mkl.eu.client.service.vo.enumeration.MovePhaseEnum;
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
     * @param battleService service for battles.
     * @param siegeService service for sieges.
     * @param container    container to call back when services are called.
     * @return a Contextual Menu for a Province.
     */
    public static ContextualMenu createMenuProvince(final IMapMarker province, IBoardService boardService, IBattleService battleService, ISiegeService siegeService,
                                                    IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(container.getGlobalConfiguration().getMessage("map.menu.province"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getGlobalConfiguration().getMessage(province.getId())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        menu.addAllMenuItems(createAdminMenu(province, boardService, container));

        ContextualMenu neighbours = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("map.menu.province.neighbors"));
        for (final BorderMarker border : province.getNeighbours()) {
            StringBuilder label = new StringBuilder(container.getGlobalConfiguration().getMessage(border.getProvince().getId()));
            if (border.getType() != null) {
                label.append(" (").append(container.getGlobalConfiguration().getMessage(border.getType())).append(")");
            }
            neighbours.addMenuItem(ContextualMenuItem.createMenuLabel(label.toString()));
        }
        menu.addMenuItem(neighbours);
        if (province.getStacks().stream()
                .anyMatch(stack -> stack.getStack().getMovePhase() == MovePhaseEnum.FIGHTING)) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.province.choose_battle"),
                    container.callServiceAsEvent(battleService::chooseBattle, () -> new ChooseProvinceRequest(province.getId()), "Error when choosing battle.")));
        }
        if (province.getStacks().stream()
                .anyMatch(stack -> stack.getStack().getMovePhase().isBesieging())) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.province.choose_siege"),
                    container.callServiceAsEvent(siegeService::chooseSiege, () -> new ChooseProvinceRequest(province.getId()), "Error when choosing siege.")));
        }

        return menu;
    }

    private static List<ContextualMenuItem> createAdminMenu(IMapMarker province, IBoardService boardService, IMenuContainer container) {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu admin = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("map.menu.admin"));
        Map<CountryTypeEnum, ContextualMenu> countryMenus = new HashMap<>();
        for (CountryReferential country : container.getGlobalConfiguration().getReferential().getCountries()) {
            ContextualMenu countryTypeMenu = countryMenus.get(country.getType());
            if (countryTypeMenu == null) {
                countryTypeMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage(country.getType()));
                admin.addMenuItem(countryTypeMenu);
                countryMenus.put(country.getType(), countryTypeMenu);
            }
            ContextualMenu countryMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage(country.getName()));
            countryTypeMenu.addMenuItem(countryMenu);
            ContextualMenu landMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("Land army"));
            countryMenu.addMenuItem(landMenu);
            ContextualMenu navalMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("Naval army"));
            countryMenu.addMenuItem(navalMenu);
            ContextualMenu ecoMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("Economic"));
            countryMenu.addMenuItem(ecoMenu);
            ContextualMenu warMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("War"));
            countryMenu.addMenuItem(warMenu);
            ContextualMenu diploMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("Diplomacy"));
            countryMenu.addMenuItem(diploMenu);
            ContextualMenu trashMenu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("Others"));
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

                menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage(counter), container.callServiceAsEvent(boardService::createCounter, () -> new CreateCounterRequest(province.getId(), counter, country.getName()), "Error when creating counter.")));
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
        ContextualMenu menu = new ContextualMenu(container.getGlobalConfiguration().getMessage("map.menu.stack"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getGlobalConfiguration().getMessage("map.menu.stack")));
        String owner = UIUtil.getCountryName(stack.getStack().getCountry(), container.getGlobalConfiguration());
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getGlobalConfiguration().getMessage("map.menu.stack.info", owner, stack.getStack().getMove())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        ContextualMenu control = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("map.menu.stack.control"));
        List<String> countries = stack.getCounters().stream()
                .map(CounterMarker::getCountry)
                .distinct()
                .collect(Collectors.toList());
        for (String country : countries) {
            if (StringUtils.equals(country, stack.getStack().getCountry())) {
                continue;
            }
            String countryName = UIUtil.getCountryName(country, container.getGlobalConfiguration());
            control.addMenuItem(ContextualMenuItem.createMenuItem(countryName, container.callServiceAsEvent(boardService::takeStackControl, () -> new TakeStackControlRequest(stack.getId(), country), "Error when taking control of stack.")));
        }
        menu.addMenuItem(control);
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("map.menu.stack.move"));
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(container.getGlobalConfiguration().getMessage(border.getProvince().getId()));
            if (border.getType() != null) {
                label.append(" (").append(container.getGlobalConfiguration().getMessage(border.getType())).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), container.callServiceAsEvent(boardService::moveStack, () -> new MoveStackRequest(stack.getId(), border.getProvince().getId()), "Error when moving stack.")));
        }
        menu.addMenuItem(move);
        if (stack.getStack().getMovePhase() == MovePhaseEnum.IS_MOVING) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.stack.end_move"),
                    container.callServiceAsEvent(boardService::endMoveStack, () -> new EndMoveStackRequest(stack.getId()), "Error when ending movement of stack.")));
        }

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
        ContextualMenu menu = new ContextualMenu(container.getGlobalConfiguration().getMessage("map.menu.counter"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(container.getGlobalConfiguration().getMessage("map.menu.counter")));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu(container));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.counter.disband"),
                container.callServiceAsEvent(boardService::removeCounter, () -> new RemoveCounterRequest(counter.getId()), "Error when deleting counter.")));

        return menu;
    }

    private static List<ContextualMenuItem> createGlobalMenu(IMenuContainer container) {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu menu = ContextualMenuItem.createMenuSubMenu(container.getGlobalConfiguration().getMessage("map.menu.map"));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.map.color"),
                event -> MapConfiguration.switchColor()));
        menu.addMenuItem(ContextualMenuItem.createMenuItem(container.getGlobalConfiguration().getMessage("map.menu.map.moving_stack"),
                event -> MapConfiguration.switchStacksMovePhase()));

        menus.add(menu);
        menus.add(ContextualMenuItem.createMenuSeparator());
        return menus;
    }
}
