package uz.uzinfocom.isemid.features.analytic.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticReportRequest;
import uz.uzinfocom.isemid.features.analytic.dto.response.AnalyticReportResponse;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;

@Mapper(componentModel = "spring")
public interface AnalyticReportMapper {

    AnalyticReport toEntity(AnalyticReportRequest request);

    AnalyticReportResponse toResponse(AnalyticReport entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdOrg", ignore = true)
    @Mapping(target = "updatedOrg", ignore = true)
    void update(@MappingTarget AnalyticReport entity, AnalyticReportRequest request);
}
