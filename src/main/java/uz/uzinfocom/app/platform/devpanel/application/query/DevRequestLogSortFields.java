package uz.uzinfocom.app.platform.devpanel.application.query;

import java.util.Map;

public final class DevRequestLogSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "occurredAt", "occurredAt",
            "httpStatus", "httpStatus",
            "durationMs", "durationMs",
            "principal", "principal"
    );

    private DevRequestLogSortFields() {
    }
}
