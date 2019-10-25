package com.mkl.eu.front.client.main;

import com.mkl.eu.front.client.game.GameFrame;
import javafx.application.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Class launched by the jar.
 *
 * @author MKL.
 */
public class Main {

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

        UIUtil.centerFrame(main);
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
