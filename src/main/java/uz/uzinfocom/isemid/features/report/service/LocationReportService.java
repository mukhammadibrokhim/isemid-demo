package uz.uzinfocom.isemid.features.report.service;

import java.time.LocalDate;
import uz.uzinfocom.isemid.features.report.dtos.LocationReportDto;
import uz.uzinfocom.isemid.features.user.models.User;

public interface LocationReportService {
    LocationReportDto getByLocation(
            String mkb10Code,
            LocalDate fromDate,
            LocalDate toDate,
            String parentCode,
            User currentUser
    );
}
