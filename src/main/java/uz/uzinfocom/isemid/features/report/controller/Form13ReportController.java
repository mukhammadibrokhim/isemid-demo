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
import uz.uzinfocom.isemid.features.report.dtos.Form13ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form13ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.FORM13)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form13ReportController {
    private final Form13ReportService form13ReportService;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<Form13ReportDto> generate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ApiResponse.success(messageUtil, form13ReportService.generateForm13(start,end));
    }
}
