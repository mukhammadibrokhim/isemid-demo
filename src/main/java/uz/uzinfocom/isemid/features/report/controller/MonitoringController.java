package uz.uzinfocom.isemid.features.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;

@RestController
@RequestMapping(ApiPaths.Report.ROOT)
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;
    private final MessageUtil messageUtil;

    @GetMapping("/monitoring")
    public ApiResponse<MonitoringResponse> getMonitoring(
            @RequestParam String fromDate,
            @RequestParam String toDate
    ) {
        return ApiResponse.success(messageUtil, monitoringService.getMonitoring(fromDate, toDate));
    }
}
