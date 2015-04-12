package com.mkl.eu.front.component.menu;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 * Item on a Contextual Menu.
 * I'm sure I could have found something similar already coded, but wanted to see what I could do.
 *
 * @author MKL.
 */
public class ContextualMenuItem {
    /** Text of the item. */
    private String text;
    /** Type of the item. */
    private ContextualMenuItemTypeEnum type;
    /** Callback on click. */
    private EventHandler<ActionEvent> handler;

    /**
     * Constructor.
     *
     * @param text    the text to display.
     * @param type    the type of the item.
     * @param handler the handler to callback on click action.
     */
    public ContextualMenuItem(String text, ContextualMenuItemTypeEnum type, EventHandler<ActionEvent> handler) {
        this.text = text;
        this.type = type;
        this.handler = handler;
    }

    /**
     * Factory method for a common menu item.
     *
     * @param text    the text to display.
     * @param handler the handler to callback on click action.
     * @return the item created.
     */
    public static ContextualMenuItem createMenuItem(String text, EventHandler<ActionEvent> handler) {
        return new ContextualMenuItem(text, ContextualMenuItemTypeEnum.ITEM, handler);
    }

    /**
     * Factory method for a label menu item.
     *
     * @param text the text to display.
     * @return the item created.
     */
    public static ContextualMenuItem createMenuLabel(String text) {
        return new ContextualMenuItem(text, ContextualMenuItemTypeEnum.TEXT, null);
    }

    /**
     * Factory method for a separator menu item.
     *
     * @return the item created.
     */
    public static ContextualMenuItem createMenuSeparator() {
        return new ContextualMenuItem(null, ContextualMenuItemTypeEnum.SEPARATOR, null);
    }

    /** @return the text. */
    public String getText() {
        return text;
    }

    /** @return the type. */
    public ContextualMenuItemTypeEnum getType() {
        return type;
    }

    /** @return the handler. */
    public EventHandler<ActionEvent> getHandler() {
        return handler;
    }
}
