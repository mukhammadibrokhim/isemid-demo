package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form7Report;
import uz.uzinfocom.isemid.features.report.repository.Form7ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form7ReportService;

@Service
@RequiredArgsConstructor
public class Form7ReportServiceImpl implements Form7ReportService {
    private final Form7ReportRepository reportRepository;

    @Override
    public Form7Report create(Form7ReportRequest request) {
        return null;
    }

    @Override
    public Form7Report getById(Long id) {
        return null;
    }

    @Override
    public Form7Report update(Long id, Form7ReportRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
