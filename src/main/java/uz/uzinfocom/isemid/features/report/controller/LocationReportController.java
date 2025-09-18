package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.service.LocationReportService;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.ROOT)
@Tag(name = "Location report")
@RequiredArgsConstructor
public class LocationReportController {
    private final LocationReportService locationReportService;
    private final MessageUtil messageUtil;

    @GetMapping("/by-location")
    public ApiResponse<?> reportByLocation(
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, locationReportService.getByLocation(mkb10Code, fromDate, toDate, currentUser));
    }
}
