package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.Form8RegionSummaryDto;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

public interface Form8ReportService {
    List<Form8RegionSummaryDto> getRegionCitySummary(
            LocalDate fromDate,
            LocalDate toDate,
            String mkb10Code,
            User currentUser,
            String stateCode,
            String cityCode);
}
