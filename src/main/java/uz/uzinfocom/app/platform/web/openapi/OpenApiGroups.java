package uz.uzinfocom.app.platform.web.openapi;

import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.Arrays;
import java.util.stream.Stream;

public final class OpenApiGroups {

    private OpenApiGroups() {
    }

    private static final String[] AUTH_PATHS = {
            ApiPaths.Auth.ROOT,
            ApiPaths.Auth.ROOT + "/**"
    };

    private static final String[] REFERENCES_PATHS = {
            "/v1/references",
            "/v1/references/**",

            ApiPaths.LisReference.ROOT,
            ApiPaths.LisReference.ROOT + "/**"
    };

    private static final String[] ACCESS_CONTROL_PATHS = {
            "/v1/role",
            "/v1/role/**",
            "/v1/roles",
            "/v1/roles/**",

            "/v1/permission",
            "/v1/permission/**",
            "/v1/permissions",
            "/v1/permissions/**",

            "/v1/action",
            "/v1/action/**",
            "/v1/actions",
            "/v1/actions/**"
    };

    /**
     * The admin group is meant to be a one-stop view of everything an
     * {@code isemid_super_admin}/{@code isemid_admin} can do, not just the
     * {@code /v1/admin/**} subtree - directory/reference management
     * ({@link #REFERENCES_PATHS}) and role/permission/action management
     * ({@link #ACCESS_CONTROL_PATHS}) are just as admin-gated
     * ({@code @adminAccessGuard.isAdmin()}), even though they live under
     * their own path roots and keep their own dedicated groups too.
     */
    private static final String[] ADMIN_OWN_PATHS = {
            "/v1/admin",
            "/v1/admin/**"
    };

    private static final String[] ADMIN_PATHS = Stream.of(ADMIN_OWN_PATHS, REFERENCES_PATHS, ACCESS_CONTROL_PATHS)
            .flatMap(Arrays::stream)
            .toArray(String[]::new);

    /**
     * The developer panel itself ({@code /v1/dev/**}) -
     * authenticated via a separate local {@code DevUser} HTTP Basic chain
     * (see {@code DevPanelSecurityConfig}), not the SSO/DHP bearer JWT the
     * rest of the API requires. Kept out of {@link #ADMIN_PATHS} entirely:
     * dev-panel account management ({@code /v1/dev/dev-users/**}) is also
     * reached and authenticated through this same chain now, gated to
     * {@code ROLE_DEV_ROOT} rather than any SSO admin authority - not even
     * {@code isemid_super_admin} can manage these accounts.
     */
    private static final String[] DEV_PANEL_PATHS = {
            ApiPaths.Dev.ROOT,
            ApiPaths.Dev.ROOT + "/**"
    };

    /**
     * Every organization-hierarchy report under {@code modules.report}
     * (Form 1, Form 2, Form 3, ...) — drill-down tables meant to be
     * printed/exported, kept in their own group so they never get lost
     * among the rest of the main business API as more reports are added.
     */
    private static final String[] REPORT_PATHS = {
            ApiPaths.Report.ROOT,
            ApiPaths.Report.ROOT + "/**"
    };

    /**
     * Every endpoint whose data comes from an external system rather than
     * being native ISEMID business data — API2's citizen/legal-entity
     * lookups. Kept in one group so a consumer integrating with an outside
     * system never has to go hunting for these across the main business-API
     * group.
     *
     * <p>The LIS results callback ({@link ApiPaths.Act#LIS_CALLBACK}) is
     * deliberately <em>not</em> here: it's an ordinary {@code /v1/acts/**}
     * endpoint, authenticated the same way as the rest of the Act API, so it
     * belongs in the main group alongside the rest of Act, not a
     * special-cased one.
     */
    private static final String[] INTEGRATION_PATHS = {
            ApiPaths.Citizen.ROOT,
            ApiPaths.Citizen.ROOT + "/**",
            ApiPaths.LegalEntity.ROOT,
            ApiPaths.LegalEntity.ROOT + "/**"
    };

    public static final ApiDocumentationGroup AUTH = new ApiDocumentationGroup(
            "auth",
            "Аутентификация",
            "Аутентификация",
            "Login-proxy: обмен логина/пароля пользователя на токен доступа через внешнего провайдера "
                    + "аутентификации (SSO и т. п.). Не путать с проверкой уже выданных токенов — она "
                    + "происходит на уровне resource-server и не является частью REST API.",
            AUTH_PATHS
    );

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

    public static final ApiDocumentationGroup DEV_PANEL = new ApiDocumentationGroup(
            "dev-panel",
            "Dev Panel",
            "Dev Panel",
            "Панель разработчика: история неудачных запросов, попытки входа, метрики CPU/RAM/диска/HTTP и "
                    + "управление самими учётными записями панели (только для root-аккаунтов). "
                    + "Аутентификация через отдельную локальную учётную запись DevUser (HTTP Basic), а не "
                    + "через SSO/DHP bearer-токен, как весь остальной API.",
            DEV_PANEL_PATHS
    );

    public static final ApiDocumentationGroup REPORT = new ApiDocumentationGroup(
            "report",
            "Отчёты",
            "Отчёты",
            "API отчётов по организационной иерархии (республика→регион→район→организация): Form 1, "
                    + "Form 2, Form 3 и последующие — на основе форм №058 и №058-1.",
            REPORT_PATHS
    );

    public static final ApiDocumentationGroup INTEGRATION = new ApiDocumentationGroup(
            "integration",
            "Внешние интеграции",
            "Внешние интеграции",
            "API, получающие данные из внешних систем: справочные запросы API2 (гражданин, юридическое "
                    + "лицо) и приём результатов от внешних систем.",
            INTEGRATION_PATHS
    );

    /**
     * The inbound-integration surface itself — {@code /integration/v1/**},
     * deliberately outside {@link ApiPaths#API_V1} (see {@link ApiPaths.Integration}),
     * so it needs its own group rather than an entry in {@link #INTEGRATION}
     * (which only covers {@code /v1/**}-rooted paths and would never match
     * this surface anyway).
     */
    private static final String[] INBOUND_INTEGRATION_PATHS = {
            ApiPaths.Integration.ROOT,
            ApiPaths.Integration.ROOT + "/**"
    };

    public static final ApiDocumentationGroup INBOUND_INTEGRATION = new ApiDocumentationGroup(
            "inbound-integration",
            "Входящая интеграция",
            "Входящая интеграция",
            "API для внешних систем, напрямую отправляющих данные в платформу (форма №058, №058-1 и т. д.): "
                    + "получение токена доступа и приём форм от зарегистрированных интеграционных клиентов.",
            INBOUND_INTEGRATION_PATHS
    );

    public static String[] pathsToExcludeFromMain() {
        return Stream.of(
                        AUTH.pathsToMatch(),
                        REFERENCES.pathsToMatch(),
                        ACCESS_CONTROL.pathsToMatch(),
                        ADMIN.pathsToMatch(),
                        REPORT.pathsToMatch(),
                        INTEGRATION.pathsToMatch(),
                        DEV_PANEL.pathsToMatch()
                )
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }
}