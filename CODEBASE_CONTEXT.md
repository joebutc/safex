# SafeX (safex) - Codebase context

SafeX is a Java 23 + JavaFX 25 desktop app built with Maven (JPMS module `safex`).

Architecture is MVC + state routing:
- `App` launches JavaFX (`FxApp`) and sets a Prism software-render fallback.
- `FxApp` bootstraps DB, stage, and MVC wiring.
- `AppModel` stores current app `State`, current username, and whether the active session is clinic-mode.
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
    - `Dashboard` -> `DashboardPage`
    - `ClinicDashboard` -> `ClinicDashboardPage`
    - `ClinicSchedule` -> `DoctorSchedule`
  - Applies shared CSS via `AppStyles.apply(scene)`.
  - Catches render exceptions and shows a diagnostic fallback scene.

- `src/main/java/app/view/AppStyles.java`
  - Scene defaults (`900x650`) and shared CSS attach (`/styles/app.css`).
  - Missing stylesheet is non-fatal (stderr warning).

### Model + persistence

- `src/main/java/app/model/State.java`
  - States: `Login`, `ClinicLogin`, `Register`, `ClinicRegister`, `Dashboard`, `ClinicDashboard`, `ClinicSchedule`, `Notify`, `Settings`, `LegalOnboard`.

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
    - `goToClinicSchedule()`
  - Clinic auth routes to `State.ClinicDashboard`; standard auth routes to `State.Dashboard`.
  - `goToDashboard()` is session-aware and returns clinic users to clinic dashboard.

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
  - Main hub for patient users.
  - Cards: Notify, Settings, Terms, NHS resources.

- `ClinicDashboardPage`
  - Main hub for clinic users.
  - Cards: Schedule GUI and Terms & Conditions.
  - Schedule card routes to `ClinicSchedule`.

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

- `DoctorSchedule`
  - FXML-backed calendar entry page (`/fxmlFiles/monthCalenderUI.fxml`).
  - Loads monthly schedule UI via `FXMLLoader`.

### Scheduling feature (FXML)

- `src/main/java/app/Schedule/CalendarController.java`
  - Controls `monthCalenderUI.fxml`.
  - Renders month grid dynamically from `YearMonth`.
  - Supports previous/next month navigation.
  - Opens day view (`dayCalender.fxml`) when a date button is clicked.

- `src/main/java/app/Schedule/DoctorScheduleController.java`
  - Controls `dayCalender.fxml`.
  - Builds 09:00 to 17:00 slots in 30-minute intervals.
  - Supports back navigation to monthly calendar.

- Status:
  - Calendar pages are present and compile.
  - They are routed from `State.ClinicSchedule` and reachable from `ClinicDashboardPage`.

### Services/util

- `src/main/java/app/service/EmailService.java`
  - Sends anonymous notification emails with Jakarta Mail.
  - Reads `email.address` and `email.password` from `config.properties`.
  - Uses Gmail SMTP (`smtp.gmail.com:587`, STARTTLS).

- `src/main/java/app/util/PdfOpener.java`
  - Copies bundled PDF resource to temp file and opens it with `Desktop.open(...)`.

## Module descriptor

- `src/main/java/module-info.java`
  - Requires: `javafx.controls`, `javafx.fxml`, `java.desktop`, `java.sql`, `jakarta.mail`.
  - Exports: `app`, `app.model`, `app.view`, `app.service`, `app.Schedule`.
  - Opens: `app.view` and `app.Schedule` to `javafx.fxml`.

## Resources (`src/main/resources`)

- `styles/app.css`
- `images/Bell.png`, `images/+.png`, `images/deleteIcon.png`, `images/Home.png`
- `images/arrow_back.png`
- `privacy.pdf`, `tcs.pdf`
- `fxmlFiles/monthCalenderUI.fxml`, `fxmlFiles/dayCalender.fxml`
- Local-only: `config.properties` (gitignored)
- Also present but likely redundant with Maven deps: `slf4j-api.jar`, `slf4j-simple.jar`, `sqlite-jdbc.jar`

## Known issues / cleanup

- `pom.xml` currently declares `sqlite-jdbc` twice with different versions.
- Day/month schedule controllers currently handle navigation internally (FXML scene swaps), not through `AppController`.
- Real credentials and local DB must stay uncommitted (`config.properties`, `useraccounts.db`).
