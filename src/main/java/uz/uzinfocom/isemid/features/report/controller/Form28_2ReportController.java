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
import uz.uzinfocom.isemid.features.report.dtos.Form28_2ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form28_2ReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.Report.FORM28_2)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form28_2ReportController {
    private final Form28_2ReportService service;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<List<Form28_2ReportDto>> getReport(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String mkb10Code
    ) {
        return ApiResponse.success(messageUtil, service.getReports(fromDate, toDate, mkb10Code));
    }
}
