package uz.uzinfocom.app.platform.settings.application.query;

import java.util.Map;

public final class SystemSettingSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "settingKey", "settingKey",
            "valueType", "valueType",
            "active", "active"
    );

    private SystemSettingSortFields() {
    }
}
