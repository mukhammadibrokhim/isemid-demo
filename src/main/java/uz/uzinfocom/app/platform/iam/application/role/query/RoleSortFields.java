package uz.uzinfocom.app.platform.iam.application.role.query;

import java.util.Map;

public final class RoleSortFields {

    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "name", "name",
            "descriptionUz", "descriptionUz",
            "descriptionUzCyril", "descriptionUzCyril",
            "descriptionRu", "descriptionRu",
            "descriptionKaa", "descriptionKaa"
    );

    private RoleSortFields() {
    }
}