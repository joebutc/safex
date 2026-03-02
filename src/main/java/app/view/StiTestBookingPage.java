package app.view;

import app.AppController;
import app.model.Action;
import app.model.Context;
import app.model.Result;
import app.service.GateService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Step-by-step eligibility wizard that routes the patient through the
 * GateService decision matrix one question at a time.
 *
 * Smart routing: answering Yes to a fail-gate question immediately shows
 * the CLINIC_REFER result — no need to answer remaining questions.
 */
public class StiTestBookingPage implements Page {

    private static final String NHS_BASE =
            "https://www.nhs.uk/service-search/sexual-health-services/" +
            "find-sti-testing-and-treatment/results?location=";

    // ── Step IDs ──────────────────────────────────────────────────────────────
    private static final int S_AGE          =  1;
    private static final int S_FRASER        =  2;
    private static final int S_LOCATION      =  3;
    private static final int S_SYMPTOMS      =  4;
    private static final int S_ASSAULT       =  5;
    private static final int S_PEP           =  6;
    private static final int S_PREGNANCY     =  7;
    private static final int S_IMMUNO        =  8;
    private static final int S_SAFEGUARDING  =  9;
    private static final int S_WINDOW        = 10;
    private static final int S_RECENT_KIT    = 11;
    private static final int S_WEEKS         = 12;
    private static final int S_CONSENT       = 13;
    private static final int S_RESULT        = 14;
    private static final int TOTAL_STEPS     = 11; // main-path steps for progress bar

    private final AppController controller;

    // ── Collected answers ─────────────────────────────────────────────────────
    private int     age          = 18;
    private boolean fraser       = false;
    private String  location     = "";
    private boolean symptomatic  = false;
    private boolean assault      = false;
    private boolean pep          = false;
    private boolean preg         = false;
    private boolean immuno       = false;
    private boolean safeguarding = false;
    private boolean windowElap   = false;
    private boolean recentKit    = false;
    private int     kitWeeks     = 6;
    private Result  finalResult  = null;

    // ── Navigation history for Back button ────────────────────────────────────
    private final Deque<Integer> history = new ArrayDeque<>();
    private int currentStep = S_AGE;

    // ── Mutable header UI ─────────────────────────────────────────────────────
    private VBox        contentArea;
    private ProgressBar progressBar;
    private Label       stepLabel;
    private Button      headerBackBtn;

    public StiTestBookingPage(AppController controller) {
        this.controller = controller;
    }

    // ── Scene ─────────────────────────────────────────────────────────────────

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);
        root.setTop(buildHeader());

        contentArea = new VBox();
        contentArea.setAlignment(Pos.TOP_CENTER);
        contentArea.setPadding(new Insets(36, 40, 36, 40));

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F8FAFC; -fx-background: #F8FAFC;");
        root.setCenter(scroll);

        showStep(S_AGE);
        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void advance(int next) {
        history.push(currentStep);
        currentStep = next;
        showStep(next);
    }

    private void goBack() {
        if (!history.isEmpty()) {
            currentStep = history.pop();
            showStep(currentStep);
        } else if (controller != null) {
            controller.goToDashboard();
        }
    }

    private void showStep(int step) {
        updateProgress(step);
        contentArea.getChildren().setAll(buildStep(step));
    }

    private void updateProgress(int step) {
        if (progressBar == null) return;
        int n = displayNumber(step);
        if (step == S_RESULT) {
            progressBar.setProgress(1.0);
            stepLabel.setText("Complete");
        } else if (n > 0) {
            progressBar.setProgress((double) n / TOTAL_STEPS);
            stepLabel.setText("Step " + n + " of " + TOTAL_STEPS);
        }
        headerBackBtn.setOnAction(e -> {
            if (history.isEmpty() && controller != null) controller.goToDashboard();
            else goBack();
        });
    }

    private int displayNumber(int step) {
        return switch (step) {
            case S_AGE, S_FRASER  ->  1;
            case S_LOCATION       ->  2;
            case S_SYMPTOMS       ->  3;
            case S_ASSAULT        ->  4;
            case S_PEP            ->  5;
            case S_PREGNANCY      ->  6;
            case S_IMMUNO         ->  7;
            case S_SAFEGUARDING   ->  8;
            case S_WINDOW         ->  9;
            case S_RECENT_KIT,
                 S_WEEKS          -> 10;
            case S_CONSENT        -> 11;
            default               -> -1;
        };
    }

    // ── Step dispatcher ───────────────────────────────────────────────────────

    private VBox buildStep(int step) {
        return switch (step) {
            case S_AGE         -> stepAge();
            case S_FRASER      -> stepFraser();
            case S_LOCATION    -> stepLocation();
            case S_SYMPTOMS    -> booleanCard("3", "\uD83E\uDE7A", "Any current symptoms?",
                    "Are you experiencing any symptoms right now — such as unusual discharge, sores, itching, or pain?",
                    "Symptoms require a physical examination at a clinic for an accurate diagnosis.",
                    () -> { symptomatic = true;  earlyFail("G2", "Symptomatic — a sexual health clinic can examine and test you accurately."); },
                    () -> { symptomatic = false; advance(S_ASSAULT); });
            case S_ASSAULT     -> booleanCard("4", "\uD83D\uDEE1\uFE0F", "Recent sexual assault?",
                    "Have you experienced a sexual assault recently?",
                    "If you have, urgent specialist care is available. Your safety comes first.",
                    () -> { assault = true;  earlyFail("G3", "Assault reported — immediate SARC or A&E referral for forensic care, PEP and EC."); },
                    () -> { assault = false; advance(S_PEP); });
            case S_PEP         -> booleanCard("5", "\uD83D\uDC8A", "Do you need PEP?",
                    "Do you think you may need PEP — emergency HIV prevention medication?",
                    "PEP must be started within 72 hours. It is available at sexual health clinics and A&E.",
                    () -> { pep = true;  earlyFail("G4", "PEP needed — go urgently to a sexual health clinic or A&E."); },
                    () -> { pep = false; advance(S_PREGNANCY); });
            case S_PREGNANCY   -> booleanCard("6", "\uD83E\uDEB7", "Pregnant or possibly?",
                    "Are you pregnant, or do you think you might be?",
                    "Pregnant individuals need coordinated care from sexual health and obstetric teams.",
                    () -> { preg = true;  earlyFail("G5", "Pregnant — please visit a sexual health clinic and your obstetric team."); },
                    () -> { preg = false; advance(S_IMMUNO); });
            case S_IMMUNO      -> booleanCard("7", "\uD83C\uDFE5", "Immunocompromised?",
                    "Are you immunocompromised — for example, on immunosuppressants or HIV positive with a low CD4 count?",
                    "A specialist HIV or sexual health clinic can provide the most appropriate care.",
                    () -> { immuno = true;  earlyFail("G6", "Immunocompromised — specialist sexual health or HIV clinic recommended."); },
                    () -> { immuno = false; advance(S_SAFEGUARDING); });
            case S_SAFEGUARDING -> booleanCard("8", "\u26A0\uFE0F", "Any safeguarding concerns?",
                    "Are there safeguarding concerns in your situation — such as coercion, exploitation, or vulnerability?",
                    "These situations are best handled by a clinical team with safeguarding experience.",
                    () -> { safeguarding = true;  earlyFail("G7", "Safeguarding concern — clinical triage with a safeguarding lead is required."); },
                    () -> { safeguarding = false; advance(S_WINDOW); });
            case S_WINDOW      -> booleanCard("9", "\u23F1\uFE0F", "At least 2 weeks since exposure?",
                    "Has it been at least 2 weeks since the potential exposure you want to test for?",
                    "Many STI tests need a window period of at least 2 weeks for accurate results.",
                    () -> { windowElap = true;  advance(S_RECENT_KIT); },
                    () -> { windowElap = false; earlyFail("G8", "Too early to test — please wait until the window period has passed, then reorder. Use safer sex in the meantime."); });
            case S_RECENT_KIT  -> booleanCard("10", "\uD83D\uDCE6", "Home kit in last 12 weeks?",
                    "Have you received a home STI testing kit in the last 12 weeks?",
                    "Frequent orders may indicate ongoing risk — a clinic can provide extra support.",
                    () -> { recentKit = true;  advance(S_WEEKS); },
                    () -> { recentKit = false; advance(S_CONSENT); });
            case S_WEEKS       -> stepWeeks();
            case S_CONSENT     -> stepConsent();
            case S_RESULT      -> stepResult();
            default            -> new VBox();
        };
    }

    // ── Individual step builders ──────────────────────────────────────────────

    private VBox stepAge() {
        Spinner<Integer> spinner = new Spinner<>(10, 99, age);
        spinner.setEditable(true);
        spinner.setPrefWidth(130);
        spinner.setStyle("-fx-font-size: 16;");

        Button next = primaryBtn("Next  \u2192");
        next.setOnAction(e -> {
            age = spinner.getValue();
            if (age < 13) {
                earlyFail("G1", "Under 13 — safeguarding referral. Please speak to a trusted adult or call 111.");
            } else if (age <= 15) {
                advance(S_FRASER);
            } else {
                advance(S_LOCATION);
            }
        });

        return stepCard("1", "\uD83C\uDF82", "How old are you?",
                "Your age helps us check eligibility. Some age groups need extra clinical support.",
                centred(vbox(14, centred(spinner), centred(next))));
    }

    private VBox stepFraser() {
        return booleanCard("1", "\uD83D\uDCCB", "Fraser competency",
                "Has a healthcare professional confirmed you have Fraser competency — meaning you can independently understand and consent to your care?",
                "Fraser competency is required for individuals aged 13–15 to use home testing kits.",
                () -> { fraser = true;  advance(S_LOCATION); },
                () -> { fraser = false; earlyFail("G1", "13–15 without Fraser competency — please visit a sexual health clinic for a triage and support."); });
    }

    private VBox stepLocation() {
        TextField tf = new TextField(location);
        tf.setPromptText("e.g. London, SW1A 1AA");
        tf.setFont(Font.font("System", 16));
        tf.setMaxWidth(380);
        tf.setPadding(new Insets(10, 14, 10, 14));

        Button next = primaryBtn("Next  \u2192");
        next.setOnAction(e -> {
            String loc = tf.getText().trim();
            if (loc.isBlank()) {
                alert(Alert.AlertType.WARNING, "Location required",
                        "Please enter your postcode or town to find nearby services.");
                return;
            }
            location = loc;
            advance(S_SYMPTOMS);
        });

        return stepCard("2", "\uD83D\uDCCD", "Where are you located?",
                "Your postcode or town is used to find NHS sexual health services near you.",
                centred(vbox(14, centred(tf), centred(next))));
    }

    private VBox stepWeeks() {
        Spinner<Integer> spinner = new Spinner<>(1, 12, kitWeeks);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        spinner.setStyle("-fx-font-size: 16;");

        Button next = primaryBtn("Next  \u2192");
        next.setOnAction(e -> {
            kitWeeks = spinner.getValue();
            if (kitWeeks < 12) {
                earlyFail("G9", "Kit received less than 12 weeks ago — please contact your clinic for ongoing support and assessment.");
            } else {
                advance(S_CONSENT);
            }
        });

        return stepCard("10", "\uD83D\uDCC5", "How many weeks ago?",
                "You mentioned you received a home kit recently. How many weeks ago was that?",
                centred(vbox(14, centred(spinner), centred(next))));
    }

    private VBox stepConsent() {
        CheckBox box = new CheckBox(
                "I understand what home STI testing involves and I consent to this eligibility check.");
        box.setWrapText(true);
        box.setFont(Font.font("System", 14));
        box.setMaxWidth(420);

        Button submit = primaryBtn("Check My Eligibility  \u2192");
        submit.setOnAction(e -> {
            if (!box.isSelected()) {
                alert(Alert.AlertType.WARNING, "Consent required",
                        "Please tick the box to confirm you understand and consent.");
                return;
            }
            Context ctx = Context.builder()
                    .age(age).fraser(fraser)
                    .symptomatic(symptomatic).assault(assault)
                    .pep(pep).preg(preg).immuno(immuno).safeguarding(safeguarding)
                    .windowElapsed(windowElap)
                    .recentKitWeeks(recentKit ? kitWeeks : null)
                    .understands(true)
                    .clinOverride(false)
                    .build();
            finalResult = new GateService().run(ctx);
            advance(S_RESULT);
        });

        return stepCard("11", "\u2705", "Almost there!",
                "Please confirm you understand and consent before we show your result.",
                centred(vbox(18, box, centred(submit))));
    }

    private VBox stepResult() {
        if (finalResult == null) finalResult = passResult();

        String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String nhsUrl  = NHS_BASE + encoded;

        VBox card;
        if (finalResult.isPassed()) {
            card = resultCard(
                    "\u2713", "#065F46", "#064E3B", "#D1FAE5", "#6EE7B7",
                    "You appear eligible for a home STI test",
                    "Based on your answers you can order a home testing kit or find a nearby sexual health service using the button below.",
                    null, "Find Testing Services Near Me  \u2192", nhsUrl, true);
        } else {
            card = resultCard(
                    "\u26A0", "#92400E", "#78350F", "#FEF3C7", "#FCD34D",
                    "We recommend visiting a sexual health clinic",
                    finalResult.getReason(),
                    "Reference: " + finalResult.getGateName(),
                    "Find Your Nearest Clinic  \u2192", nhsUrl, false);
        }

        Button restart = new Button("Start Again");
        restart.getStyleClass().add("secondary-button");
        restart.setMinHeight(36);
        restart.setOnAction(e -> resetWizard());

        VBox outer = new VBox(18, card, centred(restart));
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setMaxWidth(600);
        return outer;
    }

    // ── Reusable card layouts ─────────────────────────────────────────────────

    /** Generic question card with a custom input area at the bottom. */
    private VBox stepCard(String stepNum, String icon, String title, String subtitle, VBox inputArea) {
        Label pill = new Label("Step " + stepNum);
        pill.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        pill.setTextFill(Color.web("#1E40AF"));
        pill.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 999; -fx-padding: 2 10 2 10;");

        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 38));

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLbl.setTextFill(Color.web("#0F172A"));
        titleLbl.setWrapText(true);
        titleLbl.setTextAlignment(TextAlignment.CENTER);
        titleLbl.setMaxWidth(500);

        Label subLbl = new Label(subtitle);
        subLbl.setFont(Font.font("System", 14));
        subLbl.setTextFill(Color.web("#475569"));
        subLbl.setWrapText(true);
        subLbl.setTextAlignment(TextAlignment.CENTER);
        subLbl.setMaxWidth(460);

        VBox header = new VBox(10, pill, iconLbl, titleLbl, subLbl);
        header.setAlignment(Pos.CENTER);

        VBox card = new VBox(28, header, inputArea);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 40, 36, 40));
        card.setMaxWidth(580);
        card.setStyle("-fx-background-color: #ffffff;"
                + " -fx-background-radius: 18;"
                + " -fx-border-color: #E5E7EB;"
                + " -fx-border-width: 1;"
                + " -fx-border-radius: 18;"
                + " -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 24, 0.15, 0, 6);");

        VBox outer = new VBox(card);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setMaxWidth(600);
        return outer;
    }

    /** Yes / No card — clicking either button auto-advances. */
    private VBox booleanCard(String stepNum, String icon, String title,
                              String question, String context,
                              Runnable onYes, Runnable onNo) {
        String base = "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 1.5;"
                + " -fx-font-size: 15; -fx-font-weight: bold;"
                + " -fx-min-width: 150; -fx-min-height: 52;";

        Button yes = new Button("\u2713  Yes");
        Button no  = new Button("\u2717  No");
        yes.setStyle(base + "-fx-background-color: #DCFCE7; -fx-border-color: #4ADE80; -fx-text-fill: #166534;");
        no.setStyle( base + "-fx-background-color: #F1F5F9; -fx-border-color: #CBD5E1; -fx-text-fill: #475569;");

        yes.setOnAction(e -> {
            yes.setStyle(base + "-fx-background-color: #BBF7D0; -fx-border-color: #22C55E; -fx-text-fill: #14532D;");
            onYes.run();
        });
        no.setOnAction(e -> {
            no.setStyle(base + "-fx-background-color: #CBD5E1; -fx-border-color: #94A3B8; -fx-text-fill: #1E293B;");
            onNo.run();
        });

        Label contextLbl = new Label(context);
        contextLbl.setFont(Font.font("System", 12));
        contextLbl.setTextFill(Color.web("#94A3B8"));
        contextLbl.setWrapText(true);
        contextLbl.setTextAlignment(TextAlignment.CENTER);
        contextLbl.setMaxWidth(420);

        HBox btnRow = new HBox(16, yes, no);
        btnRow.setAlignment(Pos.CENTER);

        VBox inputArea = new VBox(14, btnRow, contextLbl);
        inputArea.setAlignment(Pos.CENTER);

        return stepCard(stepNum, icon, title, question, inputArea);
    }

    /** Result card for PASS or CLINIC_REFER outcome. */
    private VBox resultCard(String icon, String iconColor, String bodyColor,
                             String bg, String border,
                             String heading, String body, String reference,
                             String btnText, String nhsUrl, boolean primary) {
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", FontWeight.BOLD, 38));
        iconLbl.setTextFill(Color.web(iconColor));

        Label headingLbl = new Label(heading);
        headingLbl.setFont(Font.font("System", FontWeight.BOLD, 20));
        headingLbl.setTextFill(Color.web(iconColor));
        headingLbl.setWrapText(true);
        headingLbl.setTextAlignment(TextAlignment.CENTER);
        headingLbl.setMaxWidth(480);

        Label bodyLbl = new Label(body);
        bodyLbl.setFont(Font.font("System", 14));
        bodyLbl.setTextFill(Color.web(bodyColor));
        bodyLbl.setWrapText(true);
        bodyLbl.setTextAlignment(TextAlignment.CENTER);
        bodyLbl.setMaxWidth(460);

        VBox inner = new VBox(12, iconLbl, headingLbl, bodyLbl);
        inner.setAlignment(Pos.CENTER);

        if (reference != null) {
            Label refLbl = new Label(reference);
            refLbl.setFont(Font.font("System", 11));
            refLbl.setTextFill(Color.web(iconColor));
            inner.getChildren().add(refLbl);
        }

        Button nhsBtn = new Button(btnText);
        nhsBtn.getStyleClass().add(primary ? "primary-button" : "secondary-button");
        nhsBtn.setMinHeight(44);
        nhsBtn.setPrefWidth(310);
        nhsBtn.setOnAction(e -> openUrl(nhsUrl));
        inner.getChildren().add(nhsBtn);

        VBox card = new VBox(inner);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 40, 36, 40));
        card.setMaxWidth(580);
        card.setStyle("-fx-background-color: " + bg + ";"
                + " -fx-background-radius: 18;"
                + " -fx-border-color: " + border + ";"
                + " -fx-border-width: 1.5;"
                + " -fx-border-radius: 18;"
                + " -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 24, 0.15, 0, 6);");
        return card;
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private BorderPane buildHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("app-header");

        headerBackBtn = new Button("\u2190 Back");
        headerBackBtn.getStyleClass().add("secondary-button");

        Label title = new Label("Book an STI Test");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#0F172A"));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(180);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: #2563EB;");

        stepLabel = new Label("Step 1 of " + TOTAL_STEPS);
        stepLabel.setFont(Font.font("System", 12));
        stepLabel.setTextFill(Color.web("#64748B"));

        VBox progressGroup = new VBox(3, stepLabel, progressBar);
        progressGroup.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        spacer.setPrefWidth(100);

        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        VBox centerBox = new VBox(4, title, progressGroup);
        centerBox.setAlignment(Pos.CENTER);

        header.setLeft(headerBackBtn);
        header.setCenter(centerBox);
        header.setRight(spacer);
        BorderPane.setAlignment(headerBackBtn, Pos.CENTER_LEFT);
        BorderPane.setAlignment(centerBox, Pos.CENTER);
        BorderPane.setAlignment(spacer, Pos.CENTER_RIGHT);
        return header;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void earlyFail(String gateName, String reason) {
        Result r = new Result();
        r.setPassed(false);
        r.setGateName(gateName);
        r.setAction(Action.CLINIC_REFER);
        r.setReason(reason);
        r.setEvaluatedAt(LocalDateTime.now());
        finalResult = r;
        advance(S_RESULT);
    }

    private Result passResult() {
        Result r = new Result();
        r.setPassed(true);
        r.setGateName("G_ALL_PASS");
        r.setAction(Action.HOME_TEST);
        r.setReason("You are eligible for home testing.");
        r.setEvaluatedAt(LocalDateTime.now());
        return r;
    }

    private void resetWizard() {
        age = 18; fraser = false; location = ""; symptomatic = false;
        assault = false; pep = false; preg = false; immuno = false;
        safeguarding = false; windowElap = false; recentKit = false;
        kitWeeks = 6; finalResult = null; history.clear();
        showStep(S_AGE);
    }

    private Button primaryBtn(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("primary-button");
        btn.setMinHeight(44);
        btn.setPrefWidth(220);
        return btn;
    }

    private VBox centred(javafx.scene.Node node) {
        VBox b = new VBox(node);
        b.setAlignment(Pos.CENTER);
        return b;
    }

    private VBox vbox(double spacing, javafx.scene.Node... nodes) {
        VBox b = new VBox(spacing);
        b.getChildren().addAll(nodes);
        b.setAlignment(Pos.CENTER);
        return b;
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            alert(Alert.AlertType.ERROR, "Unable to open browser", "Please visit manually:\n" + url);
        }
    }

    private void alert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
