package uz.uzinfocom.app.modules.form129.application.command;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.util.UUID;

/**
 * Shared result shape for accept/reject — both simply move {@code status}
 * on, so there is no separate per-action result record the way Form0581
 * needs one per lifecycle action (its richer state carries more fields).
 */
public record Form129StatusResult(
        Long id,
        UUID uuid,
        Form129Status status
) {
}
