package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;

public interface Form3_1ReportService {
    Form3_1Report create(Form3_1ReportRequest request);

    Form3_1Report getById(Long id);

    Form3_1Report update(Long id, Form7ReportRequest request);

    void delete(Long id);
}
