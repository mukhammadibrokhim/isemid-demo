package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.model.FormReport32;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface FormReport32Service {
    Page<FormReport32> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                 String cityCode, User currentUser);

    FormReport32 create(Form3_2ReportRequest request);

    FormReport32 getById(Long id);

    FormReport32 update(Long id, Form3_2ReportRequest request);

    void delete(Long id);
}
