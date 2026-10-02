package uz.uzinfocom.app.modules.reference.application.icd10.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10Response;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10TableResponse;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10TreeResponse;
import uz.uzinfocom.app.modules.reference.application.icd10.query.projection.Icd10TableProjection;
import uz.uzinfocom.app.modules.reference.domain.Icd10;

@Mapper(componentModel = "spring")
public interface Icd10Mapper {

    Icd10Response toResponse(Icd10 entity, long childrenCount);

    Icd10TableResponse toTableResponse(Icd10TableProjection projection);

    Icd10TreeResponse toTreeResponse(Icd10 entity, long childrenCount, String name);
}
