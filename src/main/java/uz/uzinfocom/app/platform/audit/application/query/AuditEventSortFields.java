package uz.uzinfocom.app.platform.audit.application.query;

import java.util.Map;

public final class AuditEventSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "occurredAt", "occurredAt"
    );

    private AuditEventSortFields() {
    }
}
