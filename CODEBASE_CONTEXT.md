# SafeX (safex) - Codebase Context

SafeX is a Java 23 + JavaFX 25 desktop app built with Maven (JPMS module `safex`).

Architecture is MVC + state routing:
- `App` launches JavaFX (`FxApp`) and sets a software-render fallback (`prism.order=sw`) when needed.
- `FxApp` initializes DB + stage and wires `AppModel`, `AppView`, `AppController`.
- `AppModel` stores: current `State`, current username, clinic-session flag, clinic patient lookup username.
- `AppController` mutates model state, delegates persistence calls, and triggers re-render.
- `AppView` maps `State` to page classes and swaps the scene root.
- Current routed states:
  - `Login`, `ClinicLogin`
  - `Register`, `ClinicRegister`
  - `Dashboard`, `ClinicDashboard`
  - `ClinicSchedule`
  - `Notify`
  - `Settings`
  - `LegalOnboard`
  - `StiTestTracker`
  - `TreatmentTracker`
  - `ClinicPatientTreatment`

## Treatment Tracker - Full Feature Summary

### New files

| File | Purpose |
|---|---|
| `src/main/java/app/model/TreatmentEntry.java` | Treatment model (`medicationName`, `dose`, `frequency`, `quantityRemaining`, `lowStockThreshold`, `startDate`, `notes`, `active`) with helpers: `dosesPerDay()`, `daysRemaining()`, `isLowStock()` |
| `src/main/java/app/view/TreatmentTrackerPage.java` | Patient treatment UI: medication cards with supply/days remaining and actions (`Log Dose`, `Refill`, `Edit`, `Remove`); low-stock cards highlighted |
| `src/main/java/app/view/ClinicPatientTreatmentPage.java` | Clinic treatment management UI: patient lookup flow, permission check (`allow_clinic_access`), issue/update/discontinue prescriptions |

### Modified files

| File | What changed |
|---|---|
| `src/main/java/app/model/State.java` | Added `TreatmentTracker` and `ClinicPatientTreatment` |
| `src/main/java/app/model/DatabaseHandler.java` | Added tables `treatment_plans` + `treatment_permissions`; added methods: `addTreatment`, `getTreatments`, `updateTreatmentQuantity`, `deleteTreatment`, `updateTreatmentPlan`, `getPatientTreatmentsForClinic`, `getTreatmentPermissions`, `saveTreatmentPermissions` |
| `src/main/java/app/AppModel.java` | Added `clinicPatientUsername` field for clinic patient context |
| `src/main/java/app/AppController.java` | Added treatment CRUD and clinic treatment delegates; added `getPendingNotifications()` to aggregate STI-overdue + treatment low-stock notifications |
| `src/main/java/app/AppView.java` | Added routes for `TreatmentTracker` and `ClinicPatientTreatment` |
| `src/main/java/app/view/DashboardPage.java` | Added `My Treatment Plan` card and upgraded bell with pending notification badge count |
| `src/main/java/app/view/SettingsPage.java` | Added treatment permissions card/dialog (`allow_treatment_reminders`, `allow_clinic_access`) |
| `src/main/java/app/view/ClinicDashboardPage.java` | Added `Patient Treatments` card with username lookup to open clinic patient treatment page |

### Reminder bell behavior

- `getPendingNotifications()` returns a combined `List<String>` of:
  - overdue STI test reminders
  - low-stock treatment reminders for active treatments where `allow_treatment_reminders = true`
- Bell badge count:
  - `1` to `9` shows exact count
  - `9+` for anything above 9
- Bell click behavior:
  - opens one dialog listing all pending notifications in a single combined view

## Build + Run

- Compile: `mvn clean compile`
- Run (recommended): `mvn clean javafx:run`
- Alternative: `mvn exec:java`

## Runtime Data / Local-Only

- SQLite DB file: `useraccounts.db` (repo root, gitignored)
- Email credentials: `src/main/resources/config.properties` (gitignored)

## Key Files

### Boot + routing

- `src/main/java/app/App.java`
- `src/main/java/app/FxApp.java`
- `src/main/java/app/AppView.java`
- `src/main/java/app/view/AppStyles.java`

### Model + persistence

- `src/main/java/app/model/State.java`
- `src/main/java/app/model/DatabaseHandler.java`
- `src/main/java/app/model/StiTestEntry.java`
- `src/main/java/app/model/TreatmentEntry.java`
- Other model classes: `Account`, `UserAccount`, `ClinicAccount`, `PrivacySettings`, `DataRetentionType`

`DatabaseHandler` tables:
- `users`
- `clinics`
- `sti_tests`
- `test_permissions` (includes reminder interval)
- `treatment_plans`
- `treatment_permissions`

### Controller behavior

`AppController` provides:
- Type-aware auth and routing (`STANDARD` vs `CLINIC`)
- STI test operations and permissions
- Treatment operations for patient view
- Treatment operations for clinic-on-patient view
- Aggregated notification generation via `getPendingNotifications()`

### UI pages (`src/main/java/app/view`)

- Patient dashboard: `DashboardPage`
  - cards: Notify Partners, Track Your Tests, My Treatment Plan, Privacy & Settings, Terms & Conditions, Health Resources
- Clinic dashboard: `ClinicDashboardPage`
  - cards: Schedule GUI, Patient Treatments, Terms & Conditions
- STI tracking: `StiTestTrackerPage`
- Treatment tracking (patient): `TreatmentTrackerPage`
- Treatment management (clinic): `ClinicPatientTreatmentPage`
- Notify: `PartnerNotifyPage`
- Settings: `SettingsPage`
- Legal: `LegalOnboardPage`
- Schedule entry page: `DoctorSchedule` (FXML-backed)

### Scheduling feature (`src/main/java/app/Schedule`)

- `CalendarController` for `monthCalenderUI.fxml`
- `DoctorScheduleController` for `dayCalender.fxml`

## Module Descriptor

- `src/main/java/module-info.java`
  - Requires: `javafx.controls` (transitive), `javafx.fxml`, `java.desktop`, `java.sql`, `jakarta.mail`
  - Exports: `app`, `app.model`, `app.view`, `app.service`, `app.Schedule`
  - Opens: `app.view`, `app.Schedule` to `javafx.fxml`

## Resources (`src/main/resources`)

- Styles: `styles/app.css`
- Images: `Bell.png`, `+.png`, `deleteIcon.png`, `Home.png`, `arrow_back.png`
- FXML: `fxmlFiles/monthCalenderUI.fxml`, `fxmlFiles/dayCalender.fxml`
- PDFs: `privacy.pdf`, `tcs.pdf`
- Local-only: `config.properties`

## Known Issues / Cleanup

- `pom.xml` currently declares `sqlite-jdbc` twice with different versions.
- Passwords are still stored in plaintext in SQLite.
- Calendar controllers still perform internal scene swaps via FXML navigation rather than routing back through `AppController`.
