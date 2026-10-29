package uz.uzinfocom.app.orchestration.webhook.application.query;

import java.util.Map;

public class OutboundWebhookDispatchSortFields {
    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "status", "status",
            "nextAttemptAt", "nextAttemptAt",
            "lastAttemptedAt", "lastAttemptedAt",
            "createdAt", "createdAt"
    );
}
