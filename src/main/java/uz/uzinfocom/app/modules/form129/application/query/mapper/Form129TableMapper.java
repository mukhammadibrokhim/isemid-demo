package uz.uzinfocom.app.modules.form129.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form129.application.query.dto.Form129TableResponse;
import uz.uzinfocom.app.modules.form129.application.query.projection.Form129TableProjection;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface Form129TableMapper {

    Form129TableResponse toTableResponse(Form129TableProjection projection);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "middleName", source = "middleName")
    Form129TableResponse.PatientShortResponse toResponse(Form129TableProjection.PatientProjection patient);
}
