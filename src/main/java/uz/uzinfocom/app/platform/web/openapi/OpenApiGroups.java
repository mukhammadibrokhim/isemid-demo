package uz.uzinfocom.app.platform.web.openapi;

import uz.uzinfocom.app.shared.constants.api.ApiPaths;

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

    private static final String[] ADMIN_PATHS = {
            "/v1/admin",
            "/v1/admin/**"
    };

    /**
     * Every endpoint whose data comes from (or is pushed by) an external
     * system rather than being native ISEMID business data — API2's
     * citizen/legal-entity lookups today, plus the LIS results callback
     * path reserved in {@link ApiPaths.ExternalApi} for when that
     * integration is wired up. Kept in one group so a consumer integrating
     * with an outside system never has to go hunting for these across the
     * main business-API group.
     */
    private static final String[] INTEGRATION_PATHS = {
            "/v1/citizen",
            "/v1/citizen/**",
            "/v1/legal-entity",
            "/v1/legal-entity/**",
            ApiPaths.ExternalApi.ISEMID_CALLBACK,
            ApiPaths.ExternalApi.ISEMID_CALLBACK + "**"
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

    public static final ApiDocumentationGroup ADMIN = new ApiDocumentationGroup(
            "admin",
            "Admin",
            "Admin",
            "Административный API: настройки системы, управление локальным административным доступом "
                    + "и статистика по всем организациям.",
            ADMIN_PATHS
    );

    public static final ApiDocumentationGroup INTEGRATION = new ApiDocumentationGroup(
            "integration",
            "Внешние интеграции",
            "Внешние интеграции",
            "API, получающие данные из внешних систем: справочные запросы API2 (гражданин, юридическое "
                    + "лицо) и приём результатов от внешних систем.",
            INTEGRATION_PATHS
    );

    public static String[] pathsToExcludeFromMain() {
        return Stream.of(
                        REFERENCES.pathsToMatch(),
                        ACCESS_CONTROL.pathsToMatch(),
                        ADMIN.pathsToMatch(),
                        INTEGRATION.pathsToMatch()
                )
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }
}