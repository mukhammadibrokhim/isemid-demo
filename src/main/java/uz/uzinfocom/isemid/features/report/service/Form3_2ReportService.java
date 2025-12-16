package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form3_2ReportService {
    Page<Form3_2Report> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                  String cityCode, User currentUser);

    Form3_2Report create(Form3_2ReportRequest request);

    Form3_2Report getById(Long id);

    Form3_2Report update(Long id, Form3_2ReportRequest request);

    void delete(Long id);
}
