module com.framelessboard {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.framelessboard to javafx.fxml;
    exports com.framelessboard;
}