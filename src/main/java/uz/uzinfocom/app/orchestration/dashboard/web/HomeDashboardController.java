package uz.uzinfocom.app.orchestration.dashboard.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.orchestration.dashboard.application.query.Form0581DashboardQueryService;
import uz.uzinfocom.app.orchestration.dashboard.application.query.Form058DashboardQueryService;
import uz.uzinfocom.app.orchestration.dashboard.application.query.HomeDashboardQueryService;
import uz.uzinfocom.app.orchestration.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.exception.NotFoundException;

@Tag(
        name = "Dashboard",
        description = "API дашборда. /home — общий обзор (медицинские учреждения, пользователи по ролям) в "
                + "рамках доступа текущей организации. /home/{module} — статистика конкретного модуля "
                + "(форма №058, форма №058-1): сводка, динамика по месяцам, карты/акты по статусу и, где "
                + "применимо, ТОП диагнозов/источники/география. card и act — не отдельные значения module; "
                + "их статистика уже встроена в form058/form058-1 (см. cardsByStatus/actsByStatus)."
)
@RestController
@RequestMapping(ApiPaths.Dashboard.ROOT)
@RequiredArgsConstructor
public class HomeDashboardController {

    private static final String MODULE_FORM058 = "form058";
    private static final String MODULE_FORM0581 = "form058-1";

    private final HomeDashboardQueryService homeDashboardQueryService;
    private final Form058DashboardQueryService form058DashboardQueryService;
    private final Form0581DashboardQueryService form0581DashboardQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Общий обзор дашборда",
            description = "Возвращает общую сводку в рамках доступа текущей организации: медицинские "
                    + "учреждения (по медицинскому типу и уровню организации) и пользователи (по ролям). "
                    + "Не содержит статистики по конкретным модулям — см. /home/{module}."
    )
    @GetMapping(ApiPaths.Dashboard.HOME)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<HomeDashboardResponse> home() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                homeDashboardQueryService.getHome()
        );
    }

    @Operation(
            summary = "Дашборд конкретного модуля",
            description = "Возвращает полную статистику одного модуля (сводка, динамика по месяцам, карты/акты "
                    + "по статусу и, где применимо, ТОП диагнозов/источники/география), в рамках доступа "
                    + "текущей организации. Допустимые значения module: form058, form058-1."
    )
    @GetMapping(ApiPaths.Dashboard.HOME_MODULE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Object> homeModule(@Parameter(description = "form058 или form058-1") @PathVariable String module) {
        Object data = switch (module) {
            case MODULE_FORM058 -> form058DashboardQueryService.getDashboard();
            case MODULE_FORM0581 -> form0581DashboardQueryService.getDashboard();
            default -> throw new NotFoundException("dashboard.module.not_found", module);
        };

        return ApiResponse.success(messageResolver.resolve("common.success"), data);
    }
}
