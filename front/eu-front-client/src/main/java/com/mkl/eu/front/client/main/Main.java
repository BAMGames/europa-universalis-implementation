package com.mkl.eu.front.client.main;

import com.mkl.eu.front.client.map.InteractiveMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

/**
 * Java FX component which holds a PApplet.
 *
 * @author MKL.
 */
@org.springframework.stereotype.Component
public class Main extends JFrame {
    /** PApplet for the intercative map. */
    @Autowired
    private InteractiveMap map;

    /**
     * Initialize the component.
     */
    @PostConstruct
    public void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 650));
        setBounds(0, 0, 1000, 600);
    }

    /**
     * Center a component.
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
     * Main to launche JavaFX.
     *
     * @param args no args.
     */
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/mkl/eu/front/client/eu-front-client-applicationContext.xml");
        Main main = context.getBean(Main.class);

        centerFrame(main);
        main.pack();
        main.setVisible(true);
    }
}