package com.mkl.eu.front.client.window;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.chat.*;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.Diff;
import com.mkl.eu.client.service.vo.diff.DiffAttributes;
import com.mkl.eu.client.service.vo.enumeration.DiffAttributeTypeEnum;
import com.mkl.eu.front.client.event.AbstractDiffResponseListenerContainer;
import com.mkl.eu.front.client.event.IDiffListener;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mkl.eu.client.common.util.CommonUtil.findFirst;
import static java.time.temporal.ChronoField.*;

/**
 * Window containing all the chats between players.
 * TODO TG-128 add a status tab with the diffs and an error tab with the volatile logs.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class ChatWindow extends AbstractDiffResponseListenerContainer implements IDiffListener {
    public static final DateTimeFormatter MSG_DTF;

    static {
        MSG_DTF = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .toFormatter();
    }
    /** Chat service. */
    @Autowired
    private IChatService chatService;
    /** Game service. */
    @Autowired
    private IGameService gameService;
    /** Chat containing rooms and global messages. */
    private Chat chat;
    /** Countries in the game. */
    private List<PlayableCountry> countries;
    /** The TabPane to update it later. */
    private TabPane tabPane;

    /**
     * Constructor.
     *
     * @param chat       the chat to set.
     * @param countries  the countries to set.
     * @param gameConfig the gameConfig to set.
     */
    public ChatWindow(Chat chat, List<PlayableCountry> countries, GameConfiguration gameConfig) {
        super(gameConfig);
        this.chat = chat;
        this.countries = countries;
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

        tabPane.getTabs().add(createRoom(null, GlobalConfiguration.getMessage("chat.global"),
                chat.getGlobalMessages(), null, true));
        chat.getRooms().stream().filter(Room::isVisible).forEach(
                room -> tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries(), room.isPresent()))
        );

        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                            ListView<Message> messages = getCenterListView(newValue);
                            Long idRoom = null;
                            try {
                                idRoom = Long.parseLong(newValue.getId());
                            } catch (NumberFormatException e) {
                                // room is not private
                            }
                            if (messages != null && idRoom != null) {
                                long unreadMsg = messages.getItems().stream().filter(message1 -> message1.getDateRead() == null).count();

                                if (unreadMsg > 0) {
                                    Long id = idRoom;
                                    Long maxId = messages.getItems().stream().max((o1, o2) -> Long.compare(o1.getId(), o2.getId())).get().getId();
                                    callService(chatService::readRoom, () -> new ReadRoomRequest(id, maxId), "Error when reading room.", () -> {
                                        messages.getItems().stream().forEach(message1 -> message1.setDateRead(ZonedDateTime.now()));
                                        updateRoomName(newValue, id);
                                    }, null);
                                }
                            }
                        }
                );

        Tab tabNew = new Tab("+");
        tabNew.setClosable(false);
        tabPane.getTabs().add(tabNew);
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                            if (newValue == tabNew) {
                                tabPane.getSelectionModel()
                                        .select(oldValue);
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setTitle(GlobalConfiguration.getMessage("chat.room.new.title"));
                                dialog.setHeaderText(GlobalConfiguration.getMessage("chat.room.new.header"));
                                dialog.setContentText(GlobalConfiguration.getMessage("chat.room.new.content"));

                                Optional<String> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    callService(chatService::createRoom, () -> new CreateRoomRequest(result.get()), "Error when creating room.");
                                }
                            }
                        }
                );

        Tab tabVisible = new Tab();
        Image img = null;
        try {
            img = new Image(new FileInputStream(new File("data/img/eye-icon.png")), 16, 16, true, false);
        } catch (FileNotFoundException e) {
            LOGGER.error("Image located at data/img/eye-icon.png not found.", e);
        }
        ImageView eyeIcon = new ImageView(img);
        tabVisible.setGraphic(eyeIcon);
        tabVisible.setClosable(false);
        tabPane.getTabs().add(tabVisible);
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                            if (newValue == tabVisible) {
                                tabPane.getSelectionModel()
                                        .select(oldValue);
                                List<CustomSelect<Room>> choices = chat.getRooms().stream().filter(room -> !room.isVisible()).map(room1 -> new CustomSelect<>(room1, Room::getName)).collect(Collectors.toList());
                                ChoiceDialog<CustomSelect<Room>> dialog = new ChoiceDialog<>(null, choices);
                                dialog.setTitle(GlobalConfiguration.getMessage("chat.room.visible.title"));
                                dialog.setHeaderText(GlobalConfiguration.getMessage("chat.room.visible.header"));
                                dialog.setContentText(GlobalConfiguration.getMessage("chat.room.visible.content"));

                                Optional<CustomSelect<Room>> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    Room room = result.get().getObj();
                                    callService(chatService::toggleRoom, () -> new ToggleRoomRequest(room.getId(), true), "Error when toggling room.", () -> {
                                        room.setVisible(true);
                                        tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries(), room.isPresent()));
                                    }, null);
                                }
                            }
                        }
                );

        tabPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.U) {
                callService(gameService::updateGame, () -> null, "Error when updating game.");
            }
        });
    }

    /**
     * Create a tab containing a room. Can be global room or not.
     *
     * @param idRoom    id if the room if not global.
     * @param name      name of the room.
     * @param messages  messages in the room seen by the user.
     * @param countries countries in the room at the present time.
     * @param present   if the user is still present in the room. Always <code>true</code> in global room.
     * @return a tab containing a room. Can be global room or not.
     */
    private Tab createRoom(Long idRoom, String name, List<Message> messages, List<PlayableCountry> countries, boolean present) {
        Tab tab = new Tab(name);
        updateRoomName(tab, idRoom);
        String id = null;
        if (idRoom != null) {
            id = Long.toString(idRoom);
            tab.setOnCloseRequest(event1 -> {
                Room room = CommonUtil.findFirst(chat.getRooms(), room1 -> idRoom.equals(room1.getId()));
                // if it fails, we keep the tab open
                callService(chatService::toggleRoom, () -> new ToggleRoomRequest(room.getId(), false), "Error when toggling room.", () -> room.setVisible(false), event1::consume);
            });
        } else {
            tab.setClosable(false);
        }
        tab.setId(id);
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15, 12, 15, 12));
        ListView<Message> roomContent = new ListView<>();
        ObservableList<Message> messagesContent = FXCollections.observableArrayList(messages);
        roomContent.setItems(messagesContent);
        roomContent.scrollTo(messagesContent.size());
        roomContent.setCellFactory(param -> new MessageChat());
        roomContent.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        roomContent.setMaxWidth(Double.MAX_VALUE);
        roomContent.setOnKeyPressed(event -> {
            if (new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY).match(event)) {
                ObservableList<Message> selected = roomContent.getSelectionModel().getSelectedItems();
                StringBuilder sb = new StringBuilder();
                selected.forEach(message -> {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(message.getMessage());
                });
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(sb.toString());
                clipboard.setContent(content);
            }
        });
        layout.setCenter(roomContent);

        if (countries != null) {
//            Separator sep = new Separator();
//            sep.setOrientation(Orientation.VERTICAL);
//            layout.getChildren().add(sep);

            ListView<PlayableCountry> countriesView = new ListView<>();
            ObservableList<PlayableCountry> countriesContent = FXCollections.observableArrayList(countries);
            countriesView.setItems(countriesContent);

            countriesView.setCellFactory(param -> {
                ListCell<PlayableCountry> cell = new CountryCell();

                MenuItem itemKick = new MenuItem(GlobalConfiguration.getMessage("chat.room.kick"));
                itemKick.setOnAction(event -> {
                    PlayableCountry country = cell.getItem();
                    if (country != null) {
                        callService(chatService::inviteKickRoom, () -> new InviteKickRoomRequest(idRoom, false, cell.getItem().getId()), "Error when kicking in room.");
                    }
                });
                MenuItem itemInvite = new MenuItem(GlobalConfiguration.getMessage("chat.room.invite"));
                itemInvite.setOnAction(event -> {
                    List<CustomSelect<PlayableCountry>> choices = this.countries.stream().filter(playableCountry -> !countriesView.getItems().contains(playableCountry)).map(country -> new CustomSelect<>(country, PlayableCountry::getName)).collect(Collectors.toList());
                    ChoiceDialog<CustomSelect<PlayableCountry>> dialog = new ChoiceDialog<>(null, choices);
                    dialog.setTitle(GlobalConfiguration.getMessage("chat.room.invite.title"));
                    dialog.setHeaderText(GlobalConfiguration.getMessage("chat.room.invite.header"));
                    dialog.setContentText(GlobalConfiguration.getMessage("chat.room.invite.content"));

                    Optional<CustomSelect<PlayableCountry>> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        callService(chatService::inviteKickRoom, () -> new InviteKickRoomRequest(idRoom, true, result.get().getObj().getId()), "Error when inviting in room.");
                    }
                });
                ContextMenu menu = new ContextMenu(itemKick, itemInvite);
                cell.setContextMenu(menu);

                return cell;
            });
            layout.setRight(countriesView);
        }

        HBox hbox = new HBox();
        TextField input = new TextField();
        input.setMaxWidth(Double.MAX_VALUE);
        hbox.getChildren().add(input);
        Button submitBtn = new Button(GlobalConfiguration.getMessage("chat.submit"));
        submitBtn.setDisable(!present);
        input.setOnKeyPressed(event -> {
            if (KeyCode.ENTER.equals(event.getCode())) {
                submitBtn.fire();
            }
        });
        submitBtn.setOnAction(callServiceAsEvent(chatService::speakInRoom, () -> new SpeakInRoomRequest(idRoom, input.getText()), "Error when speaking in room.", () -> {
                input.clear();
                input.requestFocus();
        }, input::requestFocus));
        hbox.getChildren().add(submitBtn);

        layout.setBottom(hbox);

        tab.setOnSelectionChanged(event -> Platform.runLater(input::requestFocus));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Update the text header of the tab according the the room name and the number of unread messages.
     *
     * @param tab    to update.
     * @param idRoom id of the room.
     */
    private void updateRoomName(Tab tab, Long idRoom) {
        Room room = CommonUtil.findFirst(chat.getRooms(), room1 -> Objects.equals(idRoom, room1.getId()));
        if (tab != null && room != null) {
            String label = room.getName();
            long unreadMsg = room.getMessages().stream().filter(message1 -> message1.getDateRead() == null).count();
            if (unreadMsg > 0) {
                label = MessageFormat.format("{0} ({1})", room.getName(), unreadMsg);
            }

            tab.setText(label);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void updateMessages(List<MessageDiff> messages) {
        messages.forEach(message -> {
            if ((message.getIdRoom() == null && message.getId() > gameConfig.getMaxIdGlobalMessage())
                    || (message.getIdRoom() != null && message.getId() > gameConfig.getMaxIdMessage())) {
                Message msg = new Message();
                msg.setId(message.getId());
                msg.setMessage(message.getMessage());
                msg.setDateRead(message.getDateRead());
                msg.setDateSent(message.getDateSent());
                PlayableCountry country = CommonUtil.findFirst(countries, playableCountry -> message.getIdSender().equals(playableCountry.getId()));
                msg.setSender(country);

                final String idRoom;
                if (message.getIdRoom() != null) {
                    idRoom = Long.toString(message.getIdRoom());
                } else {
                    idRoom = null;
                }
                Tab tab = CommonUtil.findFirst(tabPane.getTabs(),
                        tab1 -> StringUtils.equals(idRoom, tab1.getId()));
                ListView<Message> listView = getCenterListView(tab);
                if (listView != null) {
                    listView.getItems().add(msg);
                    updateRoomName(tab, message.getIdRoom());
                    listView.scrollTo(listView.getItems().size());
                } else {
                    LOGGER.error("New message in unknown tab.");
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void update(Diff diff) {
        switch (diff.getTypeObject()) {
            case ROOM:
                updateRoom(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process a room diff event.
     *
     * @param diff involving a room.
     */
    private void updateRoom(Diff diff) {
        switch (diff.getType()) {
            case ADD:
                addRoom(diff);
                break;
            case LINK:
                inviteKickRoom(diff);
                break;
            default:
                break;
        }
    }

    /**
     * Process the add room diff event.
     *
     * @param diff involving a add room.
     */
    private void addRoom(Diff diff) {
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.NAME);
        if (attribute == null) {
            LOGGER.error("Missing name in room add event.");
            return;
        }

        Room room = CommonUtil.findFirst(chat.getRooms(), room1 -> StringUtils.equals(attribute.getValue(), room1.getName()));
        if (room == null) {
            LOGGER.error("Room does not exist.");
            return;
        }

        tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries(), room.isPresent()));
    }

    /**
     * Process the link room diff event.
     *
     * @param diff involving a add room.
     */
    private void inviteKickRoom(Diff diff) {
        boolean invite = false;
        Long idRoom = diff.getIdObject();
        DiffAttributes attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.INVITE);
        if (attribute != null) {
            invite = Boolean.parseBoolean(attribute.getValue());
        }

        attribute = findFirst(diff.getAttributes(), attr -> attr.getType() == DiffAttributeTypeEnum.ID_COUNTRY);
        if (attribute != null) {
            Long idCountry = Long.parseLong(attribute.getValue());
            Room room = CommonUtil.findFirst(chat.getRooms(), room1 -> idRoom.equals(room1.getId()));
            if (room == null) {
                LOGGER.error("Room '{}' not found.", idRoom);
                return;
            }
            PlayableCountry country = CommonUtil.findFirst(countries, playableCountry -> idCountry.equals(playableCountry.getId()));
            if (country == null) {
                LOGGER.error("Country '{}' not found.", idCountry);
                return;
            }

            Tab tab = CommonUtil.findFirst(tabPane.getTabs(),
                    tab1 -> StringUtils.equals(Long.toString(idRoom), tab1.getId()));

            if (idCountry.equals(gameConfig.getIdCountry())) {
                if (tab == null && invite) {
                    tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries(), room.isPresent()));
                    return;
                } else if (tab != null) {
                    Button submitBtn = getSubmitButton(tab);
                    if (submitBtn != null) {
                        submitBtn.setDisable(!invite);
                    }
                }
            }

            ListView<PlayableCountry> countryListView = getRightListView(tab);
            if (countryListView != null) {
                if (invite) {
                    countryListView.getItems().add(country);
                } else {
                    countryListView.getItems().remove(country);
                }
            }
        } else {
            LOGGER.error("Missing country id in counter add event.");
        }
    }

    /**
     * Retrieves the center ListView of a tab.
     *
     * @param tab the tab.
     * @return the center ListView of a tab.
     */
    private ListView<Message> getCenterListView(Tab tab) {
        ListView<Message> listView = null;

        if (tab.getContent() instanceof BorderPane) {
            BorderPane border = ((BorderPane) tab.getContent());
            if (border.getCenter() instanceof ListView) {
                //noinspection unchecked
                listView = (ListView<Message>) border.getCenter();
            }
        }

        return listView;
    }

    /**
     * Retrieves the right ListView of a tab.
     *
     * @param tab the tab.
     * @return the right ListView of a tab.
     */
    private ListView<PlayableCountry> getRightListView(Tab tab) {
        ListView<PlayableCountry> listView = null;

        if (tab.getContent() instanceof BorderPane) {
            BorderPane border = ((BorderPane) tab.getContent());
            if (border.getRight() instanceof ListView) {
                //noinspection unchecked
                listView = (ListView<PlayableCountry>) border.getRight();
            }
        }

        return listView;
    }

    /**
     * Retrieves the submit button at the bottom of the tab.
     *
     * @param tab the tab.
     * @return the submit button at the bottom of the tab.
     */
    private Button getSubmitButton(Tab tab) {
        Button btn = null;

        if (tab != null && tab.getContent() instanceof BorderPane) {
            BorderPane border = ((BorderPane) tab.getContent());
            if (border.getBottom() instanceof HBox) {
                HBox hbox = (HBox) border.getBottom();

                for (Node node : hbox.getChildren()) {
                    if (node instanceof Button) {
                        btn = (Button) node;
                        break;
                    }
                }
            }
        }

        return btn;
    }

    /**
     * Cell of a ListView for a Message (list of messages in the room).
     */
    private static class MessageChat extends ListCell<Message> {
        /** {@inheritDoc} */
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                Label label = new Label(item.getDateSent().format(MSG_DTF) + " <" + item.getSender().getName() + "> " + item.getMessage());
                Tooltip tooltip = new Tooltip(item.getDateSent().format(MSG_DTF));
                setTooltip(tooltip);
                setGraphic(label);
            } else {
                setTooltip(null);
                setGraphic(null);
            }
        }
    }

    /**
     * Cell of a ListView for a PlayableCountry (list of country present in the room).
     */
    private static class CountryCell extends ListCell<PlayableCountry> {

        /** {@inheritDoc} */
        @Override
        protected void updateItem(PlayableCountry item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                Label label = new Label(item.getName());
                setGraphic(label);
            } else {
                setGraphic(null);
            }
        }
    }

    /**
     * Component for a Select box whose toString() function is inadequate.
     */
    private static class CustomSelect<T> {
        /** Real object. */
        private T obj;
        /** Function to apply to the object to display the object. T -> String */
        private Function<T, String> function;

        /**
         * Constructor.
         *
         * @param obj      the obj to set.
         * @param function the function to set.
         */
        public CustomSelect(T obj, Function<T, String> function) {
            this.obj = obj;
            this.function = function;
        }

        /** @return the obj. */
        public T getObj() {
            return obj;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return function.apply(obj);
        }
    }
}
