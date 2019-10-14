import org.json.JSONObject;
import java.awt.geom.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class CustomDrawing {
    
    public JSONObject drawing;
    
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
    CustomDrawing(String drawType, String color, double startX, int startY, int endX, int endY) {
        drawing.put("Object", drawType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", startX);
        attributes.put("startY", startY);
        attributes.put("endX", endX);
        attributes.put("endY", endY);
        drawing.put("Action", attributes);
    }
    
    // Constructor for freehand drawings - freehand lines and erasers.
    CustomDrawing(String drawType, String color, double strokeWidth) {
        drawing.put("Object", drawType);
        JSONObject attributes = new JSONObject();
        attributes.put("StrokeWidth", strokeWidth);
        drawing.put("Action", attributes);
    }
    
    // Constructor for text drawings.
    CustomDrawing(String drawType, String color, int startX, int startY, String text) {
        drawing.put("Object", drawType);
        JSONObject attributes = new JSONObject();
        attributes.put("color", color);
        attributes.put("startX", lpx);
        attributes.put("startY", lpy);
        attributes.put("text", text);
    }
    
    public JSONObject getDrawing() {
        return drawing;
    }
    
}
