package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ClinicLoginPage extends LoginBase {

    @FXML private Button registerLink;
    @FXML private Button switchPortalLink;

    @Override
    protected void onInitialize() {
        titleLabel.setText("Clinic Portal");
        subtitleLabel.setText("Sign in to manage your clinic.");

        registerLink.setText("Register a new clinic");
        switchPortalLink.setText("Are you a patient? Patient Portal");

        registerLink.setOnAction(e ->
                AppController.updateView(State.CLINIC_REGISTER));

        switchPortalLink.setOnAction(e ->
                AppController.updateView(State.LOGIN));
    }

    @Override
    protected String getLoginType() {
        return "CLINIC";
    }
}
