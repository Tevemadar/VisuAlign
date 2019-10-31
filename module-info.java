module qnonlin {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;
    opens visualign to javafx.fxml;
    exports visualign;
}