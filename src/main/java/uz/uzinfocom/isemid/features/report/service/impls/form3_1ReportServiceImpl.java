package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;
import uz.uzinfocom.isemid.features.report.repository.Form3_1ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form3_1ReportService;

@Service
@RequiredArgsConstructor
public class form3_1ReportServiceImpl implements Form3_1ReportService {
    private final Form3_1ReportRepository form3_1ReportRepository;

    @Override
    public Form3_1Report create(Form3_1ReportRequest request) {
        return null;
    }

    @Override
    public Form3_1Report getById(Long id) {
        return null;
    }

    @Override
    public Form3_1Report update(Long id, Form7ReportRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
