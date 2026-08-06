package uz.uzinfocom.app.platform.reference.application.region.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionLookupResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionTableResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.projection.RegionTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Region;

@Mapper(componentModel = "spring", uses = RegionQueryMappingHelper.class)
public interface RegionMapper {

    RegionResponse toResponse(Region region);

    RegionTableResponse toTableResponse(RegionTableProjection projection);

    @Mapping(target = "name", source = "region", qualifiedByName = "regionLookupName")
    RegionLookupResponse toLookupResponse(Region region);
}
