module safex {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
}

