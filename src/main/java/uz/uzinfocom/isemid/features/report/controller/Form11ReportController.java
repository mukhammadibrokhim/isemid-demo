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
import uz.uzinfocom.isemid.features.report.service.Form11ReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.FORM11)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form11ReportController {
    private final Form11ReportService form11ReportService;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<?> form11Report(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String parentCode,
            @RequestParam(defaultValue = "100000") Long koef,
            @RequestParam(defaultValue = "10000") Long population,
            @CurrentUser User user
    ) {
        return ApiResponse.success(messageUtil, form11ReportService.generate(startDate, endDate, parentCode, koef, population, user));
    }
}
