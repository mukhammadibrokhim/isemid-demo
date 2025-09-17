package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface LocationReportService {
    LocationReportDto getByLocation(
            String mkb10Code,
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser
    );
}
