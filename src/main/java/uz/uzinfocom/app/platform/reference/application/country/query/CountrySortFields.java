package uz.uzinfocom.app.platform.reference.application.country.query;

import java.util.Map;

public final class CountrySortFields {

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("code", "code"),
            Map.entry("nameUz", "nameUz"),
            Map.entry("nameUzCyril", "nameUzCyril"),
            Map.entry("nameRu", "nameRu"),
            Map.entry("nameKaa", "nameKaa"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private CountrySortFields() {
    }
}
