package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form3_1ReportService {
    Page<Form3_1Report> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                  String cityCode, User currentUser);

    Form3_1Report create(Form3_1ReportRequest request);

    Form3_1Report getById(Long id);

    Form3_1Report update(Long id, Form7ReportRequest request);

    void delete(Long id);
}
