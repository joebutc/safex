package app.service;

import app.model.Action;
import app.model.Context;
import app.model.Result;

import java.time.LocalDateTime;

public class GateService {
    public Result run(Context ctx) {

        // G1: Age
        if (!(ctx.getAge() >= 16 || (ctx.getAge() >= 13 && ctx.getAge() <= 15 && ctx.isFraser()))) {
            return fail("G1", "Under 13 - safeguarding referral; 13-15 without Fraser assessment - clinic triage.");
        }

        // G2: Symptoms
        if (ctx.isSymptomatic()) {
            return fail("G2", "Symptomatic - refer to sexual health clinic for examination and testing.");
        }

        // G3: Recent sexual assault
        if (ctx.isAssault()) {
            return fail("G3", "Assault reported - immediate SARC/A&E referral; forensic care and PEP/EC may be required.");
        }

        // G4: Recent HIV exposure / PEP need
        if (ctx.isPep()) {
            return fail("G4", "PEP needed - urgent sexual health clinic / A&E.");
        }

        // G5: Pregnancy
        if (ctx.isPreg()) {
            return fail("G5", "Pregnant - route to sexual health clinic and obstetric team.");
        }

        // G6: Immunocompromised
        if (ctx.isImmuno()) {
            return fail("G6", "Immunocompromised - route to specialist sexual health/HIV clinic.");
        }

        // G7: Safeguarding concerns
        if (ctx.hasSafeguarding()) {
            return fail("G7", "Safeguarding concern - clinical triage with safeguarding lead.");
        }

        // G8: Window period
        if (!ctx.isWindowElapsed()) {
            return fail("G8", "Too early - advise to wait and re-order; provide interim safer-sex advice.");
        }

        // G9: Recent kit within 12 weeks
        if (ctx.recentKitWeeks() != null && ctx.recentKitWeeks() < 12 && !ctx.isClinOverride()) {
            return fail("G9", "Recent kit - advise contacting clinic; assess for repeat presentations/safeguarding.");
        }

        // G10: Understanding and consent
        if (!ctx.understands()) {
            return fail("G10", "Ensure informed consent: provide clear info and link to local clinic details.");
        }

        // All gates passed - home testing appropriate.
        return pass();
    }

    private Result fail(String gateName, String reason) {
        Result result = new Result();
        result.setPassed(false);
        result.setGateName(gateName);
        result.setAction(Action.CLINIC_REFER);
        result.setReason(reason);
        result.setEvaluatedAt(LocalDateTime.now());
        return result;
    }

    private Result pass() {
        Result result = new Result();
        result.setPassed(true);
        result.setGateName("G_ALL_PASS");
        result.setAction(Action.HOME_TEST);
        result.setReason("You are eligible for home testing.");
        result.setEvaluatedAt(LocalDateTime.now());
        return result;
    }
}
