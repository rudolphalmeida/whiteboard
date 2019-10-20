package com.framelessboard;

import org.json.JSONObject;
import java.awt.geom.*;
import java.awt.Rectangle;
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

    // Constructor for shapes - lines, rectangles, ellipses and circles.
    CustomAction(String actionType, String color, double startX, double startY, double endX, double endY, boolean filled) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("endX", endX);
        attributes.put("endY", endY);
        attributes.put("filled", filled);
        action.put("Action", attributes);
    }

    // Constructor for erasers
    CustomAction(String actionType, double startX, double startY, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("StrokeWidth", strokeWidth);
        action.put("Action", attributes);
    }

    // Constructor for Circle
    CustomAction(String actionType, String color, double startX, double startY, double radius, boolean fill, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("StrokeWidth", strokeWidth);
        action.put("Action", attributes);
    }

    // Constructor for FreeHand
    CustomAction(String actionType, String color, double startX, double startY, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("StrokeWidth", strokeWidth);
        action.put("Action", attributes);
    }

    // Constructor for freehand drawings - freehand lines and erasers.
    CustomAction(String actionType, String color, double strokeWidth) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("StrokeWidth", strokeWidth);
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

    // Constructur for background.
    CustomAction(String actionType, JSONObject encoding) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("encoding", encoding.toString());
        action.put("Action", attributes);
    }

    // Constructor for chat messages.
    CustomAction(String actionType, String message) {
        action.put("Object", actionType);
        JSONObject attributes = new JSONObject();
        attributes.put("message", message);
    }

    public JSONObject getAction() {
        return action;
    }

}