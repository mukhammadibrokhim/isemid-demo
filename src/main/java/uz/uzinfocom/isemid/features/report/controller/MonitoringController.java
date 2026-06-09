package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.dtos.response.MonitoringDynamicsResponse;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;
import uz.uzinfocom.isemid.features.report.service.RegionalDiseaseDashboardService;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.MONITORING)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;
    private final RegionalDiseaseDashboardService regionalDiseaseDashboardService;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<MonitoringResponse> getMonitoring(
            @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate toDate,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String mkb10Code
    ) {
        return ApiResponse.success(messageUtil, monitoringService.getMonitoring(fromDate, toDate, regionCode, mkb10Code));
    }

    @GetMapping("/dynamics")
    public ApiResponse<MonitoringDynamicsResponse> getDynamics(
        @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate fromDate,
        @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate toDate,
        @RequestParam(required = false) String regionCode,
        @RequestParam(required = false) String districtCode,
        @RequestParam(required = false) String mkb10Code
    ) {
        return ApiResponse.success(messageUtil, regionalDiseaseDashboardService.getMonitoring(fromDate, toDate, regionCode, districtCode, mkb10Code));
    }
}
