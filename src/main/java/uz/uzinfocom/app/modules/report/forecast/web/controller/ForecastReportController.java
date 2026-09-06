package uz.uzinfocom.app.modules.report.forecast.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.report.forecast.application.query.ForecastReportQueryService;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastBucketUnit;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastDiseaseRiskResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastMethod;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastNodeResponse;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * "Forecast" — «Kasallanish prognozi»: прогноз числа извещений (формы №058 +
 * №058-1, все живые извещения — первичные и подтверждённые, кроме отменённых).
 *
 * <p>Как и остальные отчёты, это geography-first drill-down: {@link #root}
 * возвращает первый уровень иерархии в рамках доступа вызывающего (регионы для
 * республиканского доступа) + строку «Jami» по всей республике, каждая строка —
 * компактная сводка прогноза (модель, тренд, сумма прогноза, число превышений
 * порога). {@link #children} углубляется на уровень (регион → районы, район →
 * организации). Полный ряд history[]/forecast[] и график берутся отдельно —
 * {@link #series} — для одного узла (весь доступ вызывающего либо конкретный
 * {@code regionCode}/{@code districtCode} в его рамках).
 *
 * <p>Фильтры едины: география, код МКБ-10 ({@code diagnosisCode} — первичный или
 * заключительный), окно обучения ({@code from}/{@code to}; при отсутствии {@code
 * from} — look-back по умолчанию: 180 дней / 104 недели / 36 месяцев). Модель
 * (экспоненциальное сглаживание / Holt / Holt-Winters) выбирается автоматически
 * либо задаётся {@code method}; рядом с точечным прогнозом — ~95% интервал и
 * классический эпидемический порог («endemic channel»).
 */
@Tag(
        name = "Report — Forecast",
        description = "API прогноза заболеваемости по формам №058 + №058-1. Geography-first: /root — "
                + "первый уровень иерархии + «Jami», /children — глубже, /series — полный ряд + прогноз + "
                + "эпидемический порог для одного узла. Фильтры: regionCode/districtCode, diagnosisCode "
                + "(МКБ-10), from/to, bucket (DAY/WEEK/MONTH), horizon, method."
)
@Validated
@RestController
@RequestMapping(ApiPaths.ForecastReport.ROOT)
@RequiredArgsConstructor
public class ForecastReportController {

    private final ForecastReportQueryService forecastReportQueryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "География: первый уровень + «Jami»",
            description = "Возвращает первый уровень иерархии в рамках доступа вызывающего (регионы — для "
                    + "республиканского доступа, районы — для областного, организации — для районного), "
                    + "каждый с компактной сводкой прогноза, плюс последней строкой «Jami» — прогноз по всей "
                    + "области доступа (для республиканского вызова — по всей республике)."
    )
    @GetMapping(ApiPaths.ForecastReport.ROOT_NODE)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<ForecastNodeResponse>> root(
            @Parameter(description = "Фильтр по коду МКБ-10 (первичный или заключительный). Необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Интервал агрегации: DAY, WEEK (по умолчанию) или MONTH.")
            @RequestParam(required = false) ForecastBucketUnit bucket,
            @Parameter(description = "Сколько интервалов прогнозировать вперёд. По умолчанию 8; обрезается до 90/52/24.")
            @RequestParam(required = false) @Positive @Max(120) Integer horizon,
            @Parameter(description = "Модель: AUTO (по умолчанию), NAIVE_MEAN, SES, HOLT, HOLT_WINTERS_ADDITIVE.")
            @RequestParam(required = false) ForecastMethod method,
            @Parameter(description = "Начало окна обучения (включительно). По умолчанию — look-back по умолчанию.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец окна обучения (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                forecastReportQueryService.getRoot(diagnosisCode, bucket, horizon, method, from, to)
        );
    }

    @Operation(
            summary = "География: следующий уровень",
            description = "Возвращает следующий уровень иерархии. Без regionCode/districtCode — уровень, "
                    + "соответствующий области доступа вызывающего. С regionCode — районы региона; с "
                    + "districtCode — организации района. Запрос за пределами области доступа отклоняется (403)."
    )
    @GetMapping(ApiPaths.ForecastReport.CHILDREN)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<ForecastNodeResponse>> children(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Фильтр по коду МКБ-10 (первичный или заключительный). Необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Интервал агрегации: DAY, WEEK (по умолчанию) или MONTH.")
            @RequestParam(required = false) ForecastBucketUnit bucket,
            @Parameter(description = "Сколько интервалов прогнозировать вперёд. По умолчанию 8; обрезается до 90/52/24.")
            @RequestParam(required = false) @Positive @Max(120) Integer horizon,
            @Parameter(description = "Модель: AUTO (по умолчанию), NAIVE_MEAN, SES, HOLT, HOLT_WINTERS_ADDITIVE.")
            @RequestParam(required = false) ForecastMethod method,
            @Parameter(description = "Начало окна обучения (включительно). По умолчанию — look-back по умолчанию.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец окна обучения (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                forecastReportQueryService.getChildren(regionCode, districtCode, diagnosisCode, bucket, horizon, method, from, to)
        );
    }

    @Operation(
            summary = "Полный ряд + прогноз для одного узла (график)",
            description = "Возвращает обучающий ряд (history), прогноз на horizon интервалов вперёд с "
                    + "интервалами прогноза и эпидемическим порогом (forecast) и сводку (summary). Без "
                    + "regionCode/districtCode — весь доступ вызывающего (для республиканского — вся "
                    + "республика). Запрос за пределами доступа → 403."
    )
    @GetMapping(ApiPaths.ForecastReport.SERIES)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<ForecastResponse> series(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Фильтр по коду МКБ-10 (первичный или заключительный). Необязательный.")
            @RequestParam(required = false) String diagnosisCode,
            @Parameter(description = "Интервал агрегации: DAY, WEEK (по умолчанию) или MONTH.")
            @RequestParam(required = false) ForecastBucketUnit bucket,
            @Parameter(description = "Сколько интервалов прогнозировать вперёд. По умолчанию 8; обрезается до 90/52/24.")
            @RequestParam(required = false) @Positive @Max(120) Integer horizon,
            @Parameter(description = "Модель: AUTO (по умолчанию), NAIVE_MEAN, SES, HOLT, HOLT_WINTERS_ADDITIVE.")
            @RequestParam(required = false) ForecastMethod method,
            @Parameter(description = "Начало окна обучения (включительно). По умолчанию — look-back по умолчанию.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец окна обучения (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                forecastReportQueryService.getSeries(regionCode, districtCode, diagnosisCode, bucket, horizon, method, from, to)
        );
    }

    @Operation(
            summary = "Рейтинг: какие болезни могут вырасти",
            description = "Для одного узла (весь доступ вызывающего либо конкретный regionCode/districtCode в его "
                    + "рамках) прогнозирует каждый встречавшийся в обучающем окне код МКБ-10 отдельно и возвращает "
                    + "топ по уровню риска: HIGH — есть будущие интервалы выше эпидемического порога, MEDIUM — "
                    + "порога нет, но тренд растёт, LOW — остальные. Коды с числом извещений в обучающем окне "
                    + "меньше minCases не учитываются (слишком разреженный ряд)."
    )
    @GetMapping(ApiPaths.ForecastReport.TOP_DISEASES)
    @PreAuthorize("isAuthenticated() and hasAuthority('PERMISSION_REPORTS_READ')")
    public ApiResponse<List<ForecastDiseaseRiskResponse>> topDiseases(
            @Parameter(description = "Код региона (необязательный).")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (необязательный).")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Интервал агрегации: DAY, WEEK (по умолчанию) или MONTH.")
            @RequestParam(required = false) ForecastBucketUnit bucket,
            @Parameter(description = "Сколько интервалов прогнозировать вперёд. По умолчанию 8; обрезается до 90/52/24.")
            @RequestParam(required = false) @Positive @Max(120) Integer horizon,
            @Parameter(description = "Модель: AUTO (по умолчанию), NAIVE_MEAN, SES, HOLT, HOLT_WINTERS_ADDITIVE.")
            @RequestParam(required = false) ForecastMethod method,
            @Parameter(description = "Начало окна обучения (включительно). По умолчанию — look-back по умолчанию.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец окна обучения (включительно). По умолчанию — сегодня.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Сколько болезней вернуть. По умолчанию 10, максимум 50.")
            @RequestParam(required = false) @Positive @Max(50) Integer limit,
            @Parameter(description = "Минимальное число извещений по коду в обучающем окне, чтобы попасть в рейтинг. По умолчанию 3.")
            @RequestParam(required = false) @PositiveOrZero Long minCases
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                forecastReportQueryService.getTopDiseases(
                        regionCode, districtCode, bucket, horizon, method, from, to, limit, minCases
                )
        );
    }
}
