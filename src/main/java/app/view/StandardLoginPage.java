package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class StandardLoginPage extends LoginBase {
    @FXML private Button registerLink;
    @FXML private Button forgotPasswordLink;
    @FXML private Button switchPortalLink;

    @Override
    protected void onInitialize() {
        titleLabel.setText("Welcome back");
        subtitleLabel.setText("Sign in to continue.");

        registerLink.setText("Create an account");
        forgotPasswordLink.setText("Forgot Password?");
        switchPortalLink.setText("Are you a clinic? Clinic Portal");

        registerLink.setOnAction(e ->
                AppController.updateView(State.REGISTER));

        forgotPasswordLink.setOnAction(e ->
                AppController.startRecoveryFlow(State.LOGIN));

        switchPortalLink.setOnAction(e ->
                AppController.updateView(State.CLINIC_LOGIN));
    }

    @Override
    protected String getLoginType() {
        return "STANDARD";
    }
}
