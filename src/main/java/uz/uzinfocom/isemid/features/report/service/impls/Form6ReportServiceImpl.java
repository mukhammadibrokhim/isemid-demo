package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.service.Form6ReportService;

@Service
@RequiredArgsConstructor
public class Form6ReportServiceImpl implements Form6ReportService {
    private final Form058Repository form058Repository;
}
