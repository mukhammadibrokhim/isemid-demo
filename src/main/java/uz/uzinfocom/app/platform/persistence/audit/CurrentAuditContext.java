package uz.uzinfocom.app.platform.persistence.audit;

import lombok.Setter;
import uz.uzinfocom.app.platform.iam.domain.Organization;

public final class CurrentAuditContext {

    @Setter
    private static CurrentAuditProvider provider;

    private CurrentAuditContext() {
    }

    public static Long currentUserId() {
        if (provider == null) {
            return null;
        }

        return provider.currentUserId();
    }

    public static Organization currentOrganization() {
        if (provider == null) {
            return null;
        }

        return provider.currentOrganization();
    }
}