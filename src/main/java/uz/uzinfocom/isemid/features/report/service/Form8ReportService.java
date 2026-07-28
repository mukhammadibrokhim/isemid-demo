package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.Form8RegionComparisonDto;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form8ReportService {
    Form8RegionComparisonDto getRegionCitySummary(
            LocalDate fromDate1,
            LocalDate toDate1,
            LocalDate fromDate2,
            LocalDate toDate2,
            String mkb10Code,
            User currentUser,
            String stateCode,
            String cityCode);
}
