package app.view;

import app.AppController;
import app.model.State;
import app.util.PdfOpener;
import javafx.fxml.FXML;

public class TermsConditionsPageController {

    @FXML
    private void handleBack() {
        AppController.updateView(State.SETTINGS);
    }

    @FXML
    private void openTermsPdf() {
        PdfOpener.openResourcePdf("tcs.pdf");
    }
}
