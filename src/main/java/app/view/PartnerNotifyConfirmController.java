package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class PartnerNotifyConfirmController {
    @FXML private Button notifyButton;
    @FXML private ProgressIndicator progressIndicator;

    private Runnable onNotify;

    public void configure(Runnable notifyAction) {
        onNotify = notifyAction;
    }

    Button getNotifyButton() {
        return notifyButton;
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
