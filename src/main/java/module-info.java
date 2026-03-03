module safex {
    // Dependencies
    requires transitive javafx.controls;
    requires javafx.graphics; // Added for Scene class
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;

    // Permissions for JavaFX to reflectively access your controllers/views
    opens app to javafx.fxml;
    opens app.model to javafx.fxml;
    opens app.service to javafx.fxml;
    opens app.view to javafx.fxml;
    opens app.Schedule to javafx.fxml;

    // Exports for visibility to other modules
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
    exports app.Schedule;
}