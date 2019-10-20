package com.framelessboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Session {
    @FXML
    private VBox userList = new VBox();

    @FXML
    private Button kickButton;

    private List<Button> users = new ArrayList<>();

    public void initialize() {
        receiveUser("Me");
    }

    public void receiveUser(String name) {
        Button user = new Button(name);
        user.setUserData(name);
        user.setText(name);
        user.setId(name);
        users.add(user);
        userList.getChildren().add(user);
    }

    @FXML
    public void kickUser(ActionEvent event) {
        Button removed = (Button) event.getSource();
        removeUser((String) removed.getUserData());
        // TODO: Send kick to other clients here.
    }

    public void removeUser(String name) {
        users.remove(name);
        userList.getChildren().remove(userList.lookup("#" + name));
    }
}
