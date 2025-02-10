package uz.uzinfocom.isemid.services.value_set;

import uz.uzinfocom.isemid.models.payload.requests.ReqMkb10;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

public interface Mkb10Service {
    ApiResponsePage getMkb10ByFilter(
            int page,
            int size,
            String mkbCode,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameEn,
            String fullURL);

    ApiResponse<?> getById(long id);

    ApiResponse<?> updateById(long id, ReqMkb10 reqMkb10);

    ApiResponse<?> deleteById(long id);

    ApiResponse<?> getParentWithChildren();

    ApiResponse<?> getAllChildrenByParentId(long parentId);
}
