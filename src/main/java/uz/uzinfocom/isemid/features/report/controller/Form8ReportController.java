package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.features.report.dtos.Form8RegionSummaryDto;
import uz.uzinfocom.isemid.features.report.service.Form8ReportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.Report.FORM_8)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form8ReportController {
    private final Form8ReportService form8ReportService;

    /**
     * GET /api/report/form-8/region-summary
     * Viloyat kesimida umumiy son va shahar/tuman kesimidagi taqsimot.
     * Filter:
     * - fromDate, toDate (ixtiyoriy, ikkalasi ham berilishi kerak - biri berilib biri bo'sh qolsa natija bo'sh qaytadi)
     * - mkb10Code (ixtiyoriy)
     * - lang (ixtiyoriy: uz, Cyrl, ru, kaa)
     * - stateCode, cityCode (ixtiyoriy)
     */
    @GetMapping
    public ResponseEntity<?> getForm8RegionCitySummary(
            @AuthenticationPrincipal User currentUser,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false)
            String mkb10Code,

            @RequestParam(required = false)
            String stateCode,

            @RequestParam(required = false)
            String cityCode
    ) {
        List<Form8RegionSummaryDto> result = form8ReportService.getRegionCitySummary(
                fromDate,
                toDate,
                mkb10Code,
                currentUser,
                stateCode,
                cityCode
        );

        return ResponseEntity.ok(result);
    }

}
