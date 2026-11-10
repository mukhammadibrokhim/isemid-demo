package uz.uzinfocom.app.modules.form0581.application.command.reopen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.domain.exception.InvalidForm0581StateException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;

/**
 * Reopening a {@code CANCELED} form is a super-admin-only escape hatch —
 * neither the sender nor the receiver organization can do this, regardless
 * of whether the form got there via the sender's {@code cancel()} or the
 * receiver's rejection (there is no separate "rejected" status — both close
 * the form the same way). See {@code Form0581.ensureEditable()} for why
 * {@code CANCELED} is otherwise a dead end for everyone else.
 */
@Component
@RequiredArgsConstructor
public class Form0581ReopenValidator {

    private final AdminAccessGuard form0581AccessGuard;

    public void validate(Form0581 form0581) {
        form0581AccessGuard.requireSuperAdmin();

        if (!form0581.getStatus().isReopenable()) {
            throw new InvalidForm0581StateException("error.form0581.reopen-not-allowed", form0581.getStatus());
        }
    }
}
