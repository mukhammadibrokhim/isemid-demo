package uz.uzinfocom.app.platform.settings.application.query;

import java.util.Map;

public final class RouteAccessPolicySortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "pattern", "pattern",
            "displayOrder", "displayOrder"
    );

    private RouteAccessPolicySortFields() {
    }
}
