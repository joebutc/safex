package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import app.util.PdfOpener;

public class LegalOnboardPage implements Page {
    private VBox content;

    @Override
    public Scene createScene() {
        content = new VBox(12);
        content.setPadding(new Insets(24));
        content.setMaxWidth(520);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        BorderPane root = new BorderPane(scrollPane);
        BorderPane.setAlignment(scrollPane, Pos.TOP_CENTER);

        showPrivacyPolicy();

        return new Scene(root, 600, 400);
    }

    private Label buildBullet(String text) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        label.setMaxWidth(520);
        return label;
    }

    private void onAgree() {
        System.out.println("agreed");
    }

    private void showPrivacyPolicy() {
        Label title = new Label("Privacy Policy Summary");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        Label intro = new Label("This summary provides key points from our Privacy Notice, but you can find out more details about any of these topics by using the table of contents in the full notice or reading the full sections.");
        intro.setFont(Font.font("System", FontPosture.ITALIC, 12));
        intro.setWrapText(true);
        intro.setMaxWidth(520);

        VBox bullets = new VBox(8);

        bullets.getChildren().addAll(
                buildQuestionAnswer("What personal information do we process?", " When you visit, use, or navigate our Services, we may process personal information depending on how you interact with us and the Services, the choices you make, and the products and features you use. Learn more about personal information we collect in Section 1."),
                buildQuestionAnswer("Do we process any sensitive personal information?", " Some of the information we process may be considered special category data (for example health data). We process special category data only where we have a lawful basis and, for health data, only where we have your explicit consent or processing is otherwise permitted under applicable law (for example, where necessary for the provision of health or social care). See Section 3 and Section 10."),
                buildQuestionAnswer("Do we collect any information from third parties?", " We may receive limited personal and health-related information from healthcare providers, clinics, or laboratories when you use the Services to book tests, receive results, or manage treatment. See Section 1 and Section 4 for details."),
                buildQuestionAnswer("How do we process your information?", " We process your information to provide, improve, and administer our Services, communicate with you, for safety and fraud prevention, and to comply with law. We may also process your information for other purposes with your consent. See Section 2."),
                buildQuestionAnswer("In what situations and with which parties do we share personal information?", " We may share information with healthcare providers, service partners, and in limited situations described in Section 4."),
                buildQuestionAnswer("How do we keep your information safe?", " We use organisational and technical measures to protect your personal information, but cannot guarantee absolute security. See Section 6."),
                buildQuestionAnswer("What are your rights?", " Depending where you live, you have rights to access, correct, delete, and restrict processing of your personal data; and to withdraw consent where processing is based on consent. See Section 8."),
                buildQuestionAnswer("How do you exercise your rights?", " The easiest way to exercise your rights is through the app's privacy and data controls, or by contacting us at support@safex.com. We will consider and act upon any request in accordance with applicable data protection laws. See Section 13.")
        );

        Label closing = new Label("Want to learn more about what we do with any information we collect? Review the Privacy Notice in full.");
        closing.setWrapText(true);
        closing.setMaxWidth(520);

        HBox pdfRow = buildPdfRow("Privacy Notice (full) (PDF)", "privacy.pdf");

        Button continueButton = new Button("By continuing, you acknowledge that you have read our Privacy Policy.");
        continueButton.setOnAction(event -> showTermsAndConditions());
        continueButton.setWrapText(true);
        continueButton.setMaxWidth(520);

        HBox actions = new HBox(continueButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().setAll(title, intro, bullets, closing, pdfRow, actions);
    }

    private void showTermsAndConditions() {
        Label title = new Label("Terms & Conditions Summary");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        Label intro = new Label("This summary provides key points from our Terms of Use, but you can find out more details about any of these topics by using the table of contents in the full Terms or reading the full sections.");
        intro.setFont(Font.font("System", FontPosture.ITALIC, 12));
        intro.setWrapText(true);
        intro.setMaxWidth(520);

        VBox bullets = new VBox(8);
        bullets.getChildren().addAll(
            buildQuestionAnswer("What are these Terms and who do they apply to?", " These Terms form a legally binding agreement between you and SafeX Ltd and apply to your access to and use of the SafeX mobile application and related services. See \u201cAgreement to Our Legal Terms\u201d."),
            buildQuestionAnswer("What do the Services do (and not do)?", " The Services support test booking, delivery of test results, prescription management, secure notifications, and private communications with clinics/healthcare professionals. The Services do not provide public posting, public social feeds, or public comments. See Section 1."),
            buildQuestionAnswer("Who can use the Services?", " You must be at least 18 years old, have legal capacity to enter into these Terms, and provide accurate information. See Section 3."),
            buildQuestionAnswer("What are the main rules for using the Services?", " You must not misuse the Services (for example, unlawful activity, impersonation, interfering with systems or accounts, security circumvention, malware, spam, scraping/bots without permission, or reverse engineering). See Section 4."),
            buildQuestionAnswer("Who owns the Services and what rights do you get?", " SafeX (and its licensors) own the Services and associated content. You receive a limited, non-exclusive, non-transferable, revocable licence to access and use the Services for personal, non-commercial use, subject to these Terms. See Section 2."),
            buildQuestionAnswer("What about content you provide and feedback you submit?", " You keep your rights in the content you provide, but you grant the rights needed to operate the Services. If you submit feedback, you grant SafeX a broad licence to use it (including to improve the Services). See Section 5."),
            buildQuestionAnswer("Can SafeX suspend or terminate access?", " SafeX may monitor for compliance, restrict or terminate access, and take action if these Terms are violated. Termination ends your right to use the Services, and certain sections continue to apply. See Sections 6 and 7."),
            buildQuestionAnswer("Can the Services or Terms change?", " The Services may be modified, interrupted, or discontinued, and the Terms may be updated from time to time. Material changes are indicated by updating the \u201cLast updated\u201d date and providing notice where required. See \u201cAgreement to Our Legal Terms\u201d and Section 8."),
            buildQuestionAnswer("What law applies and how are disputes handled?", " These Terms are governed by the laws of England and Wales, and disputes are generally handled through informal resolution first, then courts in England and Wales (without limiting urgent injunctive relief). See Sections 9 and 10."),
            buildQuestionAnswer("Is the Services' information medical advice?", " No. Information and features are for general informational purposes and are not a substitute for professional medical advice, diagnosis, or treatment. SafeX is not a healthcare provider. See Section 12."),
            buildQuestionAnswer("What warranties and liabilities are disclaimed/limited?", " The Services are provided \u201cas is\u201d and \u201cas available\u201d, warranties are disclaimed to the maximum extent permitted by law, and liability is limited (including a cap on aggregate liability), subject to applicable law. See Sections 13 and 14."),
            buildQuestionAnswer("Do you have to indemnify SafeX?", " You agree to indemnify and hold SafeX harmless from certain claims and costs arising from your use of the Services and violations of these Terms. See Section 15."),
            buildQuestionAnswer("How is user data handled?", " SafeX maintains data you transmit for operating/providing/improving the Services and performs routine backups, with limits on liability for loss/corruption. Personal data use is governed by the Privacy Policy. See Section 16."),
            buildQuestionAnswer("How can you contact SafeX?", " Email support@safex.com or write to SafeX Ltd, London, United Kingdom (for notices only). See Section 19.")
        );

        Label closing = new Label("Want to learn more? Review the full Terms in the app or the Terms document.");
        closing.setWrapText(true);
        closing.setMaxWidth(520);

        HBox pdfRow = buildPdfRow("Terms (full) (PDF)", "tcs.pdf");

        Button agreeButton = new Button("I Agree");
        agreeButton.setDefaultButton(true);
        agreeButton.setOnAction(event -> onAgree());
        agreeButton.setWrapText(true);
        agreeButton.setMaxWidth(520);

        HBox actions = new HBox(agreeButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().setAll(title, intro, bullets, closing, pdfRow, actions);
        }

    private HBox buildPdfRow(String labelText, String resourceName) {
        Label label = new Label(labelText);
        Button openPdfButton = new Button("Open PDF");
        openPdfButton.setOnAction(event -> PdfOpener.openResourcePdf(resourceName));

        HBox row = new HBox(10, label, openPdfButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TextFlow buildQuestionAnswer(String question, String answer) {
        Text q = new Text(question);
        q.setFont(Font.font("System", FontWeight.BOLD, 13));
        Text a = new Text(answer);
        a.setFont(Font.font("System", FontWeight.NORMAL, 13));
        TextFlow tf = new TextFlow(q, a);
        tf.setMaxWidth(520);
        return tf;
    }
}
