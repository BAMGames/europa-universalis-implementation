package com.mkl.eu.front.client.main;

import com.mkl.eu.front.client.map.InteractiveMap;

import javax.swing.*;
import java.awt.*;

/**
 * Java FX component which holds a PApplet.
 *
 * @author MKL.
 */
public class Main extends JFrame {

    /**
     * Constructor.
     */
    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        InteractiveMap map = InteractiveMap.createInstance();
        add(map, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 650));
        setBounds(0, 0, 1000, 600);

        centerFrame(this);
        pack();
        setVisible(true);
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
        new Main();
    }
}