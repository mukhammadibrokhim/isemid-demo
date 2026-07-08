package uz.uzinfocom.app.platform.reference.application.mkb10.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10Response;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10TableResponse;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.projection.Mkb10TableProjection;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;

@Mapper(componentModel = "spring")
public interface Mkb10Mapper {

    Mkb10Response toResponse(Mkb10 entity, long childrenCount);

    Mkb10TableResponse toTableResponse(Mkb10TableProjection projection);
}
