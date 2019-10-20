package com.framelessboard;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class LoginController {

    public HTTPConnect myHTTPConnect;
    @FXML
    Button loginButton;

    public void setMyHTTPConnect(HTTPConnect myHTTPConnect){
        this.myHTTPConnect = myHTTPConnect;
    }

    @FXML
    private void switchToDraw() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("draw.fxml"));
        loader.load();
        DrawController drawController = loader.getController();
        myHTTPConnect = new HTTPConnect();
        myHTTPConnect.establishConnect("abc");
        drawController.setMyHTTPConnect(myHTTPConnect);
        App.setRoot("draw");
    }
}
