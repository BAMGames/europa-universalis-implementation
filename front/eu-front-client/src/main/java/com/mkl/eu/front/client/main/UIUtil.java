package com.mkl.eu.front.client.main;

import com.mkl.eu.client.common.exception.FunctionalException;
import com.mkl.eu.client.common.exception.TechnicalException;
import com.mkl.eu.client.service.vo.board.Counter;
import com.mkl.eu.client.service.vo.enumeration.CounterFaceTypeEnum;
import com.mkl.eu.front.client.map.marker.MarkerUtils;
import com.mkl.eu.front.client.window.InteractiveMap;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for UI.
 *
 * @author MKL.
 */
public final class UIUtil {
    /**
     * Private constructor for Utility class.
     */
    private UIUtil() {

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
     * @param countryCode         code of the country.
     * @param globalConfiguration global configuration for internationalisation.
     * @return the country name to be displayed given the country code and the locale.
     */
    public static String getCountryName(String countryCode, GlobalConfiguration globalConfiguration) {
        if (StringUtils.isEmpty(countryCode)) {
            countryCode = "none";
        }

        return globalConfiguration.getMessage(countryCode);
    }

    /**
     * If in javaFx thread, just call the runnable.
     * If not, call it in a Platform::runLater.
     *
     * @param runnable the runnable to call.
     */
    public static void doInJavaFx(Runnable runnable) {
        // With Platform.runLater, this dialog can be displayed
        // even in non-javaFX component (the map)
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    /**
     * Display an alert dialog displaying the exception.
     *
     * @param ex                  the exception.
     * @param map                 possible interactive map.
     * @param globalConfiguration global configuration for internationalisation.
     */
    public static void showException(Exception ex, InteractiveMap map, GlobalConfiguration globalConfiguration) {
        doInJavaFx(() -> showExceptionInternal(ex, map, globalConfiguration));
    }

    /**
     * Display an alert dialog displaying the exception.
     *
     * @param ex                  the exception.
     * @param map                 possible interactive map.
     * @param globalConfiguration global configuration for internationalisation.
     */
    private static void showExceptionInternal(Exception ex, InteractiveMap map, GlobalConfiguration globalConfiguration) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(globalConfiguration.getMessage("exception.dialog.title"));
        alert.setHeaderText(globalConfiguration.getMessage("exception.dialog.header"));

        String code = "exception.unknown";
        Object[] params = null;
        if (ex instanceof TechnicalException) {
            code = ((TechnicalException) ex).getCode();
            params = ((TechnicalException) ex).getParams();
        } else if (ex instanceof FunctionalException) {
            code = ((FunctionalException) ex).getCode();
            params = ((FunctionalException) ex).getParams();
        }
        alert.setContentText(globalConfiguration.getMessage(code, params));


        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label(globalConfiguration.getMessage("exception.dialog.stacktrace"));

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        if (map != null) {
            alert.setOnHidden(event -> map.requestFocus());
        }

        alert.showAndWait();
    }

    /**
     * @param counter the counter.
     * @return an image of the counter.
     */
    public static ImageView getImage(Counter counter) {
        String path = MarkerUtils.getImagePath(counter);
        return getImage(path);
    }

    /**
     * @param country of the counter.
     * @param face    of the counter.
     * @return an image of the counter.
     */
    public static ImageView getImage(String country, CounterFaceTypeEnum face) {
        String path = MarkerUtils.getImagePath(country, face.name());
        return getImage(path);
    }

    /**
     * Internal method which retrieve an image from a path.
     *
     * @param path of the image.
     * @return an image from the path.
     */
    private static ImageView getImage(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            return new ImageView(new Image(fis, 40, 40, true, false));
        } catch (FileNotFoundException e) {
            // TODO TG-15 what to display if no image ?
            return null;
        }

    }
}
