package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.dtos.Form13ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form13ReportService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Form13ReportServiceImpl implements Form13ReportService {
    private final Form058Repository form058Repository;
    private final ManualReportService manualReportService;


    @Override
    public Form13ReportDto generateForm13(LocalDate start, LocalDate end) {
        return null;
    }
}
