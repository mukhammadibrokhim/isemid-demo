package uz.uzinfocom.app.modules.act.application.query;

import java.util.Map;

public final class ActSortFields {

    public static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("actType", "actType"),
            Map.entry("status", "actStatus"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private ActSortFields() {
    }
}
