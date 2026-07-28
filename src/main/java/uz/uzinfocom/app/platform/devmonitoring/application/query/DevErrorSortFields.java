package uz.uzinfocom.app.platform.devmonitoring.application.query;

import java.util.Map;

public final class DevErrorSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "occurredAt", "occurredAt",
            "httpStatus", "httpStatus",
            "status", "status"
    );

    private DevErrorSortFields() {
    }
}
