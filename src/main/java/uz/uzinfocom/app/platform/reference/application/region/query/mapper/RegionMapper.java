package uz.uzinfocom.app.platform.reference.application.region.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    RegionResponse toResponse(Region region);
}
