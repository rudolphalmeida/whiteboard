package com.framelessboard;

import org.json.JSONObject;
import java.awt.geom.*;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomAction {

    public JSONObject action = new JSONObject();

    /*

    General structure of drawing;

    e.g.

    {
        "Object": "ELLIPSE",
        "Action": {
            "color": "blue",
            "startX": 10.0,
            "startY": 10.0,
            "endX": 20.0,
            "endY": 20.0
        }
    }

    */

    // Constructor for ellipses
    CustomAction(String actionType, String color, double startX, double startY, double axis1, double axis2,  boolean filled, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("axis1", axis1);
        attributes.put("axis2", axis2);
        attributes.put("fill", filled);
        attributes.put("strokeWidth", strokeWidth);
        action.put("Action", attributes);
    }

    // Constructor for rectangles
    CustomAction(String actionType, String color, double startX, double startY, double width, double height, double strokeWidth, boolean filled) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("width", width);
        attributes.put("height", height);
        attributes.put("fill", filled);
        attributes.put("strokeWidth", strokeWidth);
        action.put("Action", attributes);
    }

    // Constructor for lines
    CustomAction(String actionType, String color, double startX, double startY, double endX, double endY, double thickness) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("endX", endX);
        attributes.put("endY", endY);
        attributes.put("thickness", thickness);
        action.put("Action", attributes);
    }


    // Constructor for Circle
    CustomAction(String actionType, String color, double startX, double startY, double radius, boolean fill, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("radius", radius);
        attributes.put("fill", fill);
        attributes.put("StrokeWidth", strokeWidth);
        action.put("Action", attributes);
    }


    // Constructor for freehand drawings - freehand lines and erasers.
    CustomAction(String actionType, String color, double strokeWidth, ArrayList<Double> pointBuffer) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("StrokeWidth", strokeWidth);
        attributes.put("color", color);
        attributes.put("pointBuffer", pointBuffer);
        action.put("Action", attributes);
    }

    // Constructor for text drawings.
    CustomAction(String actionType, String color, double startX, double startY, String text, double fontSize) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("text", text);
        attributes.put("fontSize", fontSize);
        action.put("Action", attributes);
    }

    // Constructor for fill drawings.
    CustomAction(String actionType, String color, double startX, double startY){
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        action.put("Action", attributes);
    }

    // Constructur for background.
    CustomAction(String actionType, File file) {
        pngBase64 png = new pngBase64();
        String imgaeString = png.pngToString(file.getPath());

        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("image", imgaeString);
        action.put("Action", attributes);
    }

    // Constructor for chat messages.
    CustomAction(String actionType, String message) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("message", message);
        action.put("Action", attributes);
    }

    public JSONObject getAction() {
        return action;
    }

}