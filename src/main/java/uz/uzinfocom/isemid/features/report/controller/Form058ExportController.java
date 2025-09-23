package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.features.report.service.Form058ExportService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Form058.ROOT)
@Tag(name = "Form058 EXCEL Export", description = "API для экспорта данных формы 058 в Excel")
@RequiredArgsConstructor
public class Form058ExportController {
    private final Form058ExportService form058ExportService;

    @Operation(summary = "Экспорт всех данных формы 058 в Excel",
            description = "Позволяет экспортировать все записи формы 058 в Excel файл.")
    @GetMapping("/all/export")
    public ResponseEntity<?> exportAll(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @CurrentUser User user
    ) {
        return form058ExportService.allExport(fromDate, toDate, stateCode, cityCode, user);
    }

    @Operation(summary = "Экспорт полученных данных формы 058 в Excel",
            description = "Позволяет экспортировать только полученные записи формы 058 в Excel файл.")
    @GetMapping("/received/export")
    public ResponseEntity<?> exportReceived(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @CurrentUser User user
    ) {
        return form058ExportService.receivedExport(fromDate, toDate, stateCode, cityCode,user);
    }
}
