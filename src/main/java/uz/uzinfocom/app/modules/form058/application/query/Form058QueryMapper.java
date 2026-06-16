package uz.uzinfocom.app.modules.form058.application.query;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058DetailResult;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResult;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection.Form058TableProjection;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface Form058QueryMapper {

    Form058TableResult toTableResult(Form058TableProjection projection);

    @Mapping(target = "locationRegionCode", source = "location.regionCode")
    @Mapping(target = "locationDistrictCode", source = "location.districtCode")
    @Mapping(target = "locationNeighborhoodCode", source = "location.neighborhoodCode")
    @Mapping(target = "locationAddress", source = "location.address")
    Form058DetailResult toDetailResult(Form058 form058);
}
