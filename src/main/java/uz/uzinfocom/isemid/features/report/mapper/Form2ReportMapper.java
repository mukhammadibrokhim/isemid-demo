package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form2ReportResponse;
import uz.uzinfocom.isemid.features.report.model.Form2Report;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface Form2ReportMapper {
    Form2Report toEntity(Form2ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrgUuid", qualifiedByName = "toOrgMiniResponse")
    Form2ReportResponse toResponse(Form2Report entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Form2Report entity, Form2ReportRequest request);
}
