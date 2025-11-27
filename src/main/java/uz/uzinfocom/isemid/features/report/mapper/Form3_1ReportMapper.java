package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form3_1ReportResponse;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;

@Mapper(componentModel = "spring", uses = {OrganizationMapperHelper.class})
public interface Form3_1ReportMapper {
    Form3_1Report toEntity(Form3_1ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    Form3_1ReportResponse toResponse(Form3_1Report entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Form3_1Report entity, Form7ReportRequest request);
}
