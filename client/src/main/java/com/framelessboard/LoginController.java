package com.framelessboard;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LoginController {

    public HTTPConnect myHTTPConnect;
    @FXML
    Button loginButton;

    public void setMyHTTPConnect(HTTPConnect myHTTPConnect){
        System.out.println("Start");
        this.myHTTPConnect = myHTTPConnect;
    }

    @FXML
    private void switchToDraw() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("draw.fxml"));
        Parent p = loader.load();

        DrawController drawController = loader.getController();
        myHTTPConnect = new HTTPConnect();
        myHTTPConnect.establishConnect("abc");
        myHTTPConnect.setArtist(drawController.getArtist());
        drawController.setMyHTTPConnect(myHTTPConnect);
        drawController.startUpdateThread();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(p);
        stage.setScene(scene);






    }
}
