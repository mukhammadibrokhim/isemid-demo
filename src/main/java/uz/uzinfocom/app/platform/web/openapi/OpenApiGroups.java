package uz.uzinfocom.app.platform.web.openapi;

import java.util.Arrays;
import java.util.stream.Stream;

public final class OpenApiGroups {

    private OpenApiGroups() {
    }

    private static final String[] REFERENCES_PATHS = {
            "/v1/references",
            "/v1/references/**"
    };

    private static final String[] ACCESS_CONTROL_PATHS = {
            "/v1/role",
            "/v1/role/**",
            "/v1/roles",
            "/v1/roles/**",

            "/v1/permission",
            "/v1/permission/**",
            "/v1/permissions",
            "/v1/permissions/**"
    };

    public static final ApiDocumentationGroup REFERENCES = new ApiDocumentationGroup(
            "references",
            "Справочники",
            "Справочники",
            "API для работы со справочниками, классификаторами и нормативно-справочной информацией.",
            REFERENCES_PATHS
    );

    public static final ApiDocumentationGroup ACCESS_CONTROL = new ApiDocumentationGroup(
            "access-control",
            "Роли и права доступа",
            "Роли и права доступа",
            "API для управления ролями, правами доступа и связями между ролями и правами.",
            ACCESS_CONTROL_PATHS
    );

    public static String[] pathsToExcludeFromMain() {
        return Stream.of(
                        REFERENCES.pathsToMatch(),
                        ACCESS_CONTROL.pathsToMatch()
                )
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }
}