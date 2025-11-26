package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form7ReportResponse;
import uz.uzinfocom.isemid.features.report.model.Form7Report;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface Form7ReportMapper {

    Form7Report toEntity(Form7ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    Form7ReportResponse toResponse(Form7Report form7Report);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Form7Report existingReport, Form7ReportRequest request);
}
