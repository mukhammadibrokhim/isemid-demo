package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.service.LocationReportService;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.LOCATION)
@Tag(name = "Report Forms")
public class LocationReportController {
    private final LocationReportService locationReportService;
    private final MessageUtil messageUtil;

  public LocationReportController(@Qualifier("nationalDiseaseCasesReport") LocationReportService locationReportService,
      MessageUtil messageUtil) {
    this.locationReportService = locationReportService;
    this.messageUtil = messageUtil;
  }

  @GetMapping
    public ApiResponse<?> reportByLocation(
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) LevelType level,
            @RequestParam(required = false) String parentCode,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, locationReportService.getByLocation(mkb10Code, fromDate, toDate, level, parentCode, currentUser));
    }
}
