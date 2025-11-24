package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form7Report;

public interface Form7ReportService {
    Form7Report create(Form7ReportRequest request);

    Form7Report getById(Long id);

    Form7Report update(Long id, Form7ReportRequest request);

    void delete(Long id);
}
