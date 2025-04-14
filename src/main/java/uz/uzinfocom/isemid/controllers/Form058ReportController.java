package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.Form058ReportService;

import java.time.LocalDate;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.REPORT_FORM1;

@RestController
@RequestMapping(REPORT_FORM1)
@Tag(name = "Form 1 report")
@RequiredArgsConstructor
public class Form058ReportController {
    private final Form058ReportService form058ReportService;

    @GetMapping("/form1")
    public ApiResponse<?> infections(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser
    ) {
       return form058ReportService.reportForm1(fromDate, toDate, birthDate, gender, currentUser);
    }
}
