package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.service.Form12ReportService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.ROOT)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form12ReportController {
    private final Form12ReportService form12ReportService;

    @Operation(
            summary = "Отчёт по форме 12",
            description = "Получение отчёта по форме 12 с фильтрацией по дате, дате рождения и полу пациента.",
            parameters = {
                    @Parameter(
                            name = "fromDate",
                            description = "Дата начала периода (yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "2024-01-01")
                    ),
                    @Parameter(
                            name = "toDate",
                            description = "Дата окончания периода (yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "2024-12-31")
                    ),
                    @Parameter(
                            name = "birthDate",
                            description = "Дата рождения пациента (yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "1990-05-15")
                    ),
                    @Parameter(
                            name = "gender",
                            description = "Пол пациента (MALE, FEMALE)",
                            schema = @Schema(type = "string", allowableValues = {"MALE", "FEMALE"})
                    )
            }
    )
    @GetMapping("/form12")
    public ApiResponse<?> infections(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate birthDate,
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser
    ) {
        return form12ReportService.getReport(fromDate, toDate, birthDate,mkb10Code, gender, currentUser);
    }
}
