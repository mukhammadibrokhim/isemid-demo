package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;

public interface Form3_2ReportService {
    Form3_2Report create(Form3_1ReportRequest request);

    Form3_2Report getById(Long id);

    Form3_2Report update(Long id, Form7ReportRequest request);

    void delete(Long id);
}
