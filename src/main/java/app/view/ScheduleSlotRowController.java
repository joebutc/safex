package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ScheduleSlotRowController {
    @FXML private Label timeLabel;
    @FXML private Label statusLabel;
    @FXML private Button actionButton;

    private Runnable action;

    public void configure(String time, String status, String statusStyleClass,
                          String actionLabel, Runnable onAction) {
        configure(time, status, statusStyleClass, actionLabel, onAction, "primary-button");
    }

    public void configure(String time, String status, String statusStyleClass,
                          String actionLabel, Runnable onAction, String actionStyleClass) {
        timeLabel.setText(time);
        statusLabel.setText(status);
        statusLabel.getStyleClass().removeAll(
                "slot-status-available",
                "slot-status-booked",
                "slot-status-past",
                "slot-status-offered",
                "slot-status-closed"
        );
        statusLabel.getStyleClass().add(statusStyleClass);

        action = onAction;
        boolean hasAction = actionLabel != null && onAction != null;
        actionButton.setManaged(hasAction);
        actionButton.setVisible(hasAction);
        if (hasAction) {
            actionButton.setText(actionLabel);
            actionButton.getStyleClass().removeAll("primary-button", "secondary-button");
            actionButton.getStyleClass().add(actionStyleClass);
        }
    }

    @FXML
    private void handleAction() {
        if (action != null) {
            action.run();
        }
    }
}
