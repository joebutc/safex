# SafeX (safex) - Codebase context

SafeX is a Java 23 + JavaFX 25 desktop app built with Maven (JPMS module `safex`).

Architecture is MVC + state routing:
- `App` launches JavaFX (`FxApp`) and sets a Prism software-render fallback.
- `FxApp` bootstraps DB, stage, and MVC wiring.
- `AppModel` stores current app `State` and current username.
- `AppController` mutates model state and triggers view updates.
- `AppView` maps `State` to `Page` implementations and replaces the `Scene`.
- Each `Page` builds a fresh scene tree in `createScene()`.

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
  - Sets `prism.order=sw` when not already provided (Windows blank-window mitigation).
  - Calls `Application.launch(FxApp.class, args)`.

- `src/main/java/app/FxApp.java`
  - JavaFX `Application` entrypoint.
  - Initializes DB and stage (`SafeX`, min 900x650).
  - Wires `AppModel`, `AppView`, `AppController`.
  - Calls `controller.updateView()` for initial login page.

- `src/main/java/app/AppView.java`
  - State router from `State` enum to page classes.
  - Ensures updates run on JavaFX thread.
  - Routes standard vs clinic states to typed pages:
    - `Login` / `ClinicLogin` -> `LoginPage(..., LoginType.STANDARD/CLINIC)`
    - `Register` / `ClinicRegister` -> `RegisterPage(..., LoginType.STANDARD/CLINIC)`
    - `Dashboard` / `ClinicDashboard` -> `DashboardPage`
  - Applies shared CSS via `AppStyles.apply(scene)`.
  - Catches render exceptions and shows a diagnostic fallback scene.

- `src/main/java/app/view/AppStyles.java`
  - Scene defaults (`900x650`) and shared CSS attach (`/styles/app.css`).
  - Missing stylesheet is non-fatal (stderr warning).

### Model + persistence

- `src/main/java/app/model/State.java`
  - States: `Login`, `ClinicLogin`, `Register`, `ClinicRegister`, `Dashboard`, `ClinicDashboard`, `Notify`, `Settings`, `LegalOnboard`.

- `src/main/java/app/model/DatabaseHandler.java`
  - SQLite helper for auth and registration.
  - Creates tables:
    - `users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)`
    - `clinics(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)`
  - Selects target table by account type (`STANDARD` -> `users`, `CLINIC` -> `clinics`).
  - Methods:
    - `registerUser(username, password, accountType)`
    - `checkLogin(username, password, accountType)`
  - Note: passwords are still plaintext.

- Additional model classes:
  - `Account.java`, `UserAccount.java`, `ClinicAccount.java`, `PrivacySettings.java`, `DataRetentionType.java`.

### Controller behavior

- `src/main/java/app/AppController.java`
  - Auth methods are type-aware:
    - `attemptLogin(username, password, LoginType)`
    - `attemptRegister(username, password, LoginType)`
  - Navigation methods support both flows:
    - `goToLogin(LoginType)` + `goToLogin()`
    - `goToRegister(LoginType)` + `goToRegister()`
  - Clinic auth routes to `State.ClinicDashboard`; standard auth routes to `State.Dashboard`.

### UI pages (`src/main/java/app/view`)

- `LoginPage`
  - Contains `LoginType` enum: `STANDARD`, `CLINIC`.
  - Type-specific title/subtitle and register CTA.
  - Includes a portal-switch link:
    - Standard -> Clinic portal
    - Clinic -> Patient portal
  - Calls `controller.attemptLogin(..., loginType)`.

- `RegisterPage`
  - Type-specific labels/button text for standard vs clinic registration.
  - Calls `controller.attemptRegister(..., loginType)`.
  - Returns to matching login route using `controller.goToLogin(loginType)`.
  - Validation rules:
    - Username letters-only, no spaces.
    - Password min 8 with upper/lower/number/special.
    - Confirm-password match.

- `DashboardPage`
  - Main hub (currently reused for both standard and clinic dashboards).
  - Cards: Notify, Settings, Terms, NHS resources.

- `PartnerNotifyPage`
  - Contact list editor (email required, name optional).
  - Async email sending via background threads.
  - Email syntax validation before send.

- `SettingsPage`
  - Privacy-related cards/dialogs.
  - Links to legal page and dashboard.

- `LegalOnboardPage`
  - Privacy + terms summary flow.
  - Opens bundled PDFs (`privacy.pdf`, `tcs.pdf`) via `PdfOpener`.

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
- `ClinicDashboard` currently reuses `DashboardPage` (no separate clinic dashboard page yet).
- Real credentials and local DB must stay uncommitted (`config.properties`, `useraccounts.db`).
