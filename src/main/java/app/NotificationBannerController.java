package app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NotificationBannerController {
    @FXML private HBox bannerRoot;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;

    private Runnable onCloseCallback;

    // Called by scene/dashboard to set the specific notification text.
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    // Optional: Call this to change the  "Reminder" title dynamically
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    // Passes the animation/removal logic from the dashboard
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    // Triggered when the user clicks the close button
    @FXML
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
