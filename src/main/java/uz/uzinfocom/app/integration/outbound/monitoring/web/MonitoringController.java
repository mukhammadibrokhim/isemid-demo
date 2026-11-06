package uz.uzinfocom.app.integration.outbound.monitoring.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.common.web.InboundCallerContext;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringQueryService;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringDynamicsResponse;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringResponse;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.time.LocalDate;

/**
 * Outbound monitoring surface, same caller model as {@code PatientCaseController}:
 * a registered integration client pulls data out, gated by its own scope
 * (granted per client through the integration-client admin) and by the
 * {@code {source}} key matching the client's registered one.
 * <p>
 * Request parameters and response JSON are the legacy monitoring contract
 * (dates as {@code dd.MM.yyyy}, {@code mkb10Code}, ...) and must not change;
 * only the URLs are new.
 */
@Tag(
        name = "Integration - Monitoring",
        description = "Мониторинг заболеваемости для зарегистрированных интеграционных клиентов: "
                + "сводные данные за период и динамика."
)
@RestController
@RequiredArgsConstructor
public class MonitoringController {

    private static final String DATE_PATTERN = "dd.MM.yyyy";

    private final MonitoringQueryService monitoringQueryService;

    @Operation(
            summary = "Получить мониторинг",
            description = "Сводные данные мониторинга по форме №058 за период (не более 366 дней): без regionCode "
                    + "— по областям, с regionCode — по районам этой области. В каждой локации — число случаев, "
                    + "статусы, пол, возрастные группы и разбивка по блокам МКБ-10 итогового диагноза. "
                    + "mkb10Code сопоставляется только с итоговым диагнозом. Требуется scope monitoring:read."
    )
    @GetMapping(ApiPaths.Integration.MONITORING)
    @PreAuthorize("isAuthenticated()")
    public MonitoringResponse getMonitoring(
            @Parameter(description = "Название системы-получателя (см. InboundCallerContext).", required = true)
            @PathVariable String source,
            @Parameter(description = "Начало периода (dd.MM.yyyy).", required = true)
            @RequestParam @DateTimeFormat(pattern = DATE_PATTERN) LocalDate fromDate,
            @Parameter(description = "Конец периода (dd.MM.yyyy).", required = true)
            @RequestParam @DateTimeFormat(pattern = DATE_PATTERN) LocalDate toDate,
            @Parameter(description = "Код региона (ref_region.code).", example = "UZ-AN")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код диагноза по МКБ-10.", example = "A00-A09")
            @RequestParam(required = false) String mkb10Code
    ) {
        InboundCallerContext.requireScope(IntegrationScope.MONITORING_READ);
        InboundCallerContext.requireMatchingSourceKey(source);

        return monitoringQueryService.getMonitoring(fromDate, toDate, regionCode, mkb10Code);
    }

    @Operation(
            summary = "Получить динамику мониторинга",
            description = "Динамика по форме №058 за период (не более 366 дней) с необязательной фильтрацией по "
                    + "региону, району и диагнозу (МКБ-10). Шаг ряда выбирается по длине периода: до 1 дня — часы, "
                    + "до 30 — дни, до 180 — недели, дольше — месяцы; периоды без случаев присутствуют с нулями. "
                    + "Конец периода ограничен текущим моментом. Требуется scope monitoring-dynamics:read."
    )
    @GetMapping(ApiPaths.Integration.MONITORING_DYNAMICS)
    @PreAuthorize("isAuthenticated()")
    public MonitoringDynamicsResponse getDynamics(
            @Parameter(description = "Название системы-получателя (см. InboundCallerContext).", required = true)
            @PathVariable String source,
            @Parameter(description = "Начало периода (dd.MM.yyyy).", required = true)
            @RequestParam @DateTimeFormat(pattern = DATE_PATTERN) LocalDate fromDate,
            @Parameter(description = "Конец периода (dd.MM.yyyy).", required = true)
            @RequestParam @DateTimeFormat(pattern = DATE_PATTERN) LocalDate toDate,
            @Parameter(description = "Код региона (ref_region.code).", example = "UZ-AN")
            @RequestParam(required = false) String regionCode,
            @Parameter(description = "Код района (ref_district.code).", example = "AN-202")
            @RequestParam(required = false) String districtCode,
            @Parameter(description = "Код диагноза по МКБ-10.", example = "A00-A09")
            @RequestParam(required = false) String mkb10Code
    ) {
        InboundCallerContext.requireScope(IntegrationScope.MONITORING_DYNAMICS_READ);
        InboundCallerContext.requireMatchingSourceKey(source);

        return monitoringQueryService.getDynamics(fromDate, toDate, regionCode, districtCode, mkb10Code);
    }
}
