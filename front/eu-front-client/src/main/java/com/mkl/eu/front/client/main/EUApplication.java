package com.mkl.eu.front.client.main;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.vo.SimpleRequest;
import com.mkl.eu.client.service.service.IBoardService;
import com.mkl.eu.client.service.service.ITablesService;
import com.mkl.eu.client.service.service.board.FindGamesRequest;
import com.mkl.eu.client.service.vo.GameLight;
import com.mkl.eu.front.client.game.GamePopup;
import com.mkl.eu.front.client.log.JavaFxAppender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
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
    /** Board service. */
    private IBoardService boardService;
    /** Tables service. */
    private ITablesService tablesService;
    /** Internationalisation. */
    private MessageSource message;
    /** Configuration of the application. */
    private GlobalConfiguration globalConfiguration;
    /** List of game popups openend in order to spread a window close. */
    private List<GamePopup> gamePopups = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void start(Stage primaryStage) throws FunctionalException {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        boardService = context.getBean(IBoardService.class);
        tablesService = context.getBean(ITablesService.class);
        message = context.getBean(MessageSource.class);
        globalConfiguration = context.getBean(GlobalConfiguration.class);

//        primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));
        TabPane tabPane = new TabPane();

        Scene scene = new Scene(tabPane, 300, 250);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Tab tab = new Tab(message.getMessage("game.games", null, globalConfiguration.getLocale()));
        tab.setClosable(false);
        tab.setContent(grid);
        tabPane.getTabs().add(tab);

        primaryStage.setTitle(message.getMessage("game.title", null, globalConfiguration.getLocale()));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            gamePopups.forEach(gamePopup -> gamePopup.handle(event));
            Platform.exit();
        });
        primaryStage.show();

        SimpleRequest<FindGamesRequest> findGames = new SimpleRequest<>();
        findGames.setRequest(new FindGamesRequest());
        findGames.getRequest().setUsername("Sato");
        List<GameLight> games = boardService.findGames(findGames);


        globalConfiguration.setTables(tablesService.getTables());

        for (int i = 0; i < games.size(); i++) {
            GameLight game = games.get(i);

            Text turn = new Text(Integer.toString(game.getTurn()));
            grid.add(turn, 0, i, 1, 1);


            if (!StringUtils.isEmpty(game.getCountry())) {
                Text country = new Text(message.getMessage(game.getCountry(), null, globalConfiguration.getLocale()));
                grid.add(country, 1, i, 1, 1);
            }

            if (game.getUnreadMessages() != 0) {
                Text messages = new Text("(" + game.getUnreadMessages() + ")");
                grid.add(messages, 2, i, 1, 1);
            }

            Button loadBtn = new Button(message.getMessage("game.load", null, globalConfiguration.getLocale()));
            loadBtn.setOnAction(event -> {
                GamePopup popup = context.getBean(GamePopup.class, game.getId(), game.getIdCountry());
                gamePopups.add(popup);
            });
            grid.add(loadBtn, 3, i, 1, 1);
        }

        tab = new Tab(message.getMessage("game.log", null, globalConfiguration.getLocale()));
        tab.setClosable(false);
        TextArea text = JavaFxAppender.getText();
        tab.setContent(text);
        tabPane.getTabs().add(tab);
    }
}
