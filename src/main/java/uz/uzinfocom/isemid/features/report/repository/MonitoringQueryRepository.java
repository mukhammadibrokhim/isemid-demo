package uz.uzinfocom.isemid.features.report.repository;

import uz.uzinfocom.isemid.features.report.dtos.request.MonitoringFilter;
import uz.uzinfocom.isemid.features.report.projection.MonitoringDiseaseRow;
import uz.uzinfocom.isemid.features.report.projection.MonitoringLocationRow;

import java.util.List;

public interface MonitoringQueryRepository {

    List<MonitoringLocationRow> findLocationStatistics(MonitoringFilter filter);

    List<MonitoringDiseaseRow> findDiseaseStatistics(MonitoringFilter filter);
}