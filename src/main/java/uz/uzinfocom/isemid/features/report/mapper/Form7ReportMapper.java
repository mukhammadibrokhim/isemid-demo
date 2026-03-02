package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.FormReport7Request;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport7Response;
import uz.uzinfocom.isemid.features.report.model.FormReport7;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface Form7ReportMapper {

    FormReport7 toEntity(FormReport7Request request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    FormReport7Response toResponse(FormReport7 formReport7);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FormReport7 existingReport, FormReport7Request request);
}
