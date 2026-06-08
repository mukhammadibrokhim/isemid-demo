package uz.uzinfocom.app.platform.reference.application.catalog.query;

import java.util.Map;

public final class CatalogSortFields {

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "type", "type",
            "code", "code",
            "parentCode", "parentCode",
            "nameUz", "nameUz",
            "nameRu", "nameRu"
    );

    private CatalogSortFields() {
    }
}
