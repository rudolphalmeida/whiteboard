package com.framelessboard;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    public HTTPConnect myHTTPConnect;
    @FXML
    Button loginButton;

    @FXML
    TextField userName;

    @FXML
    Label displayMessage;




    public void initialize() {

        EventHandler<javafx.event.ActionEvent> event = new EventHandler<javafx.event.ActionEvent>(){
            public void handle(ActionEvent e) {
                //displayMessage.setText("Waiting.");
                try {
                    switchToDraw();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        loginButton.setOnAction(event);

    }

    public void setMyHTTPConnect(HTTPConnect myHTTPConnect){
        System.out.println("Start");
        this.myHTTPConnect = myHTTPConnect;
    }




    @FXML
    private void switchToDraw() throws IOException {
        String field = userName.getText().trim();

        String myUserName = null;
        if (!field.equals("")) {
            if (field.matches("^[a-zA-Z0-9]*$")){
                myUserName = field;
                userName.clear();
            }
            else{
                userName.clear();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Illegal name.");
                alert.show();
                return;
            }

        }




        myHTTPConnect = new HTTPConnect();
        System.out.println(myUserName);
        myHTTPConnect.establishConnect(myUserName);


        //If cannot get the access token
        if (myHTTPConnect.token == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Error: Cannot get access token. Check Server");
            alert.show();
            return;
        }


        String state = myHTTPConnect.getUserState();
        System.out.println(state);
        while (!(state.equals("active"))){
            if (state.equals("rejected")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Manager reject your request");
                alert.show();
                return;
            }
            state = myHTTPConnect.getUserState();
            try{
                TimeUnit.SECONDS.sleep(1);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }



        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("draw.fxml"));
        Parent p = loader.load();
        DrawController drawController = loader.getController();
        myHTTPConnect.setArtist(drawController.getArtist());
        myHTTPConnect.setDrawController(drawController);
        drawController.setHttpConnect(myHTTPConnect);



        drawController.startUpdateThread();

        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(p);
        stage.setScene(scene);
        //stage.setScene(scene2);

    }


}
