package uz.uzinfocom.isemid.features.report.service;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.FormReport31;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface FormReport31Service {
    Page<FormReport31> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                 String cityCode, User currentUser);

    FormReport31 create(Form3_1ReportRequest request);

    FormReport31 getById(Long id);

    FormReport31 update(Long id, Form7ReportRequest request);

    void delete(Long id);
}
