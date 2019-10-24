package com.framelessboard;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrawController {

    public MenuItem menuNew;
    public MenuItem menuClose;
    public MenuItem menuSave;
    public MenuItem menuSaveAs;
    public MenuItem menuQuit;
    public MenuItem menuOpen;

    boolean isBusy = false;

    public HTTPConnect myHTTPConnect;

    public void setMyHTTPConnect(HTTPConnect myHTTPConnect){
        this.myHTTPConnect = myHTTPConnect;
    }

    public void startUpdateThread(){
        this.myHTTPConnect.updateThread.start();
    }

    private ArrayList<Double> pointBuffer = new ArrayList<Double>();


    @FXML
    private ToggleButton rectangleToggle;

    @FXML
    private ToggleButton circleToggle;

    @FXML
    private ToggleButton lineToggle;

    @FXML
    private ToggleButton ellipseToggle;

    @FXML
    private ToggleButton freeToggle;

    @FXML
    private ToggleButton eraserToggle;

    @FXML
    private ToggleButton textToggle;

    @FXML
    private ToggleButton fillToggle;

    @FXML
    private ColorPicker drawColor;

    @FXML
    private Slider strokeWidthInput;

    @FXML
    private TextField textToDrawInput;

    @FXML
    private CheckBox toggleFilling;

    @FXML
    private Canvas drawCanvas;

    private ToggleGroup objectGroup;

    private GraphicsContext gc;
    private Artist artist;

    public Artist getArtist(){
        return artist;
    }

    private WritableImage cleanSnapshot = null;

    enum DrawTool {
        TEXT, ERASER, FREEHAND, ELLIPSE, LINE, RECTANGLE, CIRCLE, FILL
    }

    private DrawTool currentTool;

    private void toggleCurrent(DrawTool tool) {
        if (currentTool == tool) {
            currentTool = null;
        } else {
            currentTool = tool;
        }
    }

    public void toggleDrawRectangle(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.RECTANGLE);
    }

    public void toggleDrawCircle(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.CIRCLE);
    }

    public void toggleDrawLine(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.LINE);
    }

    public void toggleDrawEllipse(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.ELLIPSE);
    }

    public void toggleDrawFree(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.FREEHAND);
    }

    public void toggleDrawEraser(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.ERASER);
    }

    public void toggleDrawFill(ActionEvent actionEvent) {
        textToDrawInput.setDisable(true);
        toggleCurrent(DrawTool.FILL);
    }

    public void toggleDrawText(ActionEvent actionEvent) {
        if (currentTool == DrawTool.TEXT) {
            textToDrawInput.setDisable(true);
            currentTool = null;
        } else {
            textToDrawInput.setDisable(false);
            currentTool = DrawTool.TEXT;
        }
    }

    /*
     * Displays an alert box asking for confirmation to save the current open file
     * */
    private boolean confirmSave() {
        if (!modifiedAfterLastSave) return false;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Save?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            save();
        } else return alert.getResult() == ButtonType.CANCEL;

        return false;
    }

    public void onNew(ActionEvent actionEvent) {
        if (!myHTTPConnect.isManager) return;
        if (confirmSave()) return;

        // Reset attributes
        file = null;
        modifiedAfterLastSave = false;
        drawColor.setValue(Color.WHITE);
        toggleFilling.setSelected(false);
        Toggle current = objectGroup.getSelectedToggle();
        if (current != null) {
            current.setSelected(false);
        }
        strokeWidthInput.setValue(strokeWidthInput.getMin());

        // Change the window title
        Stage stage = (Stage) drawCanvas.getScene().getWindow();
        stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "");

        // Reset canvas
        artist.clearCanvas();

        myHTTPConnect.deleteCanvas();
        myHTTPConnect.registerActive(myHTTPConnect.username);
        myHTTPConnect.postCanvas();
    }

    public void onOpen(ActionEvent actionEvent) {
        if (!myHTTPConnect.isManager) return;
        // Open a new workspace
        onNew(actionEvent);

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show file dialog
        file = fileChooser.showOpenDialog(drawCanvas.getScene().getWindow());

        if (file == null) {
            new Alert(Alert.AlertType.ERROR, "Error! Invalid selection...").showAndWait();
            return;
        }

        Image image = new Image(file.toURI().toString());
        CustomAction imageAction = new CustomAction("IMAGE", file);


        System.out.println("PostCanvas");
        myHTTPConnect.deleteCanvas();
        myHTTPConnect.registerActive(myHTTPConnect.username);
        myHTTPConnect.postCanvas();
        myHTTPConnect.sendCanvas(imageAction.getAction());

        //artist.drawImage(image);
    }

    public void onClose(ActionEvent actionEvent) {
        if (confirmSave()) return;

        file = null;
        Stage stage = (Stage) drawCanvas.getScene().getWindow();
        stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "");
        try {
            switchToLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (myHTTPConnect.isManager){
            myHTTPConnect.deleteCanvas();
        }
    }

    private boolean fileSelectError = false;

    private void save() {
        if (modifiedAfterLastSave && !fileSelectError) {
            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) drawCanvas.getWidth(), (int) drawCanvas.getHeight());
                    drawCanvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Error! Cannot write image...").showAndWait();
                }
                modifiedAfterLastSave = false;
                Stage stage = (Stage) drawCanvas.getScene().getWindow();
                stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "");
            } else {
                saveAs();
            }
        } else {
            // This happens if user clicked on Cancel in onSaveAs()
            // Break the loop here
            fileSelectError = false;
        }
    }

    private void saveAs() {
        if (modifiedAfterLastSave) {
            // Create file chooser with only PNG file selection
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);

            // Show file dialog
            file = fileChooser.showSaveDialog(drawCanvas.getScene().getWindow());

            // If the user clicked on cancel or there was an error in selecting
            // a file, we want to avoid going into an infinite loop of FileChoosers
            fileSelectError = file == null;

            save();
        }
    }

    public void onSave(ActionEvent actionEvent) {
        if (!myHTTPConnect.isManager) return;
        save();
    }

    public void onSaveAs(ActionEvent actionEvent) {
        if (!myHTTPConnect.isManager) return;
        saveAs();
    }

    public void onQuit(ActionEvent actionEvent) {
        if (confirmSave()) return;

        Stage stage = (Stage) drawCanvas.getScene().getWindow();
        stage.close();
    }

    // Coordinates for drawing
    private double startX, startY;
    private double endX, endY;

    private File file = null;
    private boolean modifiedAfterLastSave = false;

    @FXML
    private void initialize() {

        /// CHAT INITIALIZE PARTS ///
        messages.add(new Label(""));
        // TODO: Insert some getName here, so user knows what their name is.


        /// SESSION INITIALIZE PARTS ///
        //receiveUser("Me");
        // On exit handler
        // Reference: https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        drawCanvas.sceneProperty().addListener(((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                newScene.windowProperty().addListener(((observableWindow, oldWindow, newWindow) -> newWindow.setOnCloseRequest(event -> {
                    save();
                })));
            }
        }));

        // Keyboard shortcuts
        menuSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        menuNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        menuSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN));
        menuClose.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
        menuQuit.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN));
        menuOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));


        // ToggleGroup allows us to only select one object from the draw tools
        objectGroup = new ToggleGroup();
        textToggle.setToggleGroup(objectGroup);
        eraserToggle.setToggleGroup(objectGroup);
        freeToggle.setToggleGroup(objectGroup);
        ellipseToggle.setToggleGroup(objectGroup);
        lineToggle.setToggleGroup(objectGroup);
        rectangleToggle.setToggleGroup(objectGroup);
        circleToggle.setToggleGroup(objectGroup);
        fillToggle.setToggleGroup(objectGroup);

        // StrokeWidth
        strokeWidthInput.setMin(1.0);
        strokeWidthInput.setMax(20.0);

        // Get graphics context from canvas
        gc = drawCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.WHITE);
        gc.fillRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());

        // Artist
        artist = new Artist(drawCanvas, gc);

        cleanSnapshot = new WritableImage((int) drawCanvas.getWidth(), (int) drawCanvas.getHeight());

        // Event handlers for canvas
        // Click Event
        drawCanvas.setOnMouseClicked(event -> {
            if (currentTool == null) {
                return;
            }

            // Get coordinates of click
            double x = event.getX();
            double y = event.getY();
            CustomAction action = null;
            switch (currentTool) {
                case ELLIPSE:
                case LINE:
                case CIRCLE:
                case RECTANGLE:
                    break;
                case TEXT: {

                    //artist.drawText(textToDrawInput.getText(), drawColor.getValue(), x, y, strokeWidthInput.getValue());
                    action = new CustomAction("TEXT", drawColor.getValue().toString(), x, y,textToDrawInput.getText(), strokeWidthInput.getValue());
                    //artist.drawJSONText(action.getAction().getJSONObject("Action"));
                    myHTTPConnect.sendCanvas(action.getAction());

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case ERASER: {
                    //artist.erase(x, y, strokeWidthInput.getValue());


                    pointBuffer.add(x);
                    pointBuffer.add(y);
                    action = new CustomAction("ERASER", Color.WHITE.toString(), strokeWidthInput.getValue(), pointBuffer);
                    pointBuffer.clear();
                    myHTTPConnect.sendCanvas(action.getAction());



                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case FREEHAND: {
                    //artist.drawFreeHand(x, y, strokeWidthInput.getValue(), drawColor.getValue());

                    pointBuffer.add(x);
                    pointBuffer.add(y);

                    action = new CustomAction("FREEHAND", drawColor.getValue().toString(), strokeWidthInput.getValue(), pointBuffer);
                    pointBuffer.clear();
                    myHTTPConnect.sendCanvas(action.getAction());

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case FILL: {
                    //artist.floodFill(x, y, drawColor.getValue());
                    action = new CustomAction("FILL", drawColor.getValue().toString(), x, y);
                    myHTTPConnect.sendCanvas((action.getAction()));



                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
            }
        });

        // Drag event
        // Begin drag
        drawCanvas.setOnMousePressed(event -> {
            if (currentTool == null) return;

            startX = endX = event.getX();
            startY = endY = event.getY();

            cleanSnapshot = drawCanvas.snapshot(null, cleanSnapshot);
            //Stop Update
//            try {
//                myHTTPConnect.updateThread.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }


        });

        drawCanvas.setOnMouseDragged(event -> {
            if (currentTool == null) return;
            CustomAction action = null;
            switch (currentTool) {
                case ERASER:
                    artist.erase(event.getX(), event.getY(), strokeWidthInput.getValue());
                    //action = new CustomAction("ERASER", event.getX(), event.getY(), strokeWidthInput.getValue());
                    //System.out.println(action.getAction());
                    pointBuffer.add(event.getX());
                    pointBuffer.add(event.getY());
                    break;
                case FREEHAND:
                    artist.drawFreeHand(event.getX(), event.getY(), strokeWidthInput.getValue(), drawColor.getValue());
                    //action = new CustomAction("FREEHAND", drawColor.getValue().toString(), event.getX(), event.getY(), strokeWidthInput.getValue());
                    //System.out.println(action.getAction());
                    pointBuffer.add(event.getX());
                    pointBuffer.add(event.getY());

                    break;
                case LINE: {
                    // Restore previous snapshot
                    gc.drawImage(cleanSnapshot, 0, 0);
                    Color color = drawColor.getValue();
                    // Transparent version of the color
                    Color transparentColor = new Color(
                            color.getRed(), color.getGreen(), color.getBlue(), 0.2
                    );

                    endX = event.getX();
                    endY = event.getY();

                    artist.drawLine(startX, startY, endX, endY, 1.0, transparentColor);

                    break;
                }
                case RECTANGLE: {
                    // Save actual drag start coordinates
                    double tempX = startX;
                    double tempY = startY;

                    // Go back to clean snapshot
                    gc.drawImage(cleanSnapshot, 0, 0);
                    Color color = drawColor.getValue();
                    // Get a slightly transparent version of the current color
                    Color transparentColor = new Color(
                            color.getRed(), color.getGreen(), color.getBlue(), 0.2
                    );

                    // Draw a rectangle
                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();
                    artist.drawRectangle(startX, startY, endX - startX, endY - startY, transparentColor, false, 1.0);

                    // Restore original start co-ordinates
                    startX = tempX;
                    startY = tempY;
                    break;
                }
                case ELLIPSE: {
                    // Same process as rectangle

                    double tempX = startX;
                    double tempY = startY;

                    gc.drawImage(cleanSnapshot, 0, 0);
                    Color color = drawColor.getValue();
                    Color transparentColor = new Color(
                            color.getRed(), color.getGreen(), color.getBlue(), 0.2
                    );

                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();
                    artist.drawEllipse(startX, startY, endX - startX, endY - startY, transparentColor, false, 1.0);
                    startX = tempX;
                    startY = tempY;
                    break;
                }
                case CIRCLE: {
                    // Same as rectangle but no need of restoring coordinates here

                    gc.drawImage(cleanSnapshot, 0, 0);
                    Color color = drawColor.getValue();
                    Color transparentColor = new Color(
                            color.getRed(), color.getGreen(), color.getBlue(), 0.2
                    );

                    endX = event.getX();
                    endY = event.getY();
                    double radius = distance(startX, startY, endX, endY);

                    artist.drawCircle(startX, startY, radius, transparentColor, false, 1.0);
                    break;
                }
            }
        });

        // drag exited
        drawCanvas.setOnMouseReleased(event -> {
            if (currentTool == null) return;
            CustomAction action = null;
            switch (currentTool) {
                case TEXT:
                case ERASER:
                    action = new CustomAction("ERASER", Color.WHITE.toString(), strokeWidthInput.getValue(), pointBuffer);
                    pointBuffer.clear();
                    myHTTPConnect.sendCanvas(action.getAction());
                case FREEHAND:
                    action = new CustomAction("FREEHAND", drawColor.getValue().toString(), strokeWidthInput.getValue(), pointBuffer);
                    pointBuffer.clear();
                    myHTTPConnect.sendCanvas(action.getAction());
                case FILL:
                    break;
                case CIRCLE: {
                    double outerX = event.getX();
                    double outerY = event.getY();
                    double radius = distance(startX, startY, outerX, outerY);

                    //artist.drawCircle(startX, startY, radius, drawColor.getValue(), toggleFilling.isSelected(), strokeWidthInput.getValue());
                    action = new CustomAction("CIRCLE", drawColor.getValue().toString(),startX, startY, radius, toggleFilling.isSelected(), strokeWidthInput.getValue());
                    myHTTPConnect.sendCanvas(action.getAction());

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case LINE: {
                    //artist.drawLine(startX, startY, event.getX(), event.getY(), strokeWidthInput.getValue(), drawColor.getValue());
                    action = new CustomAction("LINE", drawColor.getValue().toString(), startX, startY, endX, endY, strokeWidthInput.getValue());
                    myHTTPConnect.sendCanvas(action.getAction());

                    startX = endX = startY = endY = 0.0;

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case RECTANGLE: {
                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();

                    //artist.drawRectangle(startX, startY, endX - startX, endY - startY, drawColor.getValue(), toggleFilling.isSelected(), strokeWidthInput.getValue());
                    action = new CustomAction("RECTANGLE", drawColor.getValue().toString(), startX, startY, endX - startX, endY - startY, strokeWidthInput.getValue(), toggleFilling.isSelected());
                    //System.out.println(action.getAction());
                    myHTTPConnect.sendCanvas(action.getAction());

                    startX = endX = startY = endY = 0.0; // Reset start and end

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
                case ELLIPSE: {
                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();

                    //artist.drawEllipse(startX, startY, endX - startX, endY - startY, drawColor.getValue(), toggleFilling.isSelected(), strokeWidthInput.getValue());
                    action = new CustomAction("ELLIPSE", drawColor.getValue().toString(), startX, startY, endX - startX, endY - startY, toggleFilling.isSelected(), strokeWidthInput.getValue());
                    myHTTPConnect.putCanvas(action.getAction());
                    startX = endX = startY = endY = 0.0; // Reset start and end

                    modifiedAfterLastSave = true;
                    Stage stage = (Stage) drawCanvas.getScene().getWindow();
                    stage.setTitle("FramelessBoard - " + (file != null ? file : "") + "*");
                    break;
                }
            }
            //myHTTPConnect.updateThread.notify();
        });
    }

    /*
     * Shape primitives like rectangle, ellipse require start to be the top-left
     * corner and end to be the bottom-right corner. However depending on how the
     * user drags the mouse they might end up at incorrect corners. This function
     * moves start and end to their proper position while maintaining the same polygon
     * */
    private void alignStartEnd() {
        // When the user drags towards top-left
        if (startX > endX && startY > endY) {
            // Swap start with end
            double temp = startX;
            startX = endX;
            endX = temp;

            temp = startY;
            startY = endY;
            endY = temp;
        } else if (startX < endX && startY > endY) { // When user drags top-right
            // Swap only the y co-ordinate
            double temp = endY;
            endY = startY;
            startY = temp;
        } else if (startX > endX && startY < endY) { // When user drags bottom-left
            // Swap only the x co-ordinate
            double temp = endX;
            endX = startX;
            startX = temp;
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double ac = Math.abs(y2 - y1);
        double cb = Math.abs(x2 - x1);

        return Math.hypot(ac, cb);
    }



    @FXML
    public void switchToLogin() throws IOException {
        myHTTPConnect.stopUpdateThread();
        System.out.println("Stop Update");
        App.setRoot("login");
    }


    ////// SESSION CONTROLS //////

    @FXML
    private VBox userList = new VBox();

    @FXML
    private Button kickButton;

    private List<Button> users = new ArrayList<>();



    public void clearUsers(){
        userList.getChildren().clear();
        users.clear();
    }

    public void receiveUser(String name) {
        Button user = new Button(name);
        user.setUserData(name);
        user.setText(name);
        user.setId(name);
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                kickUser(e);
            }
        };
        user.setOnAction(event);
        users.add(user);
        userList.getChildren().add(user);
    }

    @FXML
    public void kickUser(ActionEvent event) {
        Button removed = (Button) event.getSource();
        String temp = (String) removed.getUserData();
        if ((temp.equals(myHTTPConnect.username))){
            return;
        }
        removeUser((String) removed.getUserData());
        // TODO: Send kick to other clients here.
        myHTTPConnect.deleteActive((String) removed.getUserData());
    }

    public void removeUser(String name) {
        users.remove(name);
        userList.getChildren().remove(userList.lookup("#" + name));
    }

    public void waitingUser(String name){
        isBusy = true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, name + ": Can I join?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();


        if (alert.getResult() == ButtonType.YES) {
            //Accept
            myHTTPConnect.registerActive(name);
        } else if (alert.getResult() == ButtonType.NO){
            myHTTPConnect.deleteWaitingUsers(name);
        }
        isBusy = false;
    }

    ////// CHAT CONTROL //////

    @FXML
    private TextArea inputField;
    @FXML
    private Button sendButton;
    @FXML
    private VBox chatBox = new VBox();

    private List<Label> messages = new ArrayList<>();

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
        //System.out.println(myHTTPConnect.username);
        myHTTPConnect.sendText(message);
    }
}
