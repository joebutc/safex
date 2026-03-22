package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ClinicRegisterPage extends RegisterBase {

    @FXML private Button loginLink;

    @Override
    protected void onInitialize() {

        loginLink.setText("Already registered? Log in to clinic");
        loginLink.setOnAction(e ->
                AppController.updateView(State.CLINIC_LOGIN));
    }

    @Override
    protected String getLoginType() {
        return "CLINIC";
    }
}
