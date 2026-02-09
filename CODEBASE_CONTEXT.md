# SafeX (safex) - Codebase context

SafeX is a Java 23 + JavaFX 25 desktop app built with Maven (JPMS module: `safex`).

The app uses a simple MVC + state-router approach:

- `App` is a small launcher (sets JVM properties then starts JavaFX).
- `FxApp` is the JavaFX `Application` entrypoint and bootstraps `AppModel`, `AppView`, `AppController`.
- `AppModel` holds the current `State` and the current user (username).
- `AppController` mutates `AppModel` and tells `AppView` to re-render.
- `AppView` maps `State` -> a `Page` implementation and sets a fresh `Scene` on the primary `Stage`.
- Each `Page` implements `Page#createScene()` and creates fresh nodes every time.
- Navigation is done via `AppController` methods (no `Scene.setRoot(...)` swapping).

## Build + run

- Compile: `mvn clean compile`
- Run (recommended): `mvn clean javafx:run`
- Alternative run (exec plugin): `mvn exec:java`

## Runtime data / local-only

- SQLite file: `useraccounts.db` (gitignored) created in the repo root.
- Email credentials: `src/main/resources/config.properties` (gitignored) read by `EmailService`.

## Packages and key files

### `app/` (boot + MVC)

- `src/main/java/app/App.java`
  - Launcher (`public static void main`).
  - Sets `System.setProperty("prism.order", "sw")` unless already provided (software rendering fallback for Windows blank-window issues on some GPU/driver setups).
  - Calls `Application.launch(FxApp.class, args)`.

- `src/main/java/app/FxApp.java`
  - JavaFX entrypoint (`extends Application`).
  - `start(Stage primaryStage)`:
    - `DatabaseHandler.initialiseDatabase()`
    - Sets the window title and minimum size (900x650)
    - Wires `AppModel` + `AppView` + `AppController`
    - Calls `controller.updateView()` to render the initial state (Login).

- `src/main/java/app/AppModel.java`
  - Holds:
    - `State state` (defaults to `State.Login`)
    - `String currentUser`
  - Key methods: `getState()`, `setState(State)`, `getCurrentUser()`, `setCurrentUser(String)`

- `src/main/java/app/AppController.java`
  - Navigation + auth actions.
  - Key methods:
    - `updateView()`
    - `getCurrentUser()`
    - `attemptLogin(username, password)`
    - `attemptRegister(username, password)`
    - `goToLogin()`, `goToRegister()`, `goToDashboard()`, `goToNotify()`, `goToSettings()`, `goToLegalOnboard()`

- `src/main/java/app/AppView.java`
  - State router. `updateView(State)`:
    - Dispatches to the JavaFX Application Thread via `Platform.runLater(...)` when needed.
    - Creates the matching `Page`, calls `createScene()`, then `primaryStage.setScene(scene)`.
    - Applies the shared stylesheet via `AppStyles.apply(scene)`.

### `app/model/` (state + persistence)

- `src/main/java/app/model/State.java`
  - Enum: `Login`, `Register`, `Dashboard`, `Notify`, `Settings`, `LegalOnboard`

- `src/main/java/app/model/DatabaseHandler.java`
  - SQLite helpers.
  - DB URL: `jdbc:sqlite:useraccounts.db`
  - Creates table `users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)`
  - Key methods:
    - `initialiseDatabase()`
    - `registerUser(username, password)` -> boolean
    - `checkLogin(username, password)` -> boolean
  - Note: passwords are stored in plaintext (no hashing yet).

- Other model scaffolding:
  - `Account.java`, `UserAccount.java`, `PrivacySettings.java`, `DataRetentionType.java`

### `app/view/` (JavaFX pages)

All pages implement `Page#createScene()` and receive an `AppController` so navigation goes through controller methods.

- `src/main/java/app/view/Page.java`
  - Interface: `Scene createScene()`

- `src/main/java/app/view/AppStyles.java`
  - Shared UI constants + stylesheet loader.
  - Defines default scene size: 900x650.
  - `apply(Scene)` attaches `src/main/resources/styles/app.css` to the scene.

- `src/main/java/app/view/LoginPage.java`
  - Login form.
  - Calls `controller.attemptLogin(...)` on submit.
  - "Sign Up" link calls `controller.goToRegister()`.

- `src/main/java/app/view/RegisterPage.java`
  - Registration form.
  - Calls `controller.attemptRegister(...)` on submit.
  - "Log In" link calls `controller.goToLogin()`.

- `src/main/java/app/view/DashboardPage.java`
  - Main hub (after login).
  - Cards:
    - Notify Partners -> `controller.goToNotify()`
    - Privacy & Settings -> `controller.goToSettings()`
    - Terms & Conditions -> `controller.goToLegalOnboard()`
    - Health Resources -> opens NHS URL via `java.awt.Desktop`
  - Header has Logout -> `controller.goToLogin()`.

- `src/main/java/app/view/PartnerNotifyPage.java`
  - Partner notification flow.
  - Users add recipient rows (name optional, email required).
  - "Notify" -> confirmation view -> sends one email per recipient using `EmailService.sendNotification(...)`.
  - Home button (Home.png) returns to dashboard via `controller.goToDashboard()`.

- `src/main/java/app/view/SettingsPage.java`
  - Privacy/settings info cards (dialogs).
  - Home button returns to dashboard via `controller.goToDashboard()`.
  - Has a "Read full Terms & Conditions" button -> `controller.goToLegalOnboard()`.

- `src/main/java/app/view/LegalOnboardPage.java`
  - Privacy + Terms summaries.
  - Buttons to open bundled PDFs (`privacy.pdf`, `tcs.pdf`) via `PdfOpener.openResourcePdf(...)`.
  - Home button and "I Agree" both navigate back to dashboard via `controller.goToDashboard()`.

### `app/service/` (email)

- `src/main/java/app/service/EmailService.java`
  - `sendNotification(recipientName, recipientEmail, senderName)` -> boolean
  - Reads SMTP credentials from classpath resource `config.properties`:
    - `email.address`
    - `email.password`
  - Uses Gmail SMTP (`smtp.gmail.com:587` with STARTTLS).

### `app/util/` (PDF helper)

- `src/main/java/app/util/PdfOpener.java`
  - `openResourcePdf(resourceName)` copies the resource to a temp file and opens it via `Desktop.open(...)`.

## JPMS module descriptor

- `src/main/java/module-info.java`
  - Declares module `safex`, requires JavaFX + `java.sql` + `java.desktop` + `jakarta.mail`, and exports app packages.

## Resources (`src/main/resources/`)

- Shared theme
  - `styles/app.css` - global stylesheet used by all pages/scenes.
- `images/`
  - `Bell.png`, `+.png`, `deleteIcon.png`, `Home.png`
- PDFs
  - `privacy.pdf`, `tcs.pdf`
- Local-only (gitignored)
  - `config.properties` (SMTP credentials)
- Also present in resources (may be redundant with Maven deps)
  - `slf4j-api.jar`, `slf4j-simple.jar`, `sqlite-jdbc.jar`

## Notes / known issues

- `pom.xml` declares `sqlite-jdbc` twice with different versions (Maven warns).
- Do not commit real SMTP credentials or the local SQLite DB file (both are gitignored).
