package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.Form28_1ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form28_1ReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.Report.FORM28_1)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form28_1ReportController {
    private final Form28_1ReportService service;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<List<Form28_1ReportDto>> getReport(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String mkb10Code
    ) {
        return ApiResponse.success(messageUtil, service.getReports(fromDate, toDate, stateCode, cityCode, mkb10Code));
    }
}
