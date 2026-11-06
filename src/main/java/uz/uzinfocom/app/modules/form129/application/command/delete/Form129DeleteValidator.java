package uz.uzinfocom.app.modules.form129.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;

/**
 * Form129 has no per-organization delete: the document is a pure registry, so
 * the only actor allowed to remove one is an {@code isemid_admin} or
 * {@code isemid_super_admin} doing a cleanup. Every status is deletable for
 * them — there is no approved-record protection here the way {@code Form058}
 * has, because Form129 never carries a final medical decision.
 */
@Component
@RequiredArgsConstructor
public class Form129DeleteValidator {

    private final AdminAccessGuard adminAccessGuard;

    public void validate() {
        adminAccessGuard.requireAdmin();
    }
}
