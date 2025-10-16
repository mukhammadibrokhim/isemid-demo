package uz.uzinfocom.isemid.features.analytic.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticReportRequest;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface AnalyticReportService {
    Page<AnalyticReport> getAnalyticReport(int page, int size, LocalDate fromDate, LocalDate toDate, String regionCode,
                                           String mkb10Code, User user);

    AnalyticReport create(AnalyticReportRequest request);

    AnalyticReport getOne(Long id);

    AnalyticReport update(Long id, AnalyticReportRequest request);

    void delete(Long id);
}
