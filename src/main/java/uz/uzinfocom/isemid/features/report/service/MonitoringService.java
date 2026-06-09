package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;

import java.time.LocalDate;

public interface MonitoringService {
    MonitoringResponse getMonitoring(
            LocalDate fromDate,
            LocalDate toDate,
            String regionCode,
            String mkb10Code
    );
}
