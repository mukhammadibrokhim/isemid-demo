package uz.uzinfocom.app.platform.persistence.audit;

import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

/**
 * Port for resolving an {@link AuditableEntity}'s created/updated stamps
 * into a display-ready {@link AuditResponse}. {@code modules.iam} provides
 * the implementation (it owns the {@code User} lookup needed to resolve
 * creator/updater names) — this interface is what lets {@code platform}
 * packages (settings, integrationclient, ...) consume that without
 * depending on {@code modules.iam} directly.
 */
public interface AuditResolver {

    AuditResponse resolve(AuditableEntity entity);
}
