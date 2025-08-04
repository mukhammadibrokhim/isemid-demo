package uz.uzinfocom.isemid.features.classifier.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;

public interface Mkb10Service {
    PagedResponse<?> getMkb10ByFilter(
            int page,
            int size,
            String mkbCode,
            Long parentId, String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameEn,
            String fullURL);

    ApiResponse<?> getById(long id);

    ApiResponse<?> update(long id, Mkb10Request request);

    ApiResponse<?> delete(long id);

    ApiResponse<?> getAllMkb10s(Long parentId, String search);

}
