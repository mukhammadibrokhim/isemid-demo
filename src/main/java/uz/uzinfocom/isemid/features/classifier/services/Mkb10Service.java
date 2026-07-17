package uz.uzinfocom.isemid.features.classifier.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;

import java.util.List;

public interface Mkb10Service {
    Page<Mkb10> filter(
            int page,
            int size,
            String mkbCode,
            Long parentId, String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameEn);

    Mkb10 getById(long id);

    Mkb10 update(long id, Mkb10Request request);

    void delete(long id);

    List<Mkb10> getAllMkb10s(Long parentId, String search);

    List<Mkb10Projection> getNamesByCodesAndLocale(List<String> codes, String locale);
}
