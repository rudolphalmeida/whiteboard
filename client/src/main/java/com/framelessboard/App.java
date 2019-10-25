package com.framelessboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadLoginFXML());
        stage.setTitle("FramelessBoard");
        stage.setScene(scene);
        stage.show();
    }

    static Parent loadLoginFXML() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login" + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
