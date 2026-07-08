package uz.uzinfocom.app.platform.reference.application.manualreport.query;

import java.util.Map;

public final class ManualReportSortFields {

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("code", "code"),
            Map.entry("shortName", "shortName"),
            Map.entry("nameUz", "nameUz"),
            Map.entry("nameUzCyril", "nameUzCyril"),
            Map.entry("nameRu", "nameRu"),
            Map.entry("nameKaa", "nameKaa"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private ManualReportSortFields() {
    }
}
