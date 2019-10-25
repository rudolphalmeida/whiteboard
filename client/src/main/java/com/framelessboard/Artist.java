package com.framelessboard;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class Artist {

    private Canvas canvas;
    private GraphicsContext gc;

    Artist(Canvas canvas, GraphicsContext gc) {
        this.canvas = canvas;
        this.gc = gc;
    }

    void clearCanvas() {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    void drawImage(Image image) {
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    void drawText(String text, Color color, double x, double y, double fontSize) {
        gc.setStroke(color);
        gc.setFill(color);

        gc.setFont(new Font(fontSize));

        gc.fillText(text, x, y);
    }

    void drawFreeHand(double x, double y, double size, Color color) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.fillOval(x, y, size, size);
    }

    void erase(double x, double y, double size) {
        gc.setStroke(Color.WHITE);
        gc.setFill(Color.WHITE);
        gc.fillOval(x, y, size, size);
    }

    void floodFill(double x, double y, Color color) {
        FloodFill ff = new FloodFill(gc, canvas, x, y, color);
        ff.start();
    }

    void drawCircle(double x, double y, double radius, Color color, boolean fill, double strokeWidth) {
        gc.setStroke(color);
        gc.setFill(color);

        gc.setLineWidth(strokeWidth);

        // A circle is also an oval with both axes of length diameter
        // Oval requires the top-left corner which we can get by
        // subtracting radius from the center
        if (fill) {
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        } else {
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    void drawLine(double startX, double startY, double endX, double endY, double thickness, Color color) {
        gc.setLineWidth(thickness);

        gc.setStroke(color);
        gc.setFill(color);

        gc.strokeLine(startX, startY, endX, endY);
    }

    void drawRectangle(double x, double y, double width, double height, Color color, boolean fill, double strokeWidth) {
        gc.setStroke(color);
        gc.setFill(color);

        gc.setLineWidth(strokeWidth);

        if (fill) {
            gc.fillRect(x, y, width, height);
        } else {
            gc.strokeRect(x, y, width, height);
        }
    }

    void drawEllipse(double x, double y, double axis1, double axis2, Color color, boolean fill, double strokeWidth) {
        gc.setStroke(color);
        gc.setFill(color);

        gc.setLineWidth(strokeWidth);

        if (fill) {
            gc.fillOval(x, y, axis1, axis2);
        } else {
            gc.strokeOval(x, y, axis1, axis2);
        }
    }
}
