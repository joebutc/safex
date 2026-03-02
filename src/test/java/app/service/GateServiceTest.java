package app.service;

import app.model.Action;
import app.model.Context;
import app.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GateServiceTest {

    private static final String PASS_GATE = "G_ALL_PASS";
    private static final String PASS_REASON = "You are eligible for home testing.";
    private static final String G1_REASON = "Under 13 \u2014 safeguarding referral; ages 13 to 15 without Fraser assessment \u2014 clinic triage.";

    private GateService service;

    @BeforeEach
    void setUp() {
        service = new GateService();
    }

    private static Context.Builder eligibleContext() {
        return Context.builder()
                .age(25)
                .fraser(false)
                .symptomatic(false)
                .assault(false)
                .pep(false)
                .preg(false)
                .immuno(false)
                .safeguarding(false)
                .windowElapsed(true)
                .recentKitWeeks(null)
                .understands(true)
                .clinOverride(false);
    }

    static Stream<Arguments> failingGateCases() {
        return Stream.of(
                Arguments.of("G1", G1_REASON, eligibleContext().age(12).build()),
                Arguments.of("G1", G1_REASON, eligibleContext().age(14).fraser(false).build()),
                Arguments.of("G2", "Symptomatic \u2014 refer to sexual health clinic for examination and testing.", eligibleContext().symptomatic(true).build()),
                Arguments.of("G3", "Assault reported \u2014 immediate SARC/A&E referral; forensic care and PEP/EC may be required.", eligibleContext().assault(true).build()),
                Arguments.of("G4", "PEP needed \u2014 urgent sexual health clinic or A&E.", eligibleContext().pep(true).build()),
                Arguments.of("G5", "Pregnant \u2014 route to sexual health clinic and obstetric team.", eligibleContext().preg(true).build()),
                Arguments.of("G6", "Immunocompromised \u2014 route to specialist sexual health/HIV clinic.", eligibleContext().immuno(true).build()),
                Arguments.of("G7", "Safeguarding concern \u2014 clinical triage with safeguarding lead.", eligibleContext().safeguarding(true).build()),
                Arguments.of("G8", "Too early \u2014 advise to wait and reorder; provide interim safer sex advice.", eligibleContext().windowElapsed(false).build()),
                Arguments.of("G9", "Recent kit \u2014 advise contacting clinic; assess for repeat presentations/safeguarding.", eligibleContext().recentKitWeeks(6).build()),
                Arguments.of("G10", "Ensure informed consent: provide clear info and link to local clinic details.", eligibleContext().understands(false).build())
        );
    }

    static Stream<Context> passingCases() {
        return Stream.of(
                eligibleContext().build(),
                eligibleContext().age(14).fraser(true).build(),
                eligibleContext().age(16).build(),
                eligibleContext().recentKitWeeks(12).build(),
                eligibleContext().recentKitWeeks(6).clinOverride(true).build()
        );
    }

    @ParameterizedTest
    @MethodSource("failingGateCases")
    void run_returnsClinicReferralForFailingGate(String expectedGate, String expectedReason, Context ctx) {
        LocalDateTime before = LocalDateTime.now();
        Result result = service.run(ctx);
        LocalDateTime after = LocalDateTime.now();

        assertFailure(result, expectedGate, expectedReason, before, after);
    }

    @ParameterizedTest
    @MethodSource("passingCases")
    void run_returnsHomeTestForEligibleContext(Context ctx) {
        LocalDateTime before = LocalDateTime.now();
        Result result = service.run(ctx);
        LocalDateTime after = LocalDateTime.now();

        assertSuccess(result, before, after);
    }

    @Test
    void run_returnsFirstFailingGateWhenMultipleConditionsApply() {
        Context ctx = eligibleContext()
                .age(12)
                .symptomatic(true)
                .assault(true)
                .windowElapsed(false)
                .understands(false)
                .build();

        LocalDateTime before = LocalDateTime.now();
        Result result = service.run(ctx);
        LocalDateTime after = LocalDateTime.now();

        assertFailure(result, "G1", G1_REASON, before, after);
    }

    private static void assertFailure(
            Result result,
            String expectedGate,
            String expectedReason,
            LocalDateTime before,
            LocalDateTime after) {
        assertFalse(result.isPassed());
        assertEquals(expectedGate, result.getGateName());
        assertEquals(Action.CLINIC_REFER, result.getAction());
        assertEquals(expectedReason, result.getReason());
        assertEvaluatedAt(result, before, after);
    }

    private static void assertSuccess(Result result, LocalDateTime before, LocalDateTime after) {
        assertTrue(result.isPassed());
        assertEquals(PASS_GATE, result.getGateName());
        assertEquals(Action.HOME_TEST, result.getAction());
        assertEquals(PASS_REASON, result.getReason());
        assertEvaluatedAt(result, before, after);
    }

    private static void assertEvaluatedAt(Result result, LocalDateTime before, LocalDateTime after) {
        assertNotNull(result.getEvaluatedAt());
        assertFalse(result.getEvaluatedAt().isBefore(before));
        assertFalse(result.getEvaluatedAt().isAfter(after));
    }
}
