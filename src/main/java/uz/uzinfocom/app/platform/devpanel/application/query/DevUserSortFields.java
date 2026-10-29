package uz.uzinfocom.app.platform.devpanel.application.query;

import java.util.Map;

public final class DevUserSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "username", "username",
            "createdAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private DevUserSortFields() {
    }
}
