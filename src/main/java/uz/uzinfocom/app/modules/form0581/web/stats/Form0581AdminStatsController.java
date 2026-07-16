package uz.uzinfocom.app.modules.form0581.web.stats;

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
import uz.uzinfocom.app.modules.form0581.application.stats.query.Form0581StatsQueryService;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581Mkb10CountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581OrganizationCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581StatusCountResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * The admin dashboard — every endpoint here is unscoped (all organizations
 * at once) and admin-gated. The org-scoped "everyone" dashboard lives
 * separately at {@link Form0581StatsController} under {@code /v1/form-058-1/stats}.
 */
@Tag(
        name = "Admin - Form 058-1 Statistics",
        description = "API административного статистического дашборда по форме №058-1: агрегированные данные "
                + "по всем организациям сразу — по статусу, организациям, датам и диагнозам."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Form0581AdminStats.ROOT)
@RequiredArgsConstructor
public class Form0581AdminStatsController {

    private final Form0581StatsQueryService form0581StatsQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Статистика форм №058-1 по статусу (все организации)",
            description = "Возвращает количество форм, сгруппированное по статусу, по всем организациям сразу."
    )
    @GetMapping(ApiPaths.Form0581AdminStats.BY_STATUS)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<List<Form0581StatusCountResponse>> byStatus() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581StatsQueryService.adminCountByStatus()
        );
    }

    @Operation(
            summary = "Статистика форм №058-1 по дате создания (все организации)",
            description = "Возвращает количество форм, сгруппированное по дню создания, по всем организациям "
                    + "сразу. Параметры from/to необязательны."
    )
    @GetMapping(ApiPaths.Form0581AdminStats.BY_DATE)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<List<Form0581DailyCountResponse>> byDate(
            @Parameter(description = "Дата начала периода (включительно).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Дата окончания периода (включительно).")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581StatsQueryService.adminCountByDay(from, to)
        );
    }

    @Operation(
            summary = "Наиболее частые коды МКБ-10 среди форм №058-1 (все организации)",
            description = "Возвращает наиболее часто встречающиеся коды диагноза по МКБ-10 по всем "
                    + "организациям сразу."
    )
    @GetMapping(ApiPaths.Form0581AdminStats.TOP_MKB10)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<List<Form0581Mkb10CountResponse>> topMkb10(
            @Parameter(description = "Максимальное количество кодов в ответе.")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581StatsQueryService.adminTopMkb10(limit)
        );
    }

    @Operation(
            summary = "Статистика форм №058-1 по организации-отправителю",
            description = "Возвращает количество форм, сгруппированное по организации-отправителю, "
                    + "по всем организациям сразу."
    )
    @GetMapping(ApiPaths.Form0581AdminStats.BY_SENDER_ORGANIZATION)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<List<Form0581OrganizationCountResponse>> bySenderOrganization() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581StatsQueryService.countBySenderOrganization()
        );
    }

    @Operation(
            summary = "Статистика форм №058-1 по организации-получателю",
            description = "Возвращает количество форм, сгруппированное по организации-получателю, "
                    + "по всем организациям сразу."
    )
    @GetMapping(ApiPaths.Form0581AdminStats.BY_RECEIVER_ORGANIZATION)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<List<Form0581OrganizationCountResponse>> byReceiverOrganization() {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581StatsQueryService.countByReceiverOrganization()
        );
    }
}
