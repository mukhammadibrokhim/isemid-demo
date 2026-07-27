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
import uz.uzinfocom.isemid.features.report.dtos.Form8RegionComparisonDto;
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
     * GET /api/report/form-8/
     * Ikki davrni solishtirish: viloyat/shahar kesimida 1-davr, 2-davr va
     * ular orasidagi farq (o‘sish/pasayish) qiymatlari qaytariladi.
     * Filter:
     * - fromDate1, toDate1 (1-davr, majburiy)
     * - fromDate2, toDate2 (2-davr, majburiy)
     * - mkb10Code (ixtiyoriy)
     * - stateCode, cityCode (ixtiyoriy)
     */
    @GetMapping
    public ResponseEntity<?> getForm8RegionCitySummary(
            @AuthenticationPrincipal User currentUser,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate1,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate1,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate2,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate2,

            @RequestParam(required = false)
            String mkb10Code,

            @RequestParam(required = false)
            String stateCode,

            @RequestParam(required = false)
            String cityCode
    ) {
        List<Form8RegionComparisonDto> result = form8ReportService.getRegionCitySummary(
                fromDate1,
                toDate1,
                fromDate2,
                toDate2,
                mkb10Code,
                currentUser,
                stateCode,
                cityCode
        );

        return ResponseEntity.ok(result);
    }

}
