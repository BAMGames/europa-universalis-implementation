package com.mkl.eu.front.client.test;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TableViewSample extends Application {

    private final ObservableList<Person> data =
            FXCollections.observableArrayList(
                    new Person(1, 1, 1),
                    new Person(2, 2, 2),
                    new Person(3, 5, 10),
                    new Person(4, 10, 5),
                    new Person(5, 6, 8));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(800);
        stage.setHeight(600);

        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));

        List<List<String>> oups = new ArrayList<>();
        oups.add(new ArrayList<>());
        oups.get(0).add("#");
        oups.get(0).add("Turn number");
        oups.add(new ArrayList<>());
        oups.get(1).add("1");
        oups.get(1).add("Provinces income");
        oups.add(new ArrayList<>());
        oups.get(2).add("2");
        oups.get(2).add("Vassal provinces income");
        for (Person person : data) {
            oups.get(0).add(Integer.toString(person.getFirstName()));
            oups.get(1).add(Integer.toString(person.getLastName()));
            oups.get(2).add(Integer.toString(person.getEmail()));
        }
        ObservableList<List<String>> dataConverted = FXCollections.observableArrayList(oups);

        TableView<List<String>> table = new TableView<>();
//        table.setEditable(true);
        table.setTableMenuButtonVisible(true);
        table.setPrefWidth(750);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<List<String>, String> column1 = new TableColumn<>("");
        column1.setPrefWidth(30);
        column1.setSortable(false);
        column1.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));


        TableColumn<List<String>, String> column2 = new TableColumn<>("");
        column2.setPrefWidth(200);
        column2.setSortable(false);
        column2.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));


        TableColumn<List<String>, String> column3 = new TableColumn<>("");
        column3.setPrefWidth(50);
        column3.setSortable(false);
        column3.setVisible(false);
        column3.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(2)));


        TableColumn<List<String>, String> column4 = new TableColumn<>("");
        column4.setPrefWidth(50);
        column4.setSortable(false);
        column4.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(3)));


        TableColumn<List<String>, String> column5 = new TableColumn<>("");
        column5.setPrefWidth(50);
        column5.setSortable(false);
        column5.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(4)));

        TableColumn<List<String>, String> column6 = new TableColumn<>("");
        column6.setPrefWidth(50);
        column6.setSortable(false);
        column6.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(5)));

        TableColumn<List<String>, String> column7 = new TableColumn<>("");
        column7.setPrefWidth(50);
        column7.setSortable(false);
        column7.setVisible(false);
        column7.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(6)));

        table.setItems(dataConverted);
        table.getColumns().addAll(column1, column2, column3, column4, column5, column6, column7);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();

//        Pane header = (Pane) table.lookup("TableHeaderRow");
//        header.setVisible(false);
//        table.setLayoutY(-header.getHeight());
//        table.autosize();
    }

    public static class Person {

        private Integer firstName;
        private Integer lastName;
        private Integer email;

        private Person(Integer fName, Integer lName, Integer email) {
            this.firstName = fName;
            this.lastName = lName;
            this.email = email;
        }

        /** @return the firstName. */
        public Integer getFirstName() {
            return firstName;
        }

        /** @param firstName the firstName to set. */
        public void setFirstName(Integer firstName) {
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