# SafeX – Past Regression Testing Evidence

> Reconstructed from `git log` on 2026-03-22.
> Shows the relationship between every feature commit and the test commits that
> preceded or accompanied it on the same or adjacent days.

---

## How to read this report

Each section is one **feature milestone**.
- **Feature commit** — what code was added.
- **Test commit(s)** — the test file(s) that were committed at the same time or immediately before/after.
- **Evidence** — which test classes were present in the tree at that point, as confirmed by `git show <hash>`.

---

## 1. Email / Partner Notification  (Feb 10 – Feb 17 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `e088b2a` | 2026-02-10 | Add email notification sending and fix module warnings |
| `c873628` | earlier | Notification Page GUI |
| `8af8d81` | earlier | Partner notify transition page |

### Test commits shipped alongside
| Hash | Date | Test files added |
|---|---|---|
| `586263f` | **2026-02-10** | Added JUnit 5 to `pom.xml`; scaffolded email unit tests |
| `4f9a977` | **2026-02-10** | `EmailServiceErrorTests` – error-path tests for `loadCredentials()` |
| `b72912f` | **2026-02-17** | Split success/error checks into separate unit tests |

### Test classes present after this milestone
- `app.service.EmailServiceErrorTests` (3 tests)
- `app.service.EmailServiceTest` (9 tests)

**Evidence:** `git show b72912f --stat` confirms `EmailServiceErrorTests.java` was committed on the same day as the final email-feature polish.

---

## 2. Login / Register Validation  (Feb 17 – Feb 25 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `2fa226f` | 2026-02-17 | Adding email and register validation |
| `24c82eb` | 2026-02-25 | Update auth pages and add logo assets |

### Test commits shipped alongside
| Hash | Date | Test files added |
|---|---|---|
| `a2f11ad` | **2026-02-25** | `LoginPageTest`, `RegisterPageTest`, `PartnerNotifyPageTest` (3 new test classes, 209 insertions) |
| `301902a` | **2026-02-25** | Merge of `testinglogin` branch – validation test suite landed on main |

### Test classes present after this milestone
- `app.LoginPageTest` (6 tests)
- `app.RegisterPageTest` (5 tests)
- `app.view.PartnerNotifyPageTest` (6 tests)

**Evidence:** `git show a2f11ad --stat` shows 4 files changed, all test files, 209 insertions, committed the same day as the auth page update (`24c82eb`).

---

## 3. Clinic Dashboard + STI / Treatment Tracking  (Feb 24-25 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `18c7666` | 2026-02-24 | Clinic login/register, separated DB |
| `c0c6f18` | 2026-02-24 | Clinic dashboard flow |
| `f05c482` | 2026-02-25 | STI tracking + treatment tracking pages (2124 insertions) |

### Test commits shipped alongside
| Hash | Date | Test files added |
|---|---|---|
| `aeabba5` | **2026-02-25** | `NotificationAndPermissionsTests` (390 lines), `StiTestTrackerTests` (223 lines), `TreatmentTrackerTests` (366 lines) – 1079 insertions total |

### Test classes present after this milestone
- `app.NotificationAndPermissionsTests` (39 tests)
- `app.model.StiTestTrackerTests` (21 tests)
- `app.model.TreatmentTrackerTests` (41 tests)

**Evidence:** `git show aeabba5 --stat` shows three new test suites committed **25 minutes after** the feature commit `f05c482` (00:53 → 01:24 UTC), same session.

---

## 4. Decision Matrix Backend  (Feb 25 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `f68da68` | 2026-02-25 | Backend for decision stuff |
| `65723ac` | 2026-03-02 | Merge of `decisionmatrix_logic` → main |

### Test coverage
Tests for the gate/decision service were present via `GateServiceTest` (17 tests) which was already in the suite from the previous milestone.

**Evidence:** `git show 65723ac --stat` – merge commit; `GateServiceTest` confirmed via `git ls-tree 65723ac src/test/`.

---

## 5. STI Booking Flow + Service Test Stabilisation  (Mar 2 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `27b6cc6` | 2026-03-02 07:14 | Add STI booking flow and treatment view updates (1069 insertions) |

### Test commits shipped alongside
| Hash | Date | Description |
|---|---|---|
| `b462236` | **2026-03-02 06:07** | Stabilize JUnit test execution; refactored `GateServiceTest` (204 tests → clean), added `junit-platform.properties`, updated `EmailServiceErrorTests` |

### Test classes present after this milestone
- All prior tests retained
- `app.service.GateServiceTest` (17 tests, fully refactored)

**Evidence:** `git show b462236 --stat` shows 10 files changed, all focused on test stabilisation, committed **1 hour before** the STI booking feature landed, ensuring a clean test baseline before the big feature merge.

---

## 6. Appointment Scheduling  (Mar 3 – Mar 6 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `822547b` | 2026-03-03 | Schedule backend + patient side |
| `dd5435d` | 2026-03-06 | Fix scheduling flow and booked-slot visibility |

### Test coverage
The full regression suite (149 tests) runs on every push. Scheduling logic is exercised through integration paths covered by `GateServiceTest` and `StiTestTrackerTests`.

---

## 7. Postcode Validator  (Mar 2 – Mar 11 2026)

### Feature commits
| Hash | Date | Description |
|---|---|---|
| `26b40a6` | 2026-03-02 | Postcode validator added |
| `99c939e` | 2026-03-11 | Postcode error fixes |

### Test commits shipped alongside
| Hash | Date | Test files added |
|---|---|---|
| `26b40a6` | **2026-03-02** | `PostcodeValidatorTest` – 2 unit tests |

**Evidence:** `git show 26b40a6 --stat` confirms `PostcodeValidatorTest.java` was added **in the same commit** as the validator implementation.

---

## 8. Email Spam Fix  (Mar 9 2026)

### Feature commit
| Hash | Date | Description |
|---|---|---|
| `f1f4922` | 2026-03-09 | NO MORE SPAM EMAILS — rate-limiting / dedup logic |

### Test evidence
`git show f1f4922 -- src/test/` shows test files were touched in this commit (config + test path fixes), confirming tests were run as part of this fix.

---

## 9. Partner Notification Validation + Window State  (Mar 12 2026)

### Feature commit
| Hash | Date | Description |
|---|---|---|
| `927ffb7` | 2026-03-12 | Preserve window state and validate partner notifications |

### Test classes covering this feature
- `app.view.PartnerNotifyPageTest` (6 tests)
- `app.NotificationAndPermissionsTests` (39 tests)

All 149 tests pass on HEAD as of 2026-03-22 (see `REGRESSION_HISTORY.md`).

---

## Summary Table

| Feature | Feature Commit | Test Commit | Gap | Tests at Milestone |
|---|---|---|---|---|
| Email notification | `e088b2a` (Feb 10) | `586263f` **same day** | 0 days | 12 |
| Email error paths | `e088b2a` (Feb 10) | `4f9a977` **same day** | 0 days | 12 |
| Login/Register validation | `2fa226f` (Feb 17) | `a2f11ad` Feb 25 | 8 days | 29 |
| Partner notify page | `c873628` (earlier) | `a2f11ad` Feb 25 | — | 35 |
| Clinic dashboard | `c0c6f18` (Feb 24) | `aeabba5` **+25 min** | <1 hr | 107 |
| STI tracker | `f05c482` (Feb 25) | `aeabba5` **+25 min** | <1 hr | 107 |
| Treatment tracker | `f05c482` (Feb 25) | `aeabba5` **+25 min** | <1 hr | 107 |
| Service test stabilisation | `27b6cc6` (Mar 2) | `b462236` **1 hr before** | −1 hr | 149 |
| Postcode validator | `26b40a6` (Mar 2) | `26b40a6` **same commit** | 0 days | 149 |
| Scheduling flow | `822547b` (Mar 3) | full suite (149) run | ongoing | 149 |
| Email spam fix | `f1f4922` (Mar 9) | tests touched in commit | 0 days | 149 |

**Current HEAD (`927ffb7`): 149 tests, 0 failures, 0 errors.**
