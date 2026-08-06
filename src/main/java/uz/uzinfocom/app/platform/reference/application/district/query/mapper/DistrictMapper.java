package uz.uzinfocom.app.platform.reference.application.district.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictLookupResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.projection.DistrictTableProjection;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;
import uz.uzinfocom.app.platform.reference.domain.District;

@Mapper(componentModel = "spring", uses = {ReferenceMappingHelper.class, DistrictQueryMappingHelper.class})
public interface DistrictMapper {

    DistrictResponse toResponse(District district);

    @Mapping(target = "regionName", source = "parentCode", qualifiedByName = "regionName")
    DistrictTableResponse toTableResponse(DistrictTableProjection projection);

    @Mapping(target = "name", source = "district", qualifiedByName = "districtLookupName")
    DistrictLookupResponse toLookupResponse(District district);
}
