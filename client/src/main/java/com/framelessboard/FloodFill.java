package com.framelessboard;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.Queue;

class FloodFill extends Thread {

    private WritableImage canvasAsImage; // View of canvas as an image
    private Color replacementColor; // Color to be filled in
    private Color targetColor; // Color at click point
    private int startX, startY; // Click points
    private GraphicsContext gc; // The graphics context

    FloodFill(GraphicsContext gc, Canvas canvas, MouseEvent event, Color replacementColor) {
        this.canvasAsImage = getWriteableImage(canvas);
        this.startX = (int) event.getX();
        this.startY = (int) event.getY();
        this.replacementColor = replacementColor;
        this.gc = gc;
    }

    private WritableImage getWriteableImage(Canvas canvas) {
        WritableImage canvasSnapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        return canvas.snapshot(null, canvasSnapshot);
    }

    private void floodFill(PixelReader reader, PixelWriter writer) {
        if (targetColor.equals(replacementColor)) return;

        writer.setColor(startX, startY, replacementColor);
        Queue<Node> nodeQueue = new LinkedList<>();
        nodeQueue.add(new Node(startX, startY));

        while (!nodeQueue.isEmpty()) {
            Node n = nodeQueue.remove();

            if (inBounds(n.x - 1, n.y) && reader.getColor(n.x - 1, n.y).equals(targetColor)) {
                writer.setColor(n.x - 1, n.y, replacementColor);
                nodeQueue.add(new Node(n.x - 1, n.y));
            }

            if (inBounds(n.x + 1, n.y) && reader.getColor(n.x + 1, n.y).equals(targetColor)) {
                writer.setColor(n.x + 1, n.y, replacementColor);
                nodeQueue.add(new Node(n.x + 1, n.y));
            }

            if (inBounds(n.x, n.y - 1) && reader.getColor(n.x, n.y - 1).equals(targetColor)) {
                writer.setColor(n.x, n.y - 1, replacementColor);
                nodeQueue.add(new Node(n.x, n.y - 1));
            }

            if (inBounds(n.x, n.y + 1) && reader.getColor(n.x, n.y + 1).equals(targetColor)) {
                writer.setColor(n.x, n.y + 1, replacementColor);
                nodeQueue.add(new Node(n.x, n.y + 1));
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < canvasAsImage.getWidth() && y >= 0 && y < canvasAsImage.getHeight();
    }

    @Override
    public void run() {
        PixelReader pixelReader = canvasAsImage.getPixelReader();
        PixelWriter pixelWriter = canvasAsImage.getPixelWriter();

        this.targetColor = pixelReader.getColor(startX, startY);

        if (targetColor != replacementColor) {
            floodFill(pixelReader, pixelWriter);
            /*
             * Possible explanation for blurring of the image when using this function:
             * https://stackoverflow.com/questions/33380306/how-can-i-draw-images-into-javafx-canvas-without-blur-aliasing-in-a-given-area
             * */
            gc.drawImage(canvasAsImage, 0, 0);
        }
    }

    private static class Node {
        int x, y;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
