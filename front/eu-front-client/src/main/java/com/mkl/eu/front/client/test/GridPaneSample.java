package com.mkl.eu.front.client.test;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GridPaneSample extends Application {

    private final ObservableList<Person> data =
            FXCollections.observableArrayList(
                    new Person("1", 1, 1),
                    new Person("2", 2, 2),
                    new Person("3", 5, 10),
                    new Person("4", 10, 5),
                    new Person("5", 6, 8));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Grid Pane Sample");
        stage.setWidth(450);
        stage.setHeight(550);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));

        Label label = new Label("Economic record sheet B - Income");
        label.setTextFill(Paint.valueOf("white"));
        label.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), null, null)));
        label.setFont(new Font("Arial", 20));

        grid.add(label, 0, 0, data.size() + 2, 1);

        label = new Label("#");
        grid.add(label, 0, 1);

        label = new Label("Turn number");
        grid.add(label, 1, 1);

        label = new Label("1");
        grid.add(label, 0, 2);

        label = new Label("Provinces income");
        grid.add(label, 1, 2);

        label = new Label("2");
        grid.add(label, 0, 3);

        label = new Label("Vassal provinces income");
        grid.add(label, 1, 3);

        for (int i = 0; i < data.size(); i++) {
            Person person = data.get(i);
            label = new Label(person.getFirstName());
            grid.add(label, i + 2, 1);
            label = new Label(person.getLastName() + "");
            grid.add(label, i + 2, 2);
            label = new Label(person.getEmail() + "");
            grid.add(label, i + 2, 3);
        }

        ((Group) scene.getRoot()).getChildren().addAll(grid);

        stage.setScene(scene);
        stage.show();
    }

    public static class Person {

        private String firstName;
        private Integer lastName;
        private Integer email;

        private Person(String fName, Integer lName, Integer email) {
            this.firstName = fName;
            this.lastName = lName;
            this.email = email;
        }

        /** @return the firstName. */
        public String getFirstName() {
            return firstName;
        }

        /** @param firstName the firstName to set. */
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        /** @return the lastName. */
        public Integer getLastName() {
            return lastName;
        }

        /** @param lastName the lastName to set. */
        public void setLastName(Integer lastName) {
            this.lastName = lastName;
        }

        /** @return the email. */
        public Integer getEmail() {
            return email;
        }

        /** @param email the email to set. */
        public void setEmail(Integer email) {
            this.email = email;
        }
    }
}