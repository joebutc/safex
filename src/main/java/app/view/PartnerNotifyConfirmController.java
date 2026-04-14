package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class PartnerNotifyConfirmController {
    @FXML private Button notifyButton;
    @FXML private ProgressIndicator progressIndicator;

    private Runnable onNotify;
    private Runnable onBack;

    public void configure(Runnable notifyAction,Runnable backAction) {
        onNotify = notifyAction;
        onBack = backAction;
    }

    Button getNotifyButton() {
        return notifyButton;
    }

    @FXML
    private void backButton() {
        if (onBack != null) {
            onBack.run();
        } else {
            // Fallback just in case it wasn't configured
            AppController.goToHomeDashboard();
        }
    }

    ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    @FXML
    private void handleNotify() {
        if (onNotify != null) {
            onNotify.run();
        }
    }
}
