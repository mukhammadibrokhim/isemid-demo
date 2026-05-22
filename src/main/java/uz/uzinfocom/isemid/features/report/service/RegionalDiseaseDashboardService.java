package uz.uzinfocom.isemid.features.report.service;

import java.time.LocalDate;
import uz.uzinfocom.isemid.features.report.dtos.response.MonitoringDynamicsResponse;

/**
 * The service is used as a datasource for the dashboard of situation center of Tashkent
 */
public interface RegionalDiseaseDashboardService {
    MonitoringDynamicsResponse getMonitoring(
        LocalDate fromDate,
        LocalDate toDate,
        String regionCode,
        String districtCode,
        String mkb10Code
    );
}
