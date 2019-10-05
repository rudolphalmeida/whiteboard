package com.framelessboard;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LoginController {

    @FXML
    Button loginButton;

    @FXML
    private void switchToDraw() throws IOException {
        App.setRoot("draw");
    }
}
