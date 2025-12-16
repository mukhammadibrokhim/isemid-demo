package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form2Report;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form2ReportService {
    Page<Form2Report> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                String cityCode, User currentUser);

    Form2Report create(Form2ReportRequest request);

    Form2Report getById(Long id);

    Form2Report update(Long id, Form2ReportRequest request);

    void delete(Long id);
}
