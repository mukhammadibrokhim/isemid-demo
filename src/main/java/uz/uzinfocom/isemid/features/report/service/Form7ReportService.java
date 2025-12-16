package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form7ReportService {

    Page<FormReport7> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser
    );

    FormReport7 create(User currentUser);

    FormReport7 getById(Long id);

    FormReport7 update(Long id, Form7ReportRequest request);

    void delete(Long id);

}
