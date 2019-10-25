module com.framelessboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.json;
    requires commons.io;

    opens com.framelessboard to javafx.fxml;
    exports com.framelessboard;
}
