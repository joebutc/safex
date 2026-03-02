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

---

## STI Test Tracker - Feature Summary

### New files

| File | Purpose |
|---|---|
| `src/main/java/app/model/StiTestEntry.java` | Immutable model for a single test record (`id`, `username`, `testDate`, `stiType`, `result`, `notes`) |
| `src/main/java/app/view/StiTestTrackerPage.java` | Patient test log UI: colour-coded result badges, date/type/notes, delete per entry, `+ Log New Test` dialog |

### Modified files

| File | What changed |
|---|---|
| `src/main/java/app/model/State.java` | Added `StiTestTracker` |
| `src/main/java/app/model/DatabaseHandler.java` | Added table `sti_tests` + `test_permissions`; methods: `addStiTest`, `getStiTests`, `deleteStiTest`, `getLatestTestDate`, `getTestPermissions`, `saveTestPermissions`, `getReminderIntervalMonths` |
| `src/main/java/app/AppController.java` | Added STI CRUD delegates; `shouldShowReminder()`, `getReminderMessage()`, `getTestPermissions()`, `saveTestPermissions()`, `getReminderIntervalMonths()` |
| `src/main/java/app/AppView.java` | Added route for `StiTestTracker` |
| `src/main/java/app/view/DashboardPage.java` | Added `Track Your Tests` card; notification bell with red-dot badge |
| `src/main/java/app/view/SettingsPage.java` | Added `Testing data permissions` card/dialog (`allow_reminders`, `allow_share_with_doctor`, reminder frequency selector) |

### Reminder logic

`AppController.shouldShowReminder()`:
- Returns `false` if `allow_reminders = false` (or no DB row)
- Returns `true` if no STI tests have ever been logged
- Returns `true` if `LocalDate.now().isAfter(lastTestDate.plusMonths(interval))`
- Interval is stored per-user in `test_permissions.reminder_interval_months` (default 3)

---

## Treatment Tracker - Feature Summary

### New files

| File | Purpose |
|---|---|
| `src/main/java/app/model/TreatmentEntry.java` | Treatment model (`medicationName`, `dose`, `frequency`, `quantityRemaining`, `lowStockThreshold`, `startDate`, `notes`, `active`, `source`, `patientNote`) with helpers: `dosesPerDay()`, `daysRemaining()`, `isLowStock()`, `isClinicIssued()` |
| `src/main/java/app/view/TreatmentTrackerPage.java` | Patient treatment UI: visually distinct clinic-issued cards (blue accent, read-only) vs patient-entered cards (editable); split dose input (amount + unit ComboBox); "Note to Clinic" for clinic-issued entries |
| `src/main/java/app/view/ClinicPatientTreatmentPage.java` | Clinic treatment management UI: permission check (`allow_clinic_access`), issue/update/discontinue prescriptions; shows source badge ("Clinic-prescribed" / "Self-reported") and patient notes; split dose input |

### Modified files

| File | What changed |
|---|---|
| `src/main/java/app/model/State.java` | Added `TreatmentTracker` and `ClinicPatientTreatment` |
| `src/main/java/app/model/DatabaseHandler.java` | Added tables `treatment_plans` + `treatment_permissions`; methods: `addTreatment`, `getTreatments`, `updateTreatmentQuantity`, `deleteTreatment` (soft delete), `updateTreatmentPlan`, `getPatientTreatmentsForClinic`, `getTreatmentPermissions`, `saveTreatmentPermissions`, `updatePatientNote` |
| `src/main/java/app/AppModel.java` | Added `clinicPatientUsername` field for clinic patient context |
| `src/main/java/app/AppController.java` | Added treatment CRUD and clinic treatment delegates; `getPendingNotifications()` aggregates STI-overdue + treatment low-stock notifications |
| `src/main/java/app/AppView.java` | Added routes for `TreatmentTracker` and `ClinicPatientTreatment` |
| `src/main/java/app/view/DashboardPage.java` | Added `My Treatment Plan` card; upgraded bell to numeric badge count |
| `src/main/java/app/view/SettingsPage.java` | Added `Treatment data permissions` card/dialog (`allow_treatment_reminders`, `allow_clinic_access`) |
| `src/main/java/app/view/ClinicDashboardPage.java` | Added `Patient Treatments` card with username lookup → opens `ClinicPatientTreatmentPage` |

### Clinic-issued vs patient-entered treatment cards

`TreatmentEntry.source` (`"clinic"` or `"patient"`) drives two distinct card styles in `TreatmentTrackerPage`:

| Source | Card style | Available actions |
|---|---|---|
| `"clinic"` | Blue left accent bar (`#2563EB`), light-blue tint (`#EFF6FF`), "Prescribed by your doctor" badge | Log Dose, Refill, Note to Clinic |
| `"patient"` | White card (standard) | Log Dose, Refill, Edit, Remove |

- **Note to Clinic**: TextArea dialog on a clinic-issued card lets the patient write a message stored in `treatment_plans.patient_note`. Clinics see this note in purple on `ClinicPatientTreatmentPage`.
- `isClinicIssued()` convenience method on `TreatmentEntry` returns `"clinic".equals(source)`.

### Dose split input

Both patient and clinic forms split the dose field into two controls:
- Amount: plain `TextField` (e.g., `"200"`)
- Unit: `ComboBox` with options defined in `TreatmentTrackerPage.DOSE_UNITS`:
  `tablet(s), capsule(s), mg, mcg, g, ml, IU, patch(es), drop(s)`

On save the value is concatenated as `"{amount} {unit}"` (same format as before).
`parseDoseString(String dose)` splits on the first space to pre-populate the two fields when editing.

### Login / Register branding

Both `LoginPage` and `RegisterPage` use a split-screen layout:
- **Left panel** (310 px, `#0A0A0A`): `safex_logo.png` at 254×200 (ratio preserved) + context-aware tagline in `#94A3B8`. Dark panel background matches the logo's black background for seamless blending.
- **Right panel** (remaining width, `#F8FAFC`): the existing form card.

### Low-stock logic (`TreatmentEntry`)

| Frequency | `dosesPerDay()` |
|---|---|
| Once daily | 1.0 |
| Twice daily | 2.0 |
| Three times daily | 3.0 |
| Weekly | 1/7 ≈ 0.143 |
| As needed | 0.0 (never triggers alert) |

- `daysRemaining() = quantityRemaining / dosesPerDay()`; returns `Double.MAX_VALUE` for As needed.
- `isLowStock() = dosesPerDay() > 0 && daysRemaining() <= lowStockThreshold`

### Reminder bell behavior

- `getPendingNotifications()` returns a combined `List<String>` of:
  - Overdue STI test reminder (when `allow_reminders = true` and interval exceeded)
  - Low-stock treatment reminders for each active treatment where `allow_treatment_reminders = true`
- Bell badge count: `1`–`9` exact, `9+` above 9
- Bell click: one dialog listing all pending notifications

---

## Build + Run

- Compile: `mvn clean compile`
- Run (recommended): `mvn clean javafx:run`
- Alternative: `mvn exec:java`
- Run tests: `mvn test`

---

## Runtime Data / Local-Only

- SQLite DB file: `useraccounts.db` (repo root, gitignored)
- Email credentials: `src/main/resources/config.properties` (gitignored)

---

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
- `test_permissions` (`allow_reminders`, `allow_share_with_doctor`, `reminder_interval_months`)
- `treatment_plans` (soft-delete via `active` flag; `source` = `"patient"` or `"clinic"`; `patient_note` for patient-to-clinic messages)
- `treatment_permissions` (`allow_treatment_reminders`, `allow_clinic_access`)

### Controller behavior

`AppController` provides:
- Type-aware auth and routing (`STANDARD` vs `CLINIC`)
- STI test CRUD: `getStiTests`, `addStiTest`, `deleteStiTest`
- STI permissions: `getTestPermissions`, `saveTestPermissions`, `getReminderIntervalMonths`
- Reminder logic: `shouldShowReminder()`, `getReminderMessage()`
- Treatment CRUD (patient): `getTreatments`, `addTreatment`, `deleteTreatment`, `updateTreatmentQuantity`, `updatePatientNote`
- Treatment CRUD (clinic-on-patient): `getPatientTreatmentsForClinic`, `addTreatmentForPatient`, `updateTreatmentPlanForPatient`, `deleteTreatmentForPatient`
- Treatment permissions: `getTreatmentPermissions`, `saveTreatmentPermissions`
- Aggregated notifications: `getPendingNotifications()`

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
  - P&S cards: Privacy Overview, Data Retention, Manage Your Data, Testing Data Permissions, Treatment Data Permissions
- Legal: `LegalOnboardPage`
- Schedule entry page: `DoctorSchedule` (FXML-backed)

### Scheduling feature (`src/main/java/app/Schedule`)

- `CalendarController` for `monthCalenderUI.fxml`
- `DoctorScheduleController` for `dayCalender.fxml`

---

## Module Descriptor

- `src/main/java/module-info.java`
  - Requires: `javafx.controls` (transitive), `javafx.fxml`, `java.desktop`, `java.sql`, `jakarta.mail`
  - Exports: `app`, `app.model`, `app.view`, `app.service`, `app.Schedule`
  - Opens: `app.view`, `app.Schedule` to `javafx.fxml`

---

## Resources (`src/main/resources`)

- Styles: `styles/app.css`
- Images: `Bell.png`, `+.png`, `deleteIcon.png`, `Home.png`, `arrow_back.png`, `safex_logo.png`
- FXML: `fxmlFiles/monthCalenderUI.fxml`, `fxmlFiles/dayCalender.fxml`
- PDFs: `privacy.pdf`, `tcs.pdf`
- Local-only: `config.properties`

---

## Test Coverage (`src/test/java`)

Tests use JUnit Jupiter 5.11. DB tests connect to `useraccounts.db` at the project root (same as runtime) using isolated test usernames (`__test_*__`) with `@BeforeEach`/`@AfterEach` hard-delete cleanup.

| File | Tests | What is covered |
|---|---|---|
| `app/service/EmailServiceTest.java` | 9 | `EmailService.sendNotification()` — valid/invalid inputs, HTML escaping, display name formatting |
| `app/service/EmailServiceErrorTests.java` | 3 | `EmailService.loadCredentials()` — expects `IllegalStateException` when `config.properties` absent *(fails locally because file exists)* |
| `app/model/StiTestTrackerTests.java` | 21 | `StiTestEntry` getters; `DatabaseHandler`: `addStiTest`, `getStiTests`, `deleteStiTest` (incl. cross-user guard), `getLatestTestDate`, `getTestPermissions`, `saveTestPermissions` (upsert), `getReminderIntervalMonths` |
| `app/model/TreatmentTrackerTests.java` | 41 | `TreatmentEntry`: `dosesPerDay()` all frequencies, `daysRemaining()`, `isLowStock()` (at/below/above threshold, As needed); `DatabaseHandler`: `addTreatment`, `getTreatments`, `updateTreatmentQuantity` (cross-user guard), `deleteTreatment` (soft delete + cross-user guard), `updateTreatmentPlan`, `getPatientTreatmentsForClinic`, `getTreatmentPermissions`, `saveTreatmentPermissions` |
| `app/NotificationAndPermissionsTests.java` | 39 | `AppController`: `shouldShowReminder()` (disabled/no row/recent/overdue/boundary/custom intervals), `getReminderMessage()` (null cases, message content), test permissions round-trip, treatment permissions round-trip, `getReminderIntervalMonths`, `getPendingNotifications()` (empty cases, STI reminder, treatment low-stock, combined, excluded cases, message content) |

**Total: 113 tests — 110 pass, 3 pre-existing failures** (`EmailServiceErrorTests` expect missing `config.properties` but it exists in the local environment).

---

## Known Issues / Cleanup

- `pom.xml` declares `sqlite-jdbc` twice with different versions (3.45.1.0 and 3.46.0.0).
- Passwords are stored in plaintext in SQLite.
- Calendar controllers perform internal scene swaps via FXML navigation rather than routing through `AppController`.
- `EmailServiceErrorTests` fail locally because `config.properties` is present (tests assume it is absent).
