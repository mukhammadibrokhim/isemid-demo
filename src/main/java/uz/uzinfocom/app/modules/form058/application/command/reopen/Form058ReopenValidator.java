package uz.uzinfocom.app.modules.form058.application.command.reopen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.domain.exception.InvalidForm058StateException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;

/**
 * Reopening a {@code CANCELED} form is a super-admin-only escape hatch —
 * neither the sender nor the receiver organization can do this, regardless
 * of whether the form got there via the sender's {@code cancel()} or the
 * receiver's {@code reject()} (there is no separate "rejected" status
 * anymore — both close the form the same way). See
 * {@code Form058.ensureEditable()} for why {@code CANCELED} is otherwise a
 * dead end for everyone else.
 */
@Component
@RequiredArgsConstructor
public class Form058ReopenValidator {

    private final AdminAccessGuard form058AccessGuard;

    public void validate(Form058 form058) {
        form058AccessGuard.requireSuperAdmin();

        if (!form058.getStatus().isReopenable()) {
            throw new InvalidForm058StateException("error.form058.reopen-not-allowed", form058.getStatus());
        }
    }
}
