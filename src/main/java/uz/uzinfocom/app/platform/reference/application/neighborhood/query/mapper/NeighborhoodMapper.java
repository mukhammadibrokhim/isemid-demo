package uz.uzinfocom.app.platform.reference.application.neighborhood.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodLookupResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodTableResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.projection.NeighborhoodTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;

@Mapper(componentModel = "spring", uses = {ReferenceMappingHelper.class, NeighborhoodQueryMappingHelper.class})
public interface NeighborhoodMapper {

    NeighborhoodResponse toResponse(Neighborhood neighborhood);

    @Mapping(target = "districtName", source = "parentCode", qualifiedByName = "districtName")
    @Mapping(target = "regionName", source = "parentCode", qualifiedByName = "regionNameByDistrictCode")
    NeighborhoodTableResponse toTableResponse(NeighborhoodTableProjection projection);

    @Mapping(target = "name", source = "neighborhood", qualifiedByName = "neighborhoodLookupName")
    NeighborhoodLookupResponse toLookupResponse(Neighborhood neighborhood);
}
