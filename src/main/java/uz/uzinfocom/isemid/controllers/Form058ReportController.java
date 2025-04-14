package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Report")
@RequiredArgsConstructor
public class Form058ReportController {
    private final Form058ReportService form058ReportService;


    @Operation(
            summary = "Отчёт по инфекциям (Форма 1)",
            description = "Получение статистики инфекций по форме 058 с фильтрацией по датам.",
            parameters = {
                    @Parameter(
                            name = "fromDate",
                            description = "Дата начала (в формате yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "2024-01-01")
                    ),
                    @Parameter(
                            name = "toDate",
                            description = "Дата окончания (в формате yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "2024-12-31")
                    ),
                    @Parameter(
                            name = "birthDate",
                            description = "Дата рождения пациента (в формате yyyy-MM-dd)",
                            schema = @Schema(type = "string", format = "date", example = "1990-05-15")
                    )
            }
    )
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
