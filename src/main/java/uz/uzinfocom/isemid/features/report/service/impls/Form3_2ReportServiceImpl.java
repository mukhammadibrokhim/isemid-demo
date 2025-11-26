package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;
import uz.uzinfocom.isemid.features.report.repository.Form3_2ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form3_2ReportService;

@Service
@RequiredArgsConstructor
public class Form3_2ReportServiceImpl implements Form3_2ReportService {
    private final Form3_2ReportRepository reportRepository;

    @Override
    public Form3_2Report create(Form3_1ReportRequest request) {
        return null;
    }

    @Override
    public Form3_2Report getById(Long id) {
        return null;
    }

    @Override
    public Form3_2Report update(Long id, Form7ReportRequest request) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
