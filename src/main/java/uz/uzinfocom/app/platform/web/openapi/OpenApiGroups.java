package uz.uzinfocom.app.platform.web.openapi;

import java.util.Arrays;
import java.util.stream.Stream;

public final class OpenApiGroups {

    private OpenApiGroups() {
    }

    public static final ApiDocumentationGroup REFERENCES = new ApiDocumentationGroup(
            "references",
            "Справочники",
            "Справочники",
            "API для работы со справочниками, классификаторами и нормативно-справочной информацией.",
            new String[]{
                    "/v1/references",
                    "/v1/references/**"
            }
    );

    public static final ApiDocumentationGroup ACCESS_CONTROL = new ApiDocumentationGroup(
            "access-control",
            "Роли и права доступа",
            "Роли и права доступа",
            "API для управления ролями, правами доступа и связями между ролями и правами.",
            new String[]{
                    "/v1/role",
                    "/v1/role/**",
                    "/v1/permission",
                    "/v1/permission/**"
            }
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