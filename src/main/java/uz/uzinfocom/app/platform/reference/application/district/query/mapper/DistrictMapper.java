package uz.uzinfocom.app.platform.reference.application.district.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;

@Mapper(componentModel = "spring")
public interface DistrictMapper {

    DistrictResponse toResponse(District district);
}
