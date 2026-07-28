package uz.uzinfocom.app.platform.devmonitoring.application.query;

import java.util.Map;

public final class DevLoginHistorySortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "occurredAt", "occurredAt",
            "username", "username",
            "provider", "provider"
    );

    private DevLoginHistorySortFields() {
    }
}
