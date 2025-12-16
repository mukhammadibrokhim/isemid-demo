package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport7Response;
import uz.uzinfocom.isemid.features.report.model.FormReport7;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface Form7ReportMapper {

    FormReport7 toEntity(Form7ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    FormReport7Response toResponse(FormReport7 formReport7);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FormReport7 existingReport, Form7ReportRequest request);
}
