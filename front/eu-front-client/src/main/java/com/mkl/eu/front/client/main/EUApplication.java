package com.mkl.eu.front.client.main;

import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IGameService;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.service.game.FindGamesRequest;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.front.client.game.GamePopup;
import com.mkl.eu.front.client.log.JavaFxAppender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX component prior to loading a game (display list of games, configuration and so on).
 *
 * @author MKL.
 */
public class EUApplication extends Application {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(EUApplication.class);
    /** Application context. */
    private ApplicationContext context;
    /** Game service. */
    private IGameService gameService;
    /** Message. */
    private MessageSource message;
    /** Global configuration. */
    private GlobalConfiguration globalConfiguration;
    /** List of game popups opened in order to spread a window close. */
    private List<GamePopup> gamePopups = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void start(Stage primaryStage) {
        context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        gameService = context.getBean(IGameService.class);
        ITablesService tablesService = context.getBean(ITablesService.class);
        message = context.getBean(MessageSource.class);
        globalConfiguration = context.getBean(GlobalConfiguration.class);

        globalConfiguration.setTables(tablesService.getTables());
        globalConfiguration.setReferential(tablesService.getReferential());

//        primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createGameTab());
        tabPane.getTabs().add(createTabLog());

        Scene scene = new Scene(tabPane, 300, 250);
        primaryStage.setTitle(message.getMessage("game.title", null, globalConfiguration.getLocale()));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            gamePopups.forEach(gamePopup -> gamePopup.handle(event));
            Platform.exit();
            // Should not be necessary but processing 3 PSurfaceJOGL
            // doesn't close properly.
            System.exit(0);
        });
        primaryStage.show();
    }

    /**
     * @return the tab containing the list of games.
     */
    private Tab createGameTab() {
        TableView<GameLight> table = new TableView<>();
        table.setTableMenuButtonVisible(true);
        table.setPrefWidth(750);
        TableColumn<GameLight, String> column;

        column = new TableColumn<>(message.getMessage("game.games.id", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        column.setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("game.games.turn", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        column.setCellValueFactory(new PropertyValueFactory<>("turn"));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("game.games.status", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(message.getMessage("game.status." + param.getValue().getStatus(), null, globalConfiguration.getLocale())));
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("game.games.country", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.27));
        column.setCellValueFactory(param -> {
            StringBuilder sb = new StringBuilder(message.getMessage(param.getValue().getCountry(), null, globalConfiguration.getLocale()));
            if (param.getValue().getUnreadMessages() > 0) {
                sb.append(" (")
                        .append(param.getValue().getUnreadMessages())
                        .append(")");
            }
            return new ReadOnlyStringWrapper(sb.toString());
        });
        table.getColumns().add(column);

        column = new TableColumn<>(message.getMessage("game.games.actions", null, globalConfiguration.getLocale()));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        column.setCellValueFactory(new PropertyValueFactory<>("NONE"));
        Callback<TableColumn<GameLight, String>, TableCell<GameLight, String>> cellFactory = new Callback<TableColumn<GameLight, String>, TableCell<GameLight, String>>() {
            @Override
            public TableCell<GameLight, String> call(TableColumn<GameLight, String> param) {
                return new TableCell<GameLight, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Button btn = new Button(message.getMessage("game.games.load", null, globalConfiguration.getLocale()));
                            btn.setOnAction(event -> {
                                GameLight game = getTableView().getItems().get(getIndex());
                                GamePopup popup = context.getBean(GamePopup.class, game.getId(), game.getIdCountry());
                                gamePopups.add(popup);
                            });
                            setGraphic(btn);
                            setText(null);
                        }
                    }
                };
            }
        };
        column.setCellFactory(cellFactory);
        table.getColumns().add(column);

        List<GameLight> games = findGames();
        table.setItems(FXCollections.observableList(games));

        Tab tab = new Tab(message.getMessage("game.games", null, globalConfiguration.getLocale()));
        tab.setClosable(false);

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        Button button = new Button("reload");
        button.setOnAction(event -> table.setItems(FXCollections.observableList(findGames())));
        hBox.setAlignment(Pos.TOP_RIGHT);
        hBox.getChildren().add(button);
        vBox.getChildren().addAll(hBox, table);
        tab.setContent(vBox);

        return tab;
    }

    /**
     * @return the list of games.
     */
    private List<GameLight> findGames() {
        SimpleRequest<FindGamesRequest> findGames = new SimpleRequest<>();
        findGames.setRequest(new FindGamesRequest());
        findGames.getRequest().setUsername("Sato");
        try {
            return gameService.findGames(findGames);
        } catch (Exception e) {
            LOGGER.error("Impossible to find games.", e);
            UIUtil.showException(e, globalConfiguration, message);
            return new ArrayList<>();
        }
    }

    /**
     * @return the tab containing the logs.
     */
    private Tab createTabLog() {
        Tab tab = new Tab(message.getMessage("game.log", null, globalConfiguration.getLocale()));
        tab.setClosable(false);
        TextArea text = JavaFxAppender.getText();
        tab.setContent(text);

        return tab;
    }
}
