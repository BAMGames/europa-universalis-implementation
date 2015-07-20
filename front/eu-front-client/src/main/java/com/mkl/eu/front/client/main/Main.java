package com.mkl.eu.front.client.main;

import javafx.application.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;

/**
 * Class launched by the jar.
 *
 * @author MKL.
 */
public class Main {

    /**
     * Center a component.
     * TODO move to a utility class.
     *
     * @param component to center.
     */
    public static void centerFrame(Component component) {
        // We retrieve the screen size.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        // And we move our component in the middle of the screen.
        component.setLocation((screen.width - component.getSize().width) / 2, (screen.height - component.getSize().height) / 2);
    }

    /**
     * Main to launch.
     *
     * @param args no args.
     */
    public static void main(String[] args) {
//        launchGame();
        launchEU();
    }

    /**
     * Launch a specific game.
     */
    private static void launchGame() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        GameFrame main = context.getBean(GameFrame.class);

        centerFrame(main);
        main.pack();
        main.setVisible(true);
    }

    /**
     * Launch the EU application.
     */
    private static void launchEU() {
        Application.launch(EUApplication.class);
    }
}
