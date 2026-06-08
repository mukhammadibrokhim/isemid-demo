package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths.Report;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.report.service.LocationReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

@RestController
@RequestMapping(Report.LOCATION_V2)
@Tag(name = "Report Forms")
public class LocationReportControllerV2 {
    private final LocationReportService locationReportService;
    private final MessageUtil messageUtil;

  public LocationReportControllerV2(@Qualifier("nationalDiseaseCasesReport") LocationReportService locationReportService,
      MessageUtil messageUtil) {
    this.locationReportService = locationReportService;
    this.messageUtil = messageUtil;
  }

  @GetMapping
    public ApiResponse<LocationReportDto> reportByLocation(
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) LevelType level,
            @RequestParam(required = false) String parentCode,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, locationReportService.getByLocation(mkb10Code, fromDate, toDate, parentCode, currentUser));
    }
}
