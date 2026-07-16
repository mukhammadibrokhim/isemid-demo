package uz.uzinfocom.app.modules.form058.web.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.form058.application.stats.query.Form058StatsQueryService;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058DailyCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058StatusCountResponse;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * The "everyone" dashboard — every endpoint here is scoped to the caller's
 * own organization (via {@link Form058Direction}). The unscoped,
 * cross-organization admin dashboard lives separately at
 * {@code Form058AdminStatsController} under {@code /v1/admin/form-058/stats}.
 */
@Tag(
        name = "Form 058 Statistics",
        description = "API статистического дашборда по форме №058 в рамках доступа текущей организации: "
                + "агрегированные данные по статусу, датам и диагнозам."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form058Stats.ROOT)
@RequiredArgsConstructor
public class Form058StatsController {

    private final Form058StatsQueryService form058StatsQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Статистика форм №058 по статусу",
            description = "Возвращает количество форм, сгруппированное по статусу, в рамках доступа "
                    + "текущей организации. Направление ALL доступно только для isemid_super_admin."
    )
    @GetMapping(ApiPaths.Form058Stats.BY_STATUS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form058StatusCountResponse>> byStatus(
            @Parameter(description = "Направление: исходящие, входящие или все (только для super-admin).", required = true)
            @RequestParam Form058Direction direction
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058StatsQueryService.countByStatus(direction)
        );
    }

    @Operation(
            summary = "Статистика форм №058 по дате создания",
            description = "Возвращает количество форм, сгруппированное по дню создания, в рамках доступа "
                    + "текущей организации. Параметры from/to необязательны."
    )
    @GetMapping(ApiPaths.Form058Stats.BY_DATE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form058DailyCountResponse>> byDate(
            @Parameter(description = "Направление: исходящие, входящие или все (только для super-admin).", required = true)
            @RequestParam Form058Direction direction,
            @Parameter(description = "Дата начала периода (включительно).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Дата окончания периода (включительно).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058StatsQueryService.countByDay(direction, from, to)
        );
    }

    @Operation(
            summary = "Наиболее частые коды МКБ-10 среди форм №058",
            description = "Возвращает наиболее часто встречающиеся коды диагноза по МКБ-10 в рамках доступа "
                    + "текущей организации."
    )
    @GetMapping(ApiPaths.Form058Stats.TOP_MKB10)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Form058Mkb10CountResponse>> topMkb10(
            @Parameter(description = "Направление: исходящие, входящие или все (только для super-admin).", required = true)
            @RequestParam Form058Direction direction,
            @Parameter(description = "Максимальное количество кодов в ответе.")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058StatsQueryService.topMkb10(direction, limit)
        );
    }
}
