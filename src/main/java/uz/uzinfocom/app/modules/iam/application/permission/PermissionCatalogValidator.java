package uz.uzinfocom.app.modules.iam.application.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.repository.PermissionRepository;
import uz.uzinfocom.app.platform.security.auth.Permissions;

import java.util.Set;
import java.util.TreeSet;

/**
 * Warns at startup when an authority the code checks ({@link Permissions#required()}) cannot be
 * granted because its subject or action is missing, inactive or soft-deleted in the database.
 * Such a mismatch otherwise surfaces only as a silent 403 for every user.
 *
 * <p>Warn-only on purpose: the subject/action tables are admin-editable at runtime, so a missing
 * row must not stop the application from booting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCatalogValidator {

    private final PermissionRepository permissionRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void validateOnStartup() {
        try {
            Set<String> missing = findMissing(permissionRepository.findAllGrantableAuthorityNames());
            if (missing.isEmpty()) {
                log.info("Permission catalog OK: all {} authorities used by the code can be granted.",
                        Permissions.required().size());
            } else {
                log.warn("Permission catalog mismatch: {} authority(ies) used by the code cannot be granted "
                                + "(subject/action missing, inactive or deleted in DB): {}",
                        missing.size(), missing);
            }
        } catch (RuntimeException e) {
            log.warn("Permission catalog validation skipped: {}", e.toString());
        }
    }

    /** Authorities required by the code that are absent from {@code grantable}. */
    static Set<String> findMissing(Set<String> grantable) {
        Set<String> missing = new TreeSet<>(Permissions.required());
        missing.removeAll(grantable);
        return missing;
    }
}
