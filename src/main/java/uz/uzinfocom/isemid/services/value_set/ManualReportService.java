package uz.uzinfocom.isemid.services.value_set;

import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

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
}
