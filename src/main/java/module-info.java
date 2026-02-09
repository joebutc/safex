module safex {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires jakarta.mail;
    requires java.desktop;
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
}
