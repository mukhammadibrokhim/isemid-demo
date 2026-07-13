package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.service.Form4_9ReportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.FORM_4)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form4_9ReportController {
    private final Form4_9ReportService form4_9ReportService;


    /**
     * GET /api/report/form4_9
     * Filter:
     * - fromDate, toDate (required)
     * - birthDate (optional)
     * - mkb10Code (optional)
     * - gender (optional)
     * - parentCode (for expanding into district/org)
     */
    @GetMapping
    public ResponseEntity<?> getForm4_9Report(
            @AuthenticationPrincipal User currentUser,

            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate birthDate,

            @RequestParam(required = false)
            String mkb10Code,

            @RequestParam(required = false)
            Gender gender,

            @RequestParam(required = false)
            String stateCode,

            @RequestParam(required = false)
            String cityCode,


            @RequestParam(required = false)
            String parentCode
    ) {

        Object result = form4_9ReportService.getReport(
                fromDate,
                toDate,
                birthDate,
                mkb10Code,
                gender,
                currentUser,
                parentCode,
                stateCode,
                cityCode
        );

        return ResponseEntity.ok(result);
    }

}
