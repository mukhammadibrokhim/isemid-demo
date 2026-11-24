package uz.uzinfocom.app.platform.security.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Permission checks shared by the three notification forms (058, 058-1, 129), which all split
 * a subject into a sender side ({@code CREATE}, {@code UPDATE}, {@code DELETE}, {@code CANCEL},
 * {@code APPROVE}, {@code READ_SENT}) and a receiver side ({@code ACCEPT}, {@code REJECT},
 * {@code READ_RECEIVED}); see docs/permission-naming-mapping.md.
 *
 * <p>Used from {@code @PreAuthorize} (bean name {@code formPermissionGuard}) where the needed
 * permission depends on a request parameter (list direction) or on either of two actions, and
 * from the cancel validators, where the side is only known once the form is loaded.
 * The permission answers "may this user act in that role at all"; which side the user's
 * organization really is stays an instance rule in the validators.
 */
@Component("formPermissionGuard")
public class FormPermissionGuard {

    private static final String PREFIX = "PERMISSION_";

    /** List/export scope: {@code OUTGOING} needs READ_SENT, {@code INCOMING} READ_RECEIVED, {@code ALL} both. */
    public boolean canList(String subject, String direction) {
        if (direction == null) {
            return false;
        }
        return switch (direction.toUpperCase()) {
            case "OUTGOING" -> has(subject, "READ_SENT");
            case "INCOMING" -> has(subject, "READ_RECEIVED");
            case "ALL" -> has(subject, "READ_SENT") && has(subject, "READ_RECEIVED");
            default -> false;
        };
    }

    /** Single-form reads and stats: either side's read permission is enough (scope is checked in code). */
    public boolean canReadAny(String subject) {
        return has(subject, "READ_SENT") || has(subject, "READ_RECEIVED");
    }

    /** Cancel is one endpoint for both sides: CANCEL (sender withdraws) or REJECT (receiver declines). */
    public boolean canCancelOrReject(String subject) {
        return has(subject, "CANCEL") || has(subject, "REJECT");
    }

    /**
     * Side-aware check inside the cancel validators: a sender needs CANCEL, a receiver REJECT
     * (an organization that is both may use either).
     */
    public void requireCancelOrReject(String subject, boolean isSender, boolean isReceiver) {
        boolean allowed = (isSender && has(subject, "CANCEL")) || (isReceiver && has(subject, "REJECT"));
        if (!allowed) {
            throw new AccessDeniedException("Missing permission to cancel/reject " + subject);
        }
    }

    public boolean has(String subject, String action) {
        return authorities().contains(PREFIX + subject.toUpperCase() + "_" + action.toUpperCase());
    }

    private static Set<String> authorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
