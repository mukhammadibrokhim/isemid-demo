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
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.service.Form1ReportService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.ROOT)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form1ReportController {
    private final Form1ReportService form1Service;
    private final MessageUtil messageUtil;
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
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) Gender gender,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, form1Service.getReport(fromDate, toDate, birthDate, mkb10Code, gender, currentUser));
    }

}
