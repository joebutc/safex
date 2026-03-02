package app.service;

import app.model.Action;
import app.model.Context;
import app.model.Result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;



class GateServiceTest {

    private GateService service;

    // All gates pass
    private Context.Builder eligible() {
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

    // Static base builder for use in @MethodSource providers
    private static Context.Builder base() {
        return Context.builder()
                .age(25).fraser(false).symptomatic(false)
                .assault(false).pep(false).preg(false)
                .immuno(false).safeguarding(false)
                .windowElapsed(true).recentKitWeeks(null)
                .understands(true).clinOverride(false);
    }

    // Cases for G2-G8: each flips exactly one flag and expects a specific gate to fire
    static Stream<Arguments> singleFlagCases() {
        return Stream.of(
                Arguments.of(base().symptomatic(true).build(),    "G2"),
                Arguments.of(base().assault(true).build(),         "G3"),
                Arguments.of(base().pep(true).build(),             "G4"),
                Arguments.of(base().preg(true).build(),            "G5"),
                Arguments.of(base().immuno(true).build(),          "G6"),
                Arguments.of(base().safeguarding(true).build(),    "G7"),
                Arguments.of(base().windowElapsed(false).build(),  "G8")
        );
    }

    @BeforeEach
    void setUp() {
        service = new GateService();
    }

    // G1 — Age
    @Test
    void g1_failsUnder13() {
        Context ctx = eligible().age(12).build();
        Result r = service.run(ctx);

        assertFalse(r.isPassed());
        assertEquals("G1", r.getGateName());
        assertEquals(Action.CLINIC_REFER, r.getAction());
    }

    @Test
    void g1_fails13to15NoFraser() {
        Context ctx = eligible().age(14).fraser(false).build();
        Result r = service.run(ctx);

        assertFalse(r.isPassed());
        assertEquals("G1", r.getGateName());
        assertEquals(Action.CLINIC_REFER, r.getAction());
    }

    @Test
    void g1_passes13to15WithFraser() {
        Context ctx = eligible().age(14).fraser(true).build();
        Result r = service.run(ctx);
        // G1 should pass; verify overall pass and action
        assertTrue(r.isPassed());
        assertEquals(Action.HOME_TEST, r.getAction());
        assertEquals("G_ALL_PASS", r.getGateName());
    }

    @Test
    void g1_passes16Plus() {
        Context ctx = eligible().age(16).build();
        Result r = service.run(ctx);
        assertTrue(r.isPassed());
        assertEquals(Action.HOME_TEST, r.getAction());
        assertEquals("G_ALL_PASS", r.getGateName());
    }


    // G2-G8 — Single-flag gates (symptomatic, assault, pep, preg, immuno, safeguarding, window)
    @ParameterizedTest
    @MethodSource("singleFlagCases")
    void singleFlagGate_failsWithClinicRefer(Context ctx, String expectedGate) {
        Result r = service.run(ctx);

        assertFalse(r.isPassed());
        assertEquals(expectedGate, r.getGateName());
        assertEquals(Action.CLINIC_REFER, r.getAction());
    }


    // G9 — Recent kit (< 12 weeks, no clinician override)
    @Test
    void g9_failsRecentKit() {
        Context ctx = eligible().recentKitWeeks(6).clinOverride(false).build();
        Result r = service.run(ctx);

        assertFalse(r.isPassed());
        assertEquals("G9", r.getGateName());
        assertEquals(Action.CLINIC_REFER, r.getAction());
    }

    @Test
    void g9_passesWithClinOverride() {
        Context ctx = eligible().recentKitWeeks(6).clinOverride(true).build();
        Result r = service.run(ctx);

        // G9 should be bypassed — overall pass expected
        assertTrue(r.isPassed());
        assertEquals(Action.HOME_TEST, r.getAction());
    }

    @Test
    void g9_passes12WeeksOrMore() {
        Context ctx = eligible().recentKitWeeks(12).build();
        Result r = service.run(ctx);
        assertTrue(r.isPassed());
        assertEquals(Action.HOME_TEST, r.getAction());
    }


    // G10 — Informed consent / understanding
    @Test
    void g10_failsNoUnderstanding() {
        Context ctx = eligible().understands(false).build();
        Result r = service.run(ctx);

        assertFalse(r.isPassed());
        assertEquals("G10", r.getGateName());
        assertEquals(Action.CLINIC_REFER, r.getAction());
    }


    // All-pass


    @Test
    void allGatesPass() {
        Context ctx = eligible().build();
        Result r = service.run(ctx);

        assertTrue(r.isPassed());
        assertEquals("G_ALL_PASS", r.getGateName());
        assertEquals(Action.HOME_TEST, r.getAction());
        assertNotNull(r.getEvaluatedAt());
    }
}
