package uz.uzinfocom.app.platform.devpanel.application.query;

import java.util.Map;

public final class DevPositionSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "name", "name",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private DevPositionSortFields() {
    }
}
