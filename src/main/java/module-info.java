module safex {
    requires javafx.controls;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;
    requires javafx.fxml;
    opens app.view to javafx.fxml;
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
    exports app.Schedule;
    opens app.Schedule to javafx.fxml;
}

