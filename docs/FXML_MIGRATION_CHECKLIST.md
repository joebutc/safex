# FXML Migration Checklist

This checklist is the merge gate for moving `main` from Java-built JavaFX pages to the FXML-driven structure used on `jb-framework-tweaks-main-compliant(sade)`.

The goal is not just to "make it load in FXML". The goal is to preserve all working behaviour from `main`, avoid regressions, and move layout ownership into FXML only when the replacement is functionally equivalent.

## Automated Merge Gate

Merge requests targeting `main` must pass:

- `scripts/run-regression.sh`
- `mvn test`
- GitLab CI job `fxml-main-regression`

Automated tests are necessary but not sufficient. The manual checks below still apply because some parity risks are behavioural, visual, or multi-screen.

## Safest Migration Order

### 1. Boot and Routing

Replace `main`'s old MVC shell only after the new FXML router is stable.

- Source on `main`:
  - `src/main/java/app/FxApp.java`
  - `src/main/java/app/AppModel.java`
  - `src/main/java/app/AppView.java`
  - `src/main/java/app/view/Page.java`
- Target on FXML branch:
  - `src/main/java/app/App.java`
  - `src/main/java/app/AppController.java`
  - `src/main/java/app/model/State.java`
  - `src/main/java/app/view/AppStyles.java`

Merge gate:

- Standard login routes to patient dashboard.
- Clinic login routes to clinic dashboard.
- Logout always returns to the correct login scene.
- Back and home actions remain role-aware.

### 2. Low-Risk Shell Screens

These are the safest screens to move first because they mostly change layout structure, not domain logic.

- `src/main/resources/scenes/StandardLogin.fxml`
- `src/main/resources/scenes/StandardRegister.fxml`
- `src/main/resources/scenes/ClinicLogin.fxml`
- `src/main/resources/scenes/ClinicRegister.fxml`
- `src/main/resources/scenes/Dashboard.fxml`
- `src/main/resources/scenes/ClinicDashboard.fxml`
- `src/main/resources/scenes/LegalOnBoard.fxml`
- `src/main/resources/scenes/Settings.fxml`

Controller targets:

- `src/main/java/app/view/StandardLoginPage.java`
- `src/main/java/app/view/StandardRegisterPage.java`
- `src/main/java/app/view/ClinicLoginPage.java`
- `src/main/java/app/view/ClinicRegisterPage.java`
- `src/main/java/app/view/DashboardPage.java`
- `src/main/java/app/view/ClinicDashboardPage.java`
- `src/main/java/app/view/LegalOnboardPage.java`
- `src/main/java/app/view/SettingsPage.java`

Merge gate:

- All buttons remain wired to the same actions as on `main`.
- Notification bell still works.
- Reminder banner still works.
- Clinic and patient dashboards remain visually distinct.

### 3. Shared FXML Fragments

Move repeated row and dialog UI into FXML partials before touching high-risk workflows.

- `src/main/resources/scenes/ScheduleSlotRow.fxml`
- `src/main/resources/scenes/StiTestRow.fxml`
- `src/main/resources/scenes/CalendarDayButton.fxml`
- `src/main/resources/scenes/PartnerContactRow.fxml`
- `src/main/resources/scenes/PartnerNotifyConfirm.fxml`
- `src/main/resources/scenes/DataAccessDialog.fxml`
- `src/main/resources/scenes/TestPermissionsDialog.fxml`
- `src/main/resources/scenes/TreatmentPermissionsDialog.fxml`
- `src/main/resources/scenes/PatientLookupDialog.fxml`

Controller targets:

- `src/main/java/app/view/ScheduleSlotRowController.java`
- `src/main/java/app/view/StiTestRowController.java`
- `src/main/java/app/view/CalendarDayButtonController.java`
- `src/main/java/app/view/PartnerContactRowController.java`
- `src/main/java/app/view/PartnerNotifyConfirmController.java`
- `src/main/java/app/view/DataAccessDialogController.java`
- `src/main/java/app/view/TestPermissionsDialogController.java`
- `src/main/java/app/view/TreatmentPermissionsDialogController.java`
- `src/main/java/app/view/PatientLookupDialogController.java`

Merge gate:

- No `NullPointerException` from missing `fx:id` fields.
- Shared CSS classes resolve correctly on all extracted row/dialog scenes.
- Opening a dialog does not lose stylesheet application.

### 4. Medium-Risk Feature Screens

These screens are good FXML targets, but only after shell routing and shared fragments are already stable.

- `src/main/resources/scenes/PartnerNotify.fxml`
- `src/main/resources/scenes/StiTracker.fxml`
- `src/main/resources/scenes/TreatmentTracker.fxml`
- `src/main/resources/scenes/PatientTreatment.fxml`
- `src/main/resources/scenes/ClinicSchedule.fxml`
- `src/main/resources/scenes/Calender.fxml`

Controller targets:

- `src/main/java/app/view/PartnerNotifyPage.java`
- `src/main/java/app/view/StiTestTrackerPage.java`
- `src/main/java/app/view/TreatmentTrackerPage.java`
- `src/main/java/app/view/ClinicPatientTreatmentPage.java`
- `src/main/java/app/view/DoctorSchedule.java`
- `src/main/java/app/view/CalendarPage.java`

Merge gate:

- Partner notify opens, adds contacts, and submits without FXML load errors.
- STI tracker add/delete/reload works.
- Treatment tracker still supports add, edit, refill, delete, and note flows.
- Clinic patient treatment page still enforces patient permission checks.
- Clinic schedule allows offering and removing slots, then persisting them.

### 5. High-Risk Behavioural Migrations

These are not "just FXML". They change business rules or data shape, so they need explicit parity work.

#### Appointment System

`main` uses:

- `src/main/java/app/model/ScheduleDatabaseHandler.java`
- `src/main/java/app/model/AppointmentSlot.java`
- `src/main/java/app/Schedule/PatientCalendarPage.java`
- `src/main/java/app/Schedule/PatientSchedulePage.java`

The FXML branch uses:

- `src/main/java/app/view/BookAppointmentPage.java`
- `src/main/java/app/model/DatabaseHandler.java` appointment and `clinic_availability` methods

Required parity from `main`:

- Patient must still choose a clinic, not just a date.
- Appointment booking must remain clinic-specific.
- Clinic-offered slots must not become anonymous global slots.
- Existing appointment data in `useraccounts.db` must migrate safely.

Do not merge the FXML appointment flow into `main` unless those points are preserved.

#### STI Booking Location Step

`main` enforces UK postcode validation through:

- `src/main/java/app/util/PostcodeValidator.java`
- `src/test/java/app/util/PostcodeValidatorTest.java`

The FXML branch currently relaxes that step to "postcode or town".

Required parity from `main` unless product requirements changed:

- Keep postcode validation.
- Keep the validator tests.
- Only broaden the rule if that change is explicitly accepted.

## Manual Regression Checklist Before Merging to `main`

Run through these flows after the automated test suite passes:

1. Standard login, register, logout.
2. Clinic login, register, logout.
3. Patient dashboard loads with reminder banner and notification bell.
4. Clinic dashboard loads with clinic-only actions.
5. Partner notify opens and sends without FXML load errors.
6. STI booking completes end to end, including location validation.
7. STI tracker add and delete flow works.
8. Treatment tracker add, edit, refill, delete, and note flow works.
9. Clinic patient treatment page respects patient permission settings.
10. Clinic schedule can offer and remove slots, then confirm changes.
11. Patient appointment booking still supports clinic-specific selection and booking.
12. Existing SQLite data opens without schema errors.

## Merge Recommendation

Use `jb-framework-tweaks-main-compliant(sade)` as the structural reference branch, not as an automatic one-shot replacement for `main`.

Safe merge strategy:

1. Port the FXML router and low-risk shells first.
2. Port shared fragments next.
3. Preserve `main` behaviour for appointments and postcode validation until equivalent FXML replacements exist.
4. Keep `mvn test` green at each stage.
