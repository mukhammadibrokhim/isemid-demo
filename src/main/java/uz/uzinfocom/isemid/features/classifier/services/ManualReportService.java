package uz.uzinfocom.isemid.features.classifier.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;

public interface ManualReportService {
    ApiResponsePage getAllByFilter(
            int page,
            int size,
            String shortName,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String code,
            Boolean includeTotal,
            String fullURL
    );

    ApiResponse<?> create(ManualReportRequest manualReportRequest);

    ApiResponse<?> getById(long id);

    ApiResponse<?> delete(long id);

    ApiResponse<?> update(long id, ManualReportRequest manualReportRequest);
}
