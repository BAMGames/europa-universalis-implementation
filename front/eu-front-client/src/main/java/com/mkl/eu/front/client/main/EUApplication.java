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
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
    /** List of game popups opened in order to spread a window close. */
    private List<GamePopup> gamePopups = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void start(Stage primaryStage) {
        GlobalConfiguration.init();
        context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        gameService = context.getBean(IGameService.class);
        ITablesService tablesService = context.getBean(ITablesService.class);

        GlobalConfiguration.setTables(tablesService.getTables());
        GlobalConfiguration.setReferential(tablesService.getReferential());

//        primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

        TabPane verticalTab = new TabPane();
        verticalTab.setSide(Side.LEFT);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createGameTab(verticalTab));
        tabPane.getTabs().add(createTabLog());
        Tab tab = new Tab(GlobalConfiguration.getMessage("game.games.title"));
        tab.setClosable(false);
        tab.setContent(tabPane);
        verticalTab.getTabs().add(tab);

        Scene scene = new Scene(verticalTab, 800, 600);
        primaryStage.setTitle(GlobalConfiguration.getMessage("game.title"));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            gamePopups.forEach(GamePopup::close);
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
    private Tab createGameTab(TabPane verticalTab) {
        TableView<GameLight> table = new TableView<>();
        table.setTableMenuButtonVisible(true);
        table.setPrefWidth(750);
        TableColumn<GameLight, String> column;

        column = new TableColumn<>(GlobalConfiguration.getMessage("game.games.id"));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        column.setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().add(column);

        column = new TableColumn<>(GlobalConfiguration.getMessage("game.games.turn"));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        column.setCellValueFactory(new PropertyValueFactory<>("turn"));
        table.getColumns().add(column);

        column = new TableColumn<>(GlobalConfiguration.getMessage("game.games.status"));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(GlobalConfiguration.getMessage(param.getValue().getStatus())));
        table.getColumns().add(column);

        column = new TableColumn<>(GlobalConfiguration.getMessage("game.games.country"));
        column.prefWidthProperty().bind(table.widthProperty().multiply(0.27));
        column.setCellValueFactory(param -> {
            StringBuilder sb = new StringBuilder(GlobalConfiguration.getMessage(param.getValue().getCountry()));
            if (param.getValue().getUnreadMessages() > 0) {
                sb.append(" (")
                        .append(param.getValue().getUnreadMessages())
                        .append(")");
            }
            return new ReadOnlyStringWrapper(sb.toString());
        });
        table.getColumns().add(column);

        column = new TableColumn<>(GlobalConfiguration.getMessage("game.games.actions"));
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
                            Button btn = new Button(GlobalConfiguration.getMessage("game.games.load"));
                            btn.setOnAction(event -> {
                                GameLight game = getTableView().getItems().get(getIndex());
                                String title = GlobalConfiguration.getMessage("game.popup.title", game.getId(), game.getCountry());
                                Supplier<Tab> createTab = () -> {
                                    GamePopup popup = context.getBean(GamePopup.class, game.getId(), game.getIdCountry(), game.getCountry());
                                    gamePopups.add(popup);

                                    Tab tab = new Tab(title);
                                    tab.setContent(popup.getContent());
                                    verticalTab.getTabs().add(tab);
                                    tab.setOnClosed(event1 -> popup.close());
                                    return tab;
                                };
                                Tab tab = verticalTab.getTabs().stream()
                                        .filter(t -> StringUtils.equals(title, t.getText()))
                                        .findAny()
                                        .orElseGet(createTab);
                                verticalTab.getSelectionModel().select(tab);
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

        Tab tab = new Tab(GlobalConfiguration.getMessage("game.games"));
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
            UIUtil.showException(e, null);
            return new ArrayList<>();
        }
    }

    /**
     * @return the tab containing the logs.
     */
    private Tab createTabLog() {
        Tab tab = new Tab(GlobalConfiguration.getMessage("game.log"));
        tab.setClosable(false);
        TextArea text = JavaFxAppender.getText();
        tab.setContent(text);

        return tab;
    }
}
