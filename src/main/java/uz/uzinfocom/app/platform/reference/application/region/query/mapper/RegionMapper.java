package uz.uzinfocom.app.platform.reference.application.region.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionTableResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.projection.RegionTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Region;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    RegionResponse toResponse(Region region);

    RegionTableResponse toTableResponse(RegionTableProjection projection);
}
