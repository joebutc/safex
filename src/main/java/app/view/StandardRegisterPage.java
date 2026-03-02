package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class StandardRegisterPage extends RegisterBase {

    @FXML private Button loginLink;

    @Override
    protected void onInitialize() {

        buildBrandPanel("Your health, your privacy,\nyour control.");

        titleLabel.setText("Create account");
        subtitleLabel.setText("Join our secure community.");

        loginLink.setText("Already have an account? Log in");
        loginLink.setOnAction(e ->
                AppController.updateView(State.LOGIN));
    }

    @Override
    protected String getLoginType() {
        return "STANDARD";
    }
}
