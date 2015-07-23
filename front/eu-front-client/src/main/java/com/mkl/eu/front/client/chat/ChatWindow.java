package com.mkl.eu.front.client.chat;

import com.mkl.eu.client.common.vo.Request;
import com.mkl.eu.client.service.service.IChatService;
import com.mkl.eu.client.service.service.chat.SpeakInRoomRequest;
import com.mkl.eu.client.service.vo.chat.Chat;
import com.mkl.eu.client.service.vo.chat.Message;
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
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        TabPane tabPane = new TabPane();

        tabPane.getTabs().add(createRoom(null, message.getMessage("chat.global", null, globalConfiguration.getLocale()),
                chat.getGlobalMessages(), null));
        for (Room room : chat.getRooms()) {
            tabPane.getTabs().add(createRoom(room.getId(), room.getName(), room.getMessages(), room.getCountries()));
        }

        border.setCenter(tabPane);

        Scene scene = new Scene(border, 800, 600);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> hide());
    }

    private Tab createRoom(Long idRoom, String name, List<Message> messages, List<PlayableCountry> countries) {
        Tab tab = new Tab(name);
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15, 12, 15, 12));
        ListView<Message> roomContent = new ListView<>();
        ObservableList<Message> messagesContent = FXCollections.observableArrayList(messages);
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
            countriesView.setCellFactory(param -> new CountryCell());
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
            request.setRequest(new SpeakInRoomRequest(idRoom, msg, gameConfig.getIdCountry()));
            Long idGame = gameConfig.getIdGame();
            try {
                DiffResponse response = chatService.speakInRoom(request);
                input.clear();
                DiffEvent diff = new DiffEvent(response.getDiffs(), idGame, response.getVersionGame());
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

    private static class MessageChat extends ListCell<Message> {
        /** {@inheritDoc} */
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                Label label = new Label(item.getDateSent().format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + " <" + item.getSender().getName() + "> " + item.getMessage());
                setGraphic(label);
            } else {
                setGraphic(null);
            }
        }
    }

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
}
