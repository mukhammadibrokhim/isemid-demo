package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.Form058ReportService;

import java.time.LocalDate;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.REPORT_FORM1;

@RestController
@RequestMapping(REPORT_FORM1)
@Tag(name = "Form 058 report 6")
@RequiredArgsConstructor
public class Form058ReportController {
    private final Form058ReportService form058ReportService;

    @GetMapping("/summary")
    public ApiResponse<?> summary(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser
    ) {
        return form058ReportService.reportForm6(fromDate, toDate, birthDate, gender, currentUser);
    }

    @GetMapping("/by-region")
    public ApiResponse<?> byRegion(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser
    ) {
        return form058ReportService.reportForRegion(fromDate, toDate, birthDate, gender, currentUser);
    }

    @GetMapping("/by-district/{region-code}")
    public ApiResponse<?> byDistrict(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser,
            @PathVariable("region-code") String parameter) {
        return form058ReportService.reportForDistrictByRegionCode(parameter,fromDate, toDate, birthDate, gender, currentUser);
    }
}
