package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;

public interface MonitoringService {
    MonitoringResponse getMonitoring(String fromDate, String toDate);
}
