package uz.uzinfocom.app.platform.reference.application.district.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.projection.DistrictTableProjection;
import uz.uzinfocom.app.platform.reference.domain.District;

@Mapper(componentModel = "spring")
public interface DistrictMapper {

    DistrictResponse toResponse(District district);

    DistrictTableResponse toTableResponse(DistrictTableProjection projection);
}
