package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devmonitoring.application.query.DevMetricsQueryService;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.HttpEndpointMetricsResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.SystemMetricsResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;

@Tag(
        name = "Dev Monitoring - Metrics",
        description = "CPU/RAM/disk and per-endpoint HTTP metrics, read directly from the app's existing "
                + "MeterRegistry (the same data already exposed at /v1/actuator/prometheus), reshaped for "
                + "the internal developer monitoring panel."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevMetricsController {

    private final DevMetricsQueryService devMetricsQueryService;
    private final MessageResolver messageResolver;

    @GetMapping(ApiPaths.Dev.METRICS_SYSTEM)
    public ApiResponse<SystemMetricsResponse> systemSnapshot() {
        return ApiResponse.success(messageResolver.resolve("common.success"), devMetricsQueryService.systemSnapshot());
    }

    @GetMapping(ApiPaths.Dev.METRICS_HTTP)
    public ApiResponse<List<HttpEndpointMetricsResponse>> httpSnapshot() {
        return ApiResponse.success(messageResolver.resolve("common.success"), devMetricsQueryService.httpSnapshot());
    }
}
