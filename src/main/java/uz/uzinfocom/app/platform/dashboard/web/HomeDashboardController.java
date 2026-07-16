package uz.uzinfocom.app.platform.dashboard.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.dashboard.application.query.HomeDashboardQueryService;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "Dashboard",
        description = "API главного дашборда: сводка по случаям, динамика, ТОП диагнозов, географический "
                + "разрез (по районам своей области либо по регионам всей республики), медицинские учреждения, "
                + "карты расследования и акты — всё в рамках доступа текущей организации."
)
@RestController
@RequestMapping(ApiPaths.Dashboard.ROOT)
@RequiredArgsConstructor
public class HomeDashboardController {

    private final HomeDashboardQueryService homeDashboardQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Главный дашборд",
            description = "Возвращает сводные показатели в рамках доступа текущей организации. Если область "
                    + "видимости региональная — географический разрез строится по районам своей области; если "
                    + "республиканская — по регионам всей страны; если ограничена одним районом или организацией "
                    + "— географический разрез не строится, возвращаются только собственные показатели."
    )
    @GetMapping(ApiPaths.Dashboard.HOME)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<HomeDashboardResponse> home() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                homeDashboardQueryService.getHome()
        );
    }
}
