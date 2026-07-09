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
import uz.uzinfocom.isemid.features.report.service.Form6ReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.FORM6)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form6ReportController {
    private final Form6ReportService form6ReportService;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<Object> getForm6Report(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String mkb10Code,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, form6ReportService.generateFor6(currentUser, startDate, endDate, parentCode, mkb10Code));
    }
}
