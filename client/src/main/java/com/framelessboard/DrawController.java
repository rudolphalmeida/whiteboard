package com.framelessboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Path;

public class DrawController {

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
    private ColorPicker drawColor;

    @FXML
    private Slider strokeWidthInput;

    @FXML
    private TextField textToDrawInput;

    @FXML
    private Canvas drawCanvas;

    private GraphicsContext gc;

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

    public void toggleDrawText(ActionEvent actionEvent) {
        if (currentTool == DrawTool.TEXT) {
            textToDrawInput.setDisable(true);
            currentTool = null;
        } else {
            textToDrawInput.setDisable(false);
            currentTool = DrawTool.TEXT;
        }
    }

    enum DrawTool {
        TEXT, ERASER, FREEHAND, ELLIPSE, LINE, RECTANGLE, CIRCLE
    }

    private DrawTool currentTool = null;

    // Coordinates for drawing
    private double startX, startY;
    private double endX, endY;

    private String currentFileName = null;
    private Path currentFilePath = null;

    @FXML
    private void initialize() {
        // ToggleGroup allows us to only select one object from the draw tools
        ToggleGroup objectGroup = new ToggleGroup();
        textToggle.setToggleGroup(objectGroup);
        eraserToggle.setToggleGroup(objectGroup);
        freeToggle.setToggleGroup(objectGroup);
        ellipseToggle.setToggleGroup(objectGroup);
        lineToggle.setToggleGroup(objectGroup);
        rectangleToggle.setToggleGroup(objectGroup);
        circleToggle.setToggleGroup(objectGroup);

        // StrokeWidth
        strokeWidthInput.setMin(1.0);
        strokeWidthInput.setMax(20.0);

        // Get graphics context from canvas
        gc = drawCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.WHITE);
        gc.fillRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());

        // Event handlers for canvas
        // Click Event
        drawCanvas.setOnMouseClicked(event -> {
            if (currentTool == null) {
                return;
            }

            // Get coordinates of click
            double x = event.getX();
            double y = event.getY();

            switch (currentTool) {
                case ELLIPSE:
                case LINE:
                case CIRCLE:
                case RECTANGLE:
                    break;
                case TEXT: {
                    // TODO: Choose between stroke and fill?
                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());
                    gc.setFont(new Font(strokeWidthInput.getValue()));
                    String text = textToDrawInput.getText();
                    gc.fillText(text, x, y);
                    break;
                }
                case ERASER: {
                    // TODO: Change this to background color
                    gc.setStroke(Color.WHITE);
                    gc.setFill(Color.WHITE);
                    gc.fillOval(x, y, strokeWidthInput.getValue(), strokeWidthInput.getValue());
                    break;
                }
                case FREEHAND: {
                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());
                    gc.fillOval(x, y, strokeWidthInput.getValue(), strokeWidthInput.getValue());
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

        });

        drawCanvas.setOnMouseDragged(event -> {
            if (currentTool == null) return;

            if (currentTool == DrawTool.ERASER) {
                // TODO: Use background color
                gc.setStroke(Color.WHITE);
                gc.setFill(Color.WHITE);
                gc.fillOval(event.getX(), event.getY(), strokeWidthInput.getValue(), strokeWidthInput.getValue());
            } else if (currentTool == DrawTool.FREEHAND) {
                gc.setStroke(drawColor.getValue());
                gc.setFill(drawColor.getValue());
                gc.fillOval(event.getX(), event.getY(), strokeWidthInput.getValue(), strokeWidthInput.getValue());
            }
        });

        // drag exited
        drawCanvas.setOnMouseReleased(event -> {
            if (currentTool == null) return;

            switch (currentTool) {
                case TEXT:
                case ERASER:
                case FREEHAND:
                    break;
                case CIRCLE: {
                    double outerX = event.getX();
                    double outerY = event.getY();
                    double radius = distance(startX, startY, outerX, outerY);

                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());

                    // A circle is also an oval with both axes of length diameter
                    // Oval requires the top-left corner which we can get by doing
                    // subtracting radius from the center
                    gc.fillOval(startX - radius, startY - radius, radius * 2, radius * 2);

                    break;
                }
                case LINE: {
                    gc.setLineWidth(strokeWidthInput.getValue());
                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());
                    gc.strokeLine(startX, startY, event.getX(), event.getY());
                    startX = endX = startY = endY = 0.0;

                    break;
                }
                case RECTANGLE: {
                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();
                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());
                    gc.fillRect(startX, startY, endX - startX, endY - startY);
                    startX = endX = startY = endY = 0.0; // Reset start and end
                    break;
                }
                case ELLIPSE: {
                    endX = event.getX();
                    endY = event.getY();
                    alignStartEnd();
                    gc.setStroke(drawColor.getValue());
                    gc.setFill(drawColor.getValue());
                    gc.fillOval(startX, startY, endX - startX, endY - startY);
                    startX = endX = startY = endY = 0.0; // Reset start and end
                    break;
                }
            }
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
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}
