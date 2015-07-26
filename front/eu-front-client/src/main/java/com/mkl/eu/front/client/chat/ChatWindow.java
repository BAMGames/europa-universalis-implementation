package com.mkl.eu.front.client.chat;

import com.mkl.eu.client.common.util.CommonUtil;
import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.chat.Message;
import com.mkl.eu.client.service.vo.chat.MessageDiff;
import com.mkl.eu.client.service.vo.chat.Room;
import com.mkl.eu.client.service.vo.country.PlayableCountry;
import com.mkl.eu.client.service.vo.diff.DiffResponse;
import com.mkl.eu.front.client.event.AbstractDiffListenerContainer;
import com.mkl.eu.front.client.event.DiffEvent;
import com.mkl.eu.front.client.main.GameConfiguration;
import com.mkl.eu.front.client.main.GlobalConfiguration;
import com.mkl.eu.front.client.vo.AuthentHolder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Window containing all the chats between players.
 *
 * @author MKL.
 */
@Component
@Scope(value = "prototype")
public class ChatWindow extends AbstractDiffListenerContainer {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWindow.class);
    /** Chat service. */
    @Autowired
    private IChatService chatService;
    /** Internationalisation. */
    @Autowired
    private MessageSource message;
    /** Configuration of the application. */
    @Autowired
    private GlobalConfiguration globalConfiguration;
    /** Component holding the authentication information. */
    @Autowired
    private AuthentHolder authentHolder;
    /** Chat containing rooms and global messages. */
    private Chat chat;
    /** Game configuration. */
    private GameConfiguration gameConfig;
    /** Stage of the window. */
    private Stage stage;
    /** The TabPane to update it later. */
    private TabPane tabPane;

    /**
     * Constructor.
     *
     * @param chat the chat to set.
     */
    public ChatWindow(Chat chat, GameConfiguration gameConfig) {
        this.chat = chat;
        this.gameConfig = gameConfig;
    }

    /**
     * Initialize the window.
     */
    @PostConstruct
    public void init() {
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);

        BorderPane border = new BorderPane();

        tabPane = new TabPane();

        tabPane.getTabs().add(createRoom(null, message.getMessage("chat.global", null, globalConfiguration.getLocale()),
                chat.getGlobalMessages(), null));
        for (Room room : chat.getRooms()) {
            tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries()));
        }

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
                                dialog.setTitle("chat.room.new.title");
                                dialog.setHeaderText("chat.room.new.header");
                                dialog.setContentText("chat.room.new.content");

                                Optional<String> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    LOGGER.info("Creation de room de nom : " + result.get());
                                    // TODO call chat service createRoom
                                }
                            }
                        }
                );

        Tab tabVisible = new Tab();
        Image img = null;
        try {
            img = new Image(new FileInputStream(new File("data/img/eye-icon.png")), 16, 16, true, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
                                List<RoomSelect> choices = chat.getRooms().stream().filter(room -> !room.isVisible()).map(RoomSelect::new).collect(Collectors.toList());
                                ChoiceDialog<RoomSelect> dialog = new ChoiceDialog<>(null, choices);
                                dialog.setTitle("chat.room.visible.title");
                                dialog.setHeaderText("chat.room.visible.header");
                                dialog.setContentText("chat.room.visible.content");

                                Optional<RoomSelect> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    LOGGER.info("On veut revoir la room : " + result.get().getRoom().getName());
                                    // TODO call chat service toggle
                                }
                            }
                        }
                );

        border.setCenter(tabPane);

        Scene scene = new Scene(border, 800, 600);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> hide());
    }

    private Tab createRoom(Long idRoom, String name, List<Message> messages, List<PlayableCountry> countries) {
        Tab tab = new Tab(name);
        String id = null;
        if (idRoom != null) {
            id = Long.toString(idRoom);
            tab.setOnCloseRequest(event1 -> {
                event1.consume();
                LOGGER.info("On veut fermer la room " + tab.getText());
                // TODO call chat service toggle
            });
        } else {
            tab.setClosable(false);
        }
        tab.setId(id);
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15, 12, 15, 12));
        ListView<Message> roomContent = new ListView<>();
        ObservableList<Message> messagesContent = FXCollections.observableArrayList(messages);
        Collections.reverse(messagesContent);
        roomContent.setItems(messagesContent);
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

                MenuItem itemKick = new MenuItem(message.getMessage("chat.room.kick", null, globalConfiguration.getLocale()));
                itemKick.setOnAction(event -> {
                    PlayableCountry country = cell.getItem();
                    if (country != null) {
                        LOGGER.info("On veut kick " + country.getName());
                        // TODO call chat service kick
                    }
                });
                MenuItem itemInvite = new MenuItem(message.getMessage("chat.room.invite", null, globalConfiguration.getLocale()));
                itemInvite.setOnAction(event -> {
                    PlayableCountry country = cell.getItem();
                    LOGGER.info("On veut invite");
                    // TODO call chat service invite
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
        Button submitBtn = new Button(message.getMessage("chat.submit", null, globalConfiguration.getLocale()));
        input.setOnKeyPressed(event -> {
            if (KeyCode.ENTER.equals(event.getCode())) {
                submitBtn.fire();
            }
        });
        submitBtn.setOnAction(event -> {
            String msg = input.getText();
            Request<SpeakInRoomRequest> request = new Request<>();
            authentHolder.fillAuthentInfo(request);
            gameConfig.fillGameInfo(request);
            gameConfig.fillChatInfo(request);
            request.setRequest(new SpeakInRoomRequest(idRoom, msg, gameConfig.getIdCountry()));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = chatService.speakInRoom(request);
                input.clear();
                DiffEvent diff = new DiffEvent(response, idGame);
                processDiffEvent(diff);
            } catch (Exception e) {
                LOGGER.error("Error when moving stack.", e);
                // TODO exception handling
            }
            input.requestFocus();
        });
        hbox.getChildren().add(submitBtn);

        layout.setBottom(hbox);

        tab.setOnSelectionChanged(event -> Platform.runLater(input::requestFocus));

        tab.setContent(layout);
        return tab;
    }

    /**
     * Show this popup.
     */
    public void show() {
        this.stage.show();
    }

    /**
     * Hide this popup.
     */
    public void hide() {
        this.stage.hide();
    }

    /**
     * @return Whether or not this popup is showing.
     */
    public boolean isShowing() {
        return this.stage.isShowing();
    }

    /**
     * Update the messages in the chat.
     *
     * @param messages new messages.
     */
    public synchronized void update(List<MessageDiff> messages, List<PlayableCountry> countries) {
        messages.forEach(message -> {
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
            if (tab != null) {
                if (tab.getContent() instanceof BorderPane) {
                    BorderPane border = ((BorderPane) tab.getContent());
                    if (border.getCenter() instanceof ListView) {
                        //noinspection unchecked
                        ListView<Message> listView = (ListView<Message>) border.getCenter();
                        listView.getItems().add(0, msg);
                    }
                }
            } else {
                LOGGER.error("New message in unknown tab.");
            }
        });
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
                Label label = new Label(item.getDateSent().format(DateTimeFormatter.ISO_LOCAL_TIME) + " <" + item.getSender().getName() + "> " + item.getMessage());
                Tooltip tooltip = new Tooltip(item.getDateSent().format(DateTimeFormatter.ISO_LOCAL_DATE));
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
     * Component for a Select box containing rooms.
     */
    private static class RoomSelect {
        /** Real room. */
        private Room room;

        /**
         * Constructor.
         *
         * @param room the room to set.
         */
        public RoomSelect(Room room) {
            this.room = room;
        }

        /** @return the room. */
        public Room getRoom() {
            return room;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return room.getName();
        }
    }
}
