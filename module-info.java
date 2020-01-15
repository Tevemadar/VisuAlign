module qnonlin {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    opens visualign to javafx.fxml;
    exports visualign;
}