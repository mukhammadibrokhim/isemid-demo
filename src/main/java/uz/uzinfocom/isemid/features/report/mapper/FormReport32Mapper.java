package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport32Response;
import uz.uzinfocom.isemid.features.report.model.FormReport32;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface FormReport32Mapper {
    FormReport32 entity(Form3_2ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    FormReport32Response toResponse(FormReport32 entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FormReport32 entity, Form3_2ReportRequest request);
}
