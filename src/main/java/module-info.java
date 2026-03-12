module safex {
    // Dependencies
    requires transitive javafx.controls;
    requires javafx.graphics; // Added for Scene class
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;

    // Open packages broadly so JavaFX and IDE JUnit launches can use reflection.
    opens app;
    opens app.model;
    opens app.service;
    opens app.view;
    opens app.Schedule;
    opens app.util;

    // Exports for visibility to other modules
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
    exports app.Schedule;
    exports app.util;
}
