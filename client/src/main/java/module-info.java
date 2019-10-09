module com.framelessboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;

    opens com.framelessboard to javafx.fxml;
    exports com.framelessboard;
}
