package uz.uzinfocom.isemid.features.classifier.services;


import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

import java.util.List;

public interface ManualReportService {
    Page<ManualReport> filter(
            int page,
            int size,
            String shortName,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String code,
            Boolean includeTotal
    );

    ManualReport create(ManualReportRequest request);

    ManualReport getById(long id);

    void delete(long id);

    ManualReport update(long id, ManualReportRequest request);

    List<ManualReport> getManualReportsForForm(String type);
}
