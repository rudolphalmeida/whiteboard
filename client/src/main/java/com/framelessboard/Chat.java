package com.framelessboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    @FXML
    private TextArea inputField;
    @FXML
    private Button sendButton;
    @FXML
    private VBox chatBox = new VBox();

    private List<Label> messages = new ArrayList<>();
    public String myName = "REY";


    public HTTPConnect httpConnect;

    public void setHttpConnect(HTTPConnect httpConnect){
        this.httpConnect = httpConnect;
    }


    public void initialize() {
        // TODO: Insert some getName here, so user knows what their name is.
        //setMyName();
        messages.add(new Label(""));
    }

    @FXML
    public void buttonPress(ActionEvent event) {
        String myMessage = inputField.getText().trim();
        if (!myMessage.equals("")) {
            //myMessage = myName + ": \n" + myMessage;
            sendMessage(myMessage);
                /*
                INSERT SEND CHAT MESSAGE TO OTHERS HERE.
                sendMessage(myMessage);
                */
            inputField.clear();
        }
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public void newMessage(String message) {
        messages.add(new Label(message));
    }

    // Displays messages "received" by themselves or by others.
    public void receiveMessage(String message) {
        newMessage(message);
        System.out.println(message);
        chatBox.getChildren().add(messages.get(messages.size() - 1));
    }

    // Sends messages to all other clients - would usually put "inputField.getText()" in argument.
    public void sendMessage(String message) {
        // TODO: message sending to clients.
        //myHTTPConnect.sendText(message);
        System.out.println(httpConnect.username);
    }
}
