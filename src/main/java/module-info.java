module safex {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires jakarta.mail;
    requires org.apache.logging.log4j.core;
    requires javafx.fxml;
    opens app;
    opens app.model;
    opens app.service;
    opens app.view;
    exports app;
    exports app.model;
    exports app.view;
    exports app.service;
}

