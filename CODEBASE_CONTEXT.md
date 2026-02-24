# SafeX (safex) - Codebase context

SafeX is a Java 23 + JavaFX 25 desktop app built with Maven (JPMS module `safex`).

Architecture is MVC + state routing:
- `App` launches JavaFX (`FxApp`) and sets a Prism software-render fallback.
- `FxApp` bootstraps DB, stage, and MVC wiring.
- `AppModel` stores current app `State` and current username.
- `AppController` mutates model state and triggers view updates.
- `AppView` maps `State` to `Page` implementations and replaces the `Scene`.
- Each `Page` builds a new scene tree in `createScene()`.

## Build + run

- Compile: `mvn clean compile`
- Run (recommended): `mvn clean javafx:run`
- Alternative: `mvn exec:java`

## Runtime data / local-only

- SQLite DB file: `useraccounts.db` (repo root, gitignored).
- SMTP credentials: `src/main/resources/config.properties` (gitignored), read by `EmailService`.

## Key files

### Boot + routing

- `src/main/java/app/App.java`
  - Main launcher.
  - Sets `prism.order=sw` when not already provided to reduce Windows blank-window rendering issues.
  - Calls `Application.launch(FxApp.class, args)`.

- `src/main/java/app/FxApp.java`
  - JavaFX `Application` entrypoint.
  - Initializes DB and stage (`SafeX`, min 900x650).
  - Wires `AppModel`, `AppView`, `AppController`.
  - Calls `controller.updateView()` for initial login page.

- `src/main/java/app/AppView.java`
  - State router from `State` enum to page classes.
  - Ensures updates run on JavaFX thread.
  - Applies shared CSS via `AppStyles.apply(scene)`.
  - New safety behavior: catches page-render exceptions and shows a diagnostic fallback scene instead of leaving a blank window.

- `src/main/java/app/view/AppStyles.java`
  - Scene defaults (`900x650`) and shared CSS attach (`/styles/app.css`).
  - Missing stylesheet is now non-fatal (warns to stderr instead of throwing).

### Model + persistence

- `src/main/java/app/model/State.java`
  - States: `Login`, `Register`, `Dashboard`, `Notify`, `Settings`, `LegalOnboard`.

- `src/main/java/app/model/DatabaseHandler.java`
  - SQLite helper for user login/register.
  - Creates table:
    - `users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)`
  - Note: passwords are still stored in plaintext.

- Additional model classes:
  - `Account.java`, `UserAccount.java`, `PrivacySettings.java`, `DataRetentionType.java`.

### UI pages (`src/main/java/app/view`)

- `LoginPage`
  - Username/password login.
  - Enter key support.
  - Navigates to register page.

- `RegisterPage`
  - Registration with validation:
    - Username letters-only, no spaces.
    - Password min 8 with uppercase/lowercase/number/special char.
    - Confirm-password check.
  - Navigates back to login.

- `DashboardPage`
  - Cards for Notify, Settings, Legal, and NHS resources.
  - Logout support.

- `PartnerNotifyPage`
  - Contact list editor (email required, name optional).
  - Async email sending via background threads.
  - Email syntax validation before send.
  - Per-recipient success/failure alerts and completion summary.

- `SettingsPage`
  - Privacy-related cards and dialogs.
  - Links to legal page and back to dashboard.

- `LegalOnboardPage`
  - Privacy policy + terms summary flow.
  - Opens bundled PDFs (`privacy.pdf`, `tcs.pdf`) via `PdfOpener`.
  - Agreement navigates to dashboard.

### Services/util

- `src/main/java/app/service/EmailService.java`
  - Sends anonymous notification emails with Jakarta Mail.
  - Reads `email.address` and `email.password` from `config.properties`.
  - Uses Gmail SMTP (`smtp.gmail.com:587`, STARTTLS).

- `src/main/java/app/util/PdfOpener.java`
  - Copies bundled PDF resource to temp file and opens it with `Desktop.open(...)`.

## Module descriptor

- `src/main/java/module-info.java`
  - Requires: `javafx.controls`, `javafx.graphics` (transitive), `java.desktop`, `java.sql`, `jakarta.mail`.
  - Exports: `app`, `app.model`, `app.view`, `app.service`.

## Resources (`src/main/resources`)

- `styles/app.css`
- `images/Bell.png`, `images/+.png`, `images/deleteIcon.png`, `images/Home.png`
- `privacy.pdf`, `tcs.pdf`
- Local-only: `config.properties` (gitignored)
- Also present but likely redundant with Maven deps: `slf4j-api.jar`, `slf4j-simple.jar`, `sqlite-jdbc.jar`

## Known issues / cleanup

- `pom.xml` currently declares `sqlite-jdbc` twice with different versions.
- Real credentials and local DB must stay uncommitted (`config.properties`, `useraccounts.db`).
