package com.mkl.eu.front.client.map.component;

import com.mkl.eu.client.service.service.board.*;
import com.mkl.eu.client.service.service.military.ChooseProvinceRequest;
import com.mkl.eu.client.service.service.military.LandLootingRequest;
import com.mkl.eu.client.service.util.CounterUtil;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.*;
import com.mkl.eu.client.service.vo.ref.country.CountryReferential;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.main.UIUtil;
import com.mkl.eu.front.client.map.MapConfiguration;
import com.mkl.eu.front.client.map.component.menu.ContextualMenu;
import com.mkl.eu.front.client.map.component.menu.ContextualMenuItem;
import com.mkl.eu.front.client.map.marker.BorderMarker;
import com.mkl.eu.front.client.map.marker.CounterMarker;
import com.mkl.eu.front.client.map.marker.IMapMarker;
import com.mkl.eu.front.client.map.marker.StackMarker;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for menus.
 *
 * @author MKL.
 */
public final class MenuHelper {

    /**
     * No constructor for utility class.
     */
    private MenuHelper() {

    }

    /**
     * Create a Contextual Menu for a Province.
     *
     * @param province  where the contextual menu is.
     * @param container container to call back when services are called.
     * @return a Contextual Menu for a Province.
     */
    public static ContextualMenu createMenuProvince(final IMapMarker province, IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(GlobalConfiguration.getMessage("map.menu.province"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(GlobalConfiguration.getMessage(province.getId())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu());
        menu.addAllMenuItems(createAdminMenu(province, container));

        ContextualMenu neighbours = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("map.menu.province.neighbors"));
        for (final BorderMarker border : province.getNeighbours()) {
            StringBuilder label = new StringBuilder(GlobalConfiguration.getMessage(border.getProvince().getId()));
            if (border.getType() != null) {
                label.append(" (").append(GlobalConfiguration.getMessage(border.getType())).append(")");
            }
            neighbours.addMenuItem(ContextualMenuItem.createMenuLabel(label.toString()));
        }
        menu.addMenuItem(neighbours);
        if (container.getGame().getStatus() == GameStatusEnum.MILITARY_BATTLES && province.getStacks().stream()
                .anyMatch(stack -> stack.getStack().getMovePhase() == MovePhaseEnum.FIGHTING)) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.province.choose_battle"),
                    container.callServiceAsEvent(container.getBattleService()::chooseBattle, () -> new ChooseProvinceRequest(province.getId()), "Error when choosing battle.")));
        }
        if (container.getGame().getStatus() == GameStatusEnum.MILITARY_SIEGES && province.getStacks().stream()
                .anyMatch(stack -> stack.getStack().getMovePhase().isBesieging())) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.province.choose_siege"),
                    container.callServiceAsEvent(container.getSiegeService()::chooseSiege, () -> new ChooseProvinceRequest(province.getId()), "Error when choosing siege.")));
        }

        return menu;
    }

    private static List<ContextualMenuItem> createAdminMenu(IMapMarker province, IMenuContainer container) {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu admin = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("map.menu.admin"));
        Map<CountryTypeEnum, ContextualMenu> countryMenus = new HashMap<>();
        for (CountryReferential country : GlobalConfiguration.getReferential().getCountries()) {
            ContextualMenu countryTypeMenu = countryMenus.get(country.getType());
            if (countryTypeMenu == null) {
                countryTypeMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage(country.getType()));
                admin.addMenuItem(countryTypeMenu);
                countryMenus.put(country.getType(), countryTypeMenu);
            }
            ContextualMenu countryMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage(country.getName()));
            countryTypeMenu.addMenuItem(countryMenu);
            ContextualMenu landMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Land army"));
            countryMenu.addMenuItem(landMenu);
            ContextualMenu navalMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Naval army"));
            countryMenu.addMenuItem(navalMenu);
            ContextualMenu ecoMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Economic"));
            countryMenu.addMenuItem(ecoMenu);
            ContextualMenu warMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("War"));
            countryMenu.addMenuItem(warMenu);
            ContextualMenu diplomaticMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Diplomacy"));
            countryMenu.addMenuItem(diplomaticMenu);
            ContextualMenu leaderMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Leader"));
            countryMenu.addMenuItem(leaderMenu);
            ContextualMenu trashMenu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("Others"));
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
                    menu = diplomaticMenu;
                } else if (CounterUtil.isLeader(counter)) {
                    continue;
                } else {
                    menu = trashMenu;
                }

                menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage(counter), container.callServiceAsEvent(container.getBoardService()::createCounter, () -> new CreateCounterRequest(province.getId(), counter, country.getName(), null), "Error when creating counter.")));
            }

            GlobalConfiguration.getTables().getLeaders().stream()
                    .filter(leader -> StringUtils.equals(leader.getCountry(), country.getName()))
                    .forEach(leader -> leaderMenu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage(leader.getCode()),
                            container.callServiceAsEvent(container.getBoardService()::createCounter,
                                    () -> new CreateCounterRequest(province.getId(), CounterUtil.getLeaderType(leader), country.getName(), leader.getCode()),
                                    "Error when creating counter."))));
        }

        menus.add(admin);
        menus.add(ContextualMenuItem.createMenuSeparator());
        return menus;
    }

    /**
     * Create a Contextual Menu for a Stack.
     *
     * @param stack     where the contextual menu is.
     * @param container container to call back when services are called.
     * @return a Contextual Menu for a Stack.
     */
    public static ContextualMenu createMenuStack(final StackMarker stack, IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(GlobalConfiguration.getMessage("map.menu.stack"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(GlobalConfiguration.getMessage("map.menu.stack")));
        String owner = UIUtil.getCountryName(stack.getStack().getCountry());
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(GlobalConfiguration.getMessage("map.menu.stack.info", owner, stack.getStack().getMove())));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu());
        ContextualMenu control = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("map.menu.stack.control"));
        List<String> countries = stack.getCounters().stream()
                .map(CounterMarker::getCountry)
                .distinct()
                .collect(Collectors.toList());
        for (String country : countries) {
            if (StringUtils.equals(country, stack.getStack().getCountry())) {
                continue;
            }
            String countryName = UIUtil.getCountryName(country);
            control.addMenuItem(ContextualMenuItem.createMenuItem(countryName, container.callServiceAsEvent(container.getBoardService()::takeStackControl, () -> new TakeStackControlRequest(stack.getId(), country), "Error when taking control of stack.")));
        }
        menu.addMenuItem(control);
        ContextualMenu move = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("map.menu.stack.move"));
        for (final BorderMarker border : stack.getProvince().getNeighbours()) {
            StringBuilder label = new StringBuilder(GlobalConfiguration.getMessage(border.getProvince().getId()));
            if (border.getType() != null) {
                label.append(" (").append(GlobalConfiguration.getMessage(border.getType())).append(")");
            }
            move.addMenuItem(ContextualMenuItem.createMenuItem(label.toString(), container.callServiceAsEvent(container.getBoardService()::moveStack, () -> new MoveStackRequest(stack.getId(), border.getProvince().getId()), "Error when moving stack.")));
        }
        menu.addMenuItem(move);
        if (container.getGame().getStatus() == GameStatusEnum.MILITARY_MOVE && stack.getStack().getMovePhase() == MovePhaseEnum.IS_MOVING) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.stack.end_move"),
                    container.callServiceAsEvent(container.getBoardService()::endMoveStack, () -> new EndMoveStackRequest(stack.getId()), "Error when ending movement of stack.")));
        }
        if (container.getGame().getStatus() == GameStatusEnum.REDEPLOYMENT) {
            if (stack.getStack().getMovePhase() != MovePhaseEnum.LOOTING && stack.getStack().getMovePhase() != MovePhaseEnum.LOOTING_BESIEGING) {
                menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.stack.loot"),
                        container.callServiceAsEvent(container.getInterPhaseService()::landLooting, () -> new LandLootingRequest(stack.getId(), LandLootTypeEnum.PILLAGE), "Error when looting the province with stack.")));
                if (stack.getProvince().getStacks().stream().flatMap(s -> s.getCounters().stream())
                        .anyMatch(counter -> counter.getType() == CounterFaceTypeEnum.TRADING_POST_MINUS || counter.getType() == CounterFaceTypeEnum.TRADING_POST_PLUS)) {
                    menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.stack.burn_tp"),
                            container.callServiceAsEvent(container.getInterPhaseService()::landLooting, () -> new LandLootingRequest(stack.getId(), LandLootTypeEnum.BURN_TP), "Error when burning the trading post with stack.")));
                }
            }
            menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.stack.redeploy"),
                    container.notifyClientAsEvent(DiffTypeObjectEnum.REDEPLOY, createDiffAttribute(DiffAttributeTypeEnum.STACK, stack.getId()))));
        }

        return menu;
    }

    /**
     * Create a Contextual Menu for a Counter.
     *
     * @param counter   where the contextual menu is.
     * @param container container to call back when services are called.
     * @return a Contextual Menu for a Counter.
     */
    public static ContextualMenu createMenuCounter(final CounterMarker counter, IMenuContainer container) {
        ContextualMenu menu = new ContextualMenu(GlobalConfiguration.getMessage("map.menu.counter"));
        menu.addMenuItem(ContextualMenuItem.createMenuLabel(GlobalConfiguration.getMessage("map.menu.counter")));
        menu.addMenuItem(ContextualMenuItem.createMenuSeparator());
        menu.addAllMenuItems(createGlobalMenu());
        if (CounterUtil.isLeader(counter.getType())) {
            menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.counter.leader_lead"), container.callServiceAsEvent(container.getBoardService()::takeStackControl, () -> new TakeStackControlRequest(counter.getOwner().getId(), counter.getId()), "Error when setting leader to stack.")));
        }
        menu.addMenuItem(ContextualMenuItem.createMenuItem(GlobalConfiguration.getMessage("map.menu.counter.disband"),
                container.callServiceAsEvent(container.getBoardService()::removeCounter, () -> new RemoveCounterRequest(counter.getId()), "Error when deleting counter.")));

        return menu;
    }

    private static List<ContextualMenuItem> createGlobalMenu() {
        List<ContextualMenuItem> menus = new ArrayList<>();
        ContextualMenu menu = ContextualMenuItem.createMenuSubMenu(GlobalConfiguration.getMessage("map.menu.map"));
        menu.addMenuItem(toogleMapConfiguration(MapConfiguration::isWithColor, MapConfiguration::switchColor, "map.menu.map.color"));
        menu.addMenuItem(toogleMapConfiguration(MapConfiguration::isStacksMovePhase, MapConfiguration::switchStacksMovePhase, "map.menu.map.moving_stack"));
        menu.addMenuItem(toogleMapConfiguration(MapConfiguration::isHideArmies, MapConfiguration::switchHideArmies, "map.menu.map.hide_army"));
        menu.addMenuItem(toogleMapConfiguration(MapConfiguration::isHideInfrastructures, MapConfiguration::switchHideInfrastructures, "map.menu.map.hide_infra"));
        menu.addMenuItem(toogleMapConfiguration(MapConfiguration::isHideAll, MapConfiguration::switchHideAll, "map.menu.map.hide_all"));

        menus.add(menu);
        menus.add(ContextualMenuItem.createMenuSeparator());
        return menus;
    }

    private static ContextualMenuItem toogleMapConfiguration(Supplier<Boolean> toggled, Runnable toggle, String label) {
        Consumer<ContextualMenuItem> setMenuLabel = menuITem -> menuITem.setText((toggled.get() ? GlobalConfiguration.getMessage("map.menu.map.checked") : "") + GlobalConfiguration.getMessage(label));
        ContextualMenuItem item = ContextualMenuItem.createMenuItem(null,
                event -> {
                    toggle.run();
                    setMenuLabel.accept((ContextualMenuItem) event.getSource());
                });
        setMenuLabel.accept(item);

        return item;
    }

    /**
     * Create a diff attribute.
     *
     * @param type  of the diff attribute.
     * @param value of the diff attribute.
     * @return the diff attribute.
     */
    private static DiffAttributes createDiffAttribute(DiffAttributeTypeEnum type, Long value) {
        return createDiffAttribute(type, value != null ? value.toString() : "");
    }

    /**
     * Create a diff attribute.
     *
     * @param type  of the diff attribute.
     * @param value of the diff attribute.
     * @return the diff attribute.
     */
    private static DiffAttributes createDiffAttribute(DiffAttributeTypeEnum type, String value) {
        DiffAttributes attribute = new DiffAttributes();
        attribute.setType(type);
        attribute.setValue(value);
        return attribute;
    }
}
