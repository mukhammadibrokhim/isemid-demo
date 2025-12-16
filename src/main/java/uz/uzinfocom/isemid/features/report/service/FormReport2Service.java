package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.model.FormReport2;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface FormReport2Service {
    Page<FormReport2> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                String cityCode, User currentUser);

    FormReport2 create(Form2ReportRequest request);

    FormReport2 getById(Long id);

    FormReport2 update(Long id, Form2ReportRequest request);

    void delete(Long id);
}
