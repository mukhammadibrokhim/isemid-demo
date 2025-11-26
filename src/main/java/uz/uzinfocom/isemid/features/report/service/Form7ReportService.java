package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form7Report;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form7ReportService {

    Page<Form7Report> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser
    );

    Form7Report create(Form7ReportRequest request);

    Form7Report getById(Long id);

    Form7Report update(Long id, Form7ReportRequest request);

    void delete(Long id);

}
