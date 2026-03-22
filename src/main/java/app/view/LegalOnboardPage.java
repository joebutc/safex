package app.view;

import app.AppController;
import app.util.PdfOpener;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class LegalOnboardPage {
    @FXML private VBox privacySection;
    @FXML private VBox termsSection;

    @FXML
    private void handleHome() {
        AppController.goToHomeDashboard();
    }

    @FXML
    private void handleContinue() {
        privacySection.setVisible(false);
        privacySection.setManaged(false);
        termsSection.setVisible(true);
        termsSection.setManaged(true);
    }

    @FXML
    private void handleAgree() {
        AppController.goToHomeDashboard();
    }

    @FXML
    private void openPrivacyPdf() {
        PdfOpener.openResourcePdf("privacy.pdf");
    }

    @FXML
    private void openTermsPdf() {
        PdfOpener.openResourcePdf("tcs.pdf");
    }
}
